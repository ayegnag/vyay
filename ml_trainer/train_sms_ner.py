"""
SMS NER Training Pipeline
==========================
Fine-tunes a BERT model for Named Entity Recognition on SMS transactions.
Outputs a TFLite model ready to ship inside your Android APK.

Model options (set MODEL_CHOICE in config below):
  "tiny"       → prajjwal1/bert-tiny       ~17MB  ~8ms inference   good accuracy
  "mobile"     → google/mobilebert-uncased ~94MB  ~150ms inference  best accuracy

Setup:
  pip install transformers datasets torch scikit-learn seqeval

Then run:
  python train_sms_ner.py --action all
  python train_sms_ner.py --action all --model-choice mobile
"""

import json
import os
import random
import numpy as np
from pathlib import Path
from typing import List, Dict, Tuple, Optional

import torch
from torch.utils.data import Dataset, DataLoader
from transformers import (
    AutoTokenizer,
    AutoModelForTokenClassification,
    TrainingArguments,
    Trainer,
    DataCollatorForTokenClassification,
    EarlyStoppingCallback,
)
from sklearn.model_selection import train_test_split

# ─────────────────────────────────────────────
# CONFIG
# ─────────────────────────────────────────────

# ── Model choice ──────────────────────────────────────────────────────
# Change MODEL_CHOICE here, or override at runtime with --model-choice:
#   python train_sms_ner.py --action all --model-choice mobile
#
#   "tiny"   → prajjwal1/bert-tiny        ~17MB  ~8ms on Android   recommended
#   "mobile" → google/mobilebert-uncased  ~94MB  ~150ms on Android  highest accuracy
MODEL_CHOICE = "tiny"

MODELS = {
    "tiny": {
        # "name":       "prajjwal1/bert-tiny",
        "name": "google/bert_uncased_L-2_H-128_A-2",
        "output_dir": "./model_output_tiny",
        "tflite":     "./sms_ner_tiny.tflite",
        "batch_size": 32,    # tiny model fits larger batches
        "lr":         5e-5,  # tiny benefits from slightly higher LR
    },
    "mobile": {
        "name":       "google/mobilebert-uncased",
        "output_dir": "./model_output_mobile",
        "tflite":     "./sms_ner_mobile.tflite",
        "batch_size": 16,
        "lr":         3e-5,
    },
}

# Resolved at runtime — do not edit these directly
MODEL_NAME  = MODELS[MODEL_CHOICE]["name"]
OUTPUT_DIR  = MODELS[MODEL_CHOICE]["output_dir"]
TFLITE_PATH = MODELS[MODEL_CHOICE]["tflite"]
BATCH_SIZE  = MODELS[MODEL_CHOICE]["batch_size"]
LR          = MODELS[MODEL_CHOICE]["lr"]
MAX_LENGTH  = 128   # SMS messages are short; 128 tokens is plenty for both models
EPOCHS      = 20    # EarlyStopping usually triggers at epoch 8-12
SEED        = 42

# BIO label scheme — O must be index 0 (matches TF Lite post-processing)
LABELS = [
    "O",
    "B-AMOUNT",           "I-AMOUNT",
    "B-ACCOUNT",          "I-ACCOUNT",
    "B-RECEIVER_ACCOUNT", "I-RECEIVER_ACCOUNT",
    "B-BALANCE",          "I-BALANCE",
    "B-MERCHANT",         "I-MERCHANT",
    "B-DATE",             "I-DATE",
    "B-TX_DEBIT",         "I-TX_DEBIT",
    "B-TX_CREDIT",        "I-TX_CREDIT",
    "B-TX_BALANCE",       "I-TX_BALANCE",
]
LABEL2ID = {l: i for i, l in enumerate(LABELS)}
ID2LABEL = {i: l for i, l in enumerate(LABELS)}
NUM_LABELS = len(LABELS)


# ─────────────────────────────────────────────
# 1. DATASET
# ─────────────────────────────────────────────

class SmsNerDataset(Dataset):
    """
    Takes character-span annotations → tokenizes with WordPiece → aligns BIO tags.

    The tricky part: WordPiece splits "Rs.500" into ["rs", ".", "500"] and BERT
    prepends [CLS] and appends [SEP]. We must align our entity spans to this new
    token sequence. Tokens from continuation subwords get label -100 so the loss
    ignores them (standard HuggingFace convention).
    """

    def __init__(self, examples: List[Dict], tokenizer, max_length: int = MAX_LENGTH):
        self.examples   = examples
        self.tokenizer  = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.examples)

    def __getitem__(self, idx):
        ex      = self.examples[idx]
        text    = ex["text"]
        entities = ex["entities"]

        # Build a character-level label array first
        char_labels = ["O"] * len(text)
        for ent in entities:
            label = ent["label"]
            for ci in range(ent["start"], min(ent["end"], len(text))):
                char_labels[ci] = f"B-{label}" if ci == ent["start"] else f"I-{label}"

        # Tokenize with offset mapping so we know which char each token covers
        enc = self.tokenizer(
            text,
            max_length=self.max_length,
            truncation=True,
            padding="max_length",
            return_offsets_mapping=True,
        )

        # Align char labels → token labels
        token_labels = []
        for token_idx, (start, end) in enumerate(enc["offset_mapping"]):
            if start == end:
                # Special token ([CLS], [SEP], [PAD]) → ignore in loss
                token_labels.append(-100)
            else:
                token_labels.append(LABEL2ID.get(char_labels[start], 0))

        enc.pop("offset_mapping")
        enc["labels"] = token_labels
        return {k: torch.tensor(v) for k, v in enc.items()}


# ─────────────────────────────────────────────
# 2. METRICS
# ─────────────────────────────────────────────

def compute_metrics(p):
    """Compute entity-level F1 using seqeval (standard NER metric library)."""
    try:
        from seqeval.metrics import f1_score, classification_report
    except ImportError:
        print("Install seqeval: pip install seqeval")
        return {}

    predictions, labels = p
    predictions = np.argmax(predictions, axis=2)

    true_labels, true_preds = [], []
    for pred_seq, label_seq in zip(predictions, labels):
        seq_true, seq_pred = [], []
        for pred_id, label_id in zip(pred_seq, label_seq):
            if label_id == -100:
                continue
            seq_true.append(ID2LABEL[label_id])
            seq_pred.append(ID2LABEL[pred_id])
        true_labels.append(seq_true)
        true_preds.append(seq_pred)

    f1 = f1_score(true_labels, true_preds)
    print(classification_report(true_labels, true_preds))
    return {"f1": f1}


# ─────────────────────────────────────────────
# 3. TRAIN
# ─────────────────────────────────────────────

def train(data_path: str = "dataset_char_spans.json"):
    random.seed(SEED)
    np.random.seed(SEED)
    torch.manual_seed(SEED)

    # Load data
    with open(data_path) as f:
        all_data = json.load(f)

    train_data, val_data = train_test_split(all_data, test_size=0.15, random_state=SEED)
    print(f"Train: {len(train_data)}  Val: {len(val_data)}")

    # Tokenizer + Model
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME, use_fast=False)
    model = AutoModelForTokenClassification.from_pretrained(
        MODEL_NAME,
        num_labels=NUM_LABELS,
        id2label=ID2LABEL,
        label2id=LABEL2ID,
        ignore_mismatched_sizes=True,
    )

    train_ds = SmsNerDataset(train_data, tokenizer)
    val_ds   = SmsNerDataset(val_data, tokenizer)

    args = TrainingArguments(
        output_dir=OUTPUT_DIR,
        num_train_epochs=EPOCHS,
        per_device_train_batch_size=BATCH_SIZE,
        per_device_eval_batch_size=BATCH_SIZE,
        learning_rate=LR,
        weight_decay=0.01,
        warmup_ratio=0.1,
        lr_scheduler_type="cosine",
        eval_strategy="epoch",
        save_strategy="epoch",
        load_best_model_at_end=True,
        metric_for_best_model="f1",
        logging_steps=20,
        fp16=torch.cuda.is_available(),    # Mixed precision if GPU available
        seed=SEED,
        report_to="none",
    )

    trainer = Trainer(
        model=model,
        args=args,
        train_dataset=train_ds,
        eval_dataset=val_ds,
        processing_class=tokenizer,
        data_collator=DataCollatorForTokenClassification(tokenizer),
        compute_metrics=compute_metrics,
        callbacks=[EarlyStoppingCallback(early_stopping_patience=3)],
    )

    print("Starting training...")
    trainer.train()

    # Save best model
    trainer.save_model(OUTPUT_DIR)
    tokenizer.save_pretrained(OUTPUT_DIR)
    print(f"✅ Saved model to {OUTPUT_DIR}")

    return trainer


# ─────────────────────────────────────────────
# 4. INFERENCE TEST (PyTorch)
#    Verify the model works before TF Lite export
# ─────────────────────────────────────────────

def predict_pytorch(text: str, model_dir: str = OUTPUT_DIR):
    from transformers import pipeline

    ner = pipeline(
        "ner",
        model=model_dir,
        tokenizer=model_dir,
        aggregation_strategy="simple",   # Merges B/I tokens into full entities
    )
    results = ner(text)
    print(f"\nInput: {text}")
    for ent in results:
        print(f"  [{ent['entity_group']}] '{ent['word']}' (score={ent['score']:.2f})")
    return results


# ─────────────────────────────────────────────
# 5. EXPORT TO TF LITE
#    PyTorch → ONNX → TF → TF Lite (INT8 quantized)
#
#    Alternative path: use Optimum (HuggingFace's export tool)
#    pip install optimum[exporters]
# ─────────────────────────────────────────────

def export_tflite(model_dir: str = OUTPUT_DIR, output_path: str = TFLITE_PATH):
    """
    Exports the fine-tuned model to a TF Lite flatbuffer via litert-torch.

    Install:
        pip install litert-torch

    After export:
      • Copy sms_ner_model.tflite  → app/src/main/assets/sms_ner/
      • Copy vocab.txt             → app/src/main/assets/sms_ner/
    """
    try:
        import litert_torch
    except ImportError:
        print("Run: pip install litert-torch --no-deps")
        return

    from transformers import AutoModelForTokenClassification, AutoTokenizer

    print("Loading model for export...")
    tokenizer = AutoTokenizer.from_pretrained(model_dir, use_fast=False)
    model = AutoModelForTokenClassification.from_pretrained(model_dir)
    model.eval()

    # Build sample inputs — must exactly match what the Android interpreter sends.
    # return_token_type_ids=True is required: MobileBERT fast tokenizer omits
    # token_type_ids by default, but the model signature expects all three tensors.
    encoded = tokenizer(
        "Your a/c XX1234 debited Rs.500 on 01-Jan-2024 Bal Rs.12500",
        max_length=MAX_LENGTH,
        padding="max_length",
        truncation=True,
        return_tensors="pt",
        return_token_type_ids=True,   # ← explicit: fast tokenizer skips this otherwise
    )

    sample_inputs = (
        encoded["input_ids"],
        encoded["attention_mask"],
        encoded["token_type_ids"],
    )

    # ── Export float32 ───────────────────────────────────────────────────
    # litert_torch.convert() uses torch.export internally which is incompatible
    # with torch.quantization.quantize_dynamic (old-format LinearPackedParamsBase
    # objects cannot be traced). Float32 export is the reliable path.
    #
    # Model size by base model:
    #   prajjwal1/bert-tiny        → ~17MB  ✅ recommended
    #   google/mobilebert-uncased  → ~94MB  (works but large)
    print("Converting model to TFLite (float32)...")
    edge_model = litert_torch.convert(model, sample_inputs)
    edge_model.export(output_path)
    _report_size(output_path)


def _report_size(output_path: str) -> None:
    import os
    size_mb = os.path.getsize(output_path) / 1024 / 1024
    status = "✅" if size_mb < 40 else "⚠️  quantization may not have applied —"
    print(f"{status} TFLite model saved to {output_path} ({size_mb:.1f} MB)")
    if size_mb > 40:
        print(f"   Expected ~26 MB after INT8 quantization, got {size_mb:.1f} MB")
        print(f"   Try: python train_sms_ner.py --action export --quant-only")
    print(f"   Copy to: app/src/main/assets/sms_ner/sms_ner_model.tflite")
    print(f"   Also copy vocab.txt → app/src/main/assets/sms_ner/vocab.txt")


def _export_via_onnx(model_dir: str, output_path: str):
    """Manual export path if Optimum is unavailable."""
    import subprocess

    # Step 1: Export to ONNX
    subprocess.run([
        "python", "-m", "transformers.onnx",
        "--model", model_dir,
        "--feature", "token-classification",
        "onnx_export/",
    ], check=True)

    # Step 2: ONNX → TF Lite (requires onnx-tf)
    print("Next: pip install onnx-tf && python -c \"import onnx_tf; onnx_tf.backend.prepare(...)\"")
    print("Or use: https://github.com/onnx/onnx-tensorflow")


# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter,
        description="""
SMS NER Training Pipeline

Model choices:
  tiny   → prajjwal1/bert-tiny        ~17MB  ~8ms on Android  (default)
  mobile → google/mobilebert-uncased  ~94MB  ~150ms on Android

Examples:
  python train_sms_ner.py --action all
  python train_sms_ner.py --action all   --model-choice mobile
  python train_sms_ner.py --action train --model-choice tiny
  python train_sms_ner.py --action export
        """)
    parser.add_argument("--action",       choices=["train", "test", "export", "all"], default="all")
    parser.add_argument("--data",         default="dataset_char_spans.json")
    parser.add_argument("--model-choice", choices=["tiny", "mobile"], default=None,
                        dest="model_choice",
                        help="Override MODEL_CHOICE from config (tiny or mobile)")
    args = parser.parse_args()

    # Apply --model-choice override before anything else resolves OUTPUT_DIR/TFLITE_PATH
    if args.model_choice:
        import sys
        this = sys.modules[__name__]
        choice = args.model_choice
        this.MODEL_NAME  = MODELS[choice]["name"]
        this.OUTPUT_DIR  = MODELS[choice]["output_dir"]
        this.TFLITE_PATH = MODELS[choice]["tflite"]
        this.BATCH_SIZE  = MODELS[choice]["batch_size"]
        this.LR          = MODELS[choice]["lr"]
        print(f"Model override: {choice} → {this.MODEL_NAME}")
        print(f"  Output dir : {this.OUTPUT_DIR}")
        print(f"  TFLite path: {this.TFLITE_PATH}")
    else:
        print(f"Using MODEL_CHOICE='{MODEL_CHOICE}' from config → {MODEL_NAME}")

    if args.action in ("train", "all"):
        train(args.data)

    if args.action in ("test", "all"):
        test_cases = [
            "Your a/c XX9999 has been debited for Rs.1,200.00 on 05-Apr-2024. Avl Bal: Rs.33,800",
            "UPI: INR 499 paid to Netflix. A/c **8877 debited. Bal:INR 42,501",
            "Salary of Rs 85,000 credited to your account ending 4321 on 01-Apr-2024. Balance: Rs 1,02,340",
            "Balance Enquiry: Your HDFC Bank a/c 1234 has available balance of Rs.45,000 as on 01-Apr-2024.",
            "Sent Rs.1500.00 From HDFC Bank A/C *1331 To Grahsthi crockery store On 02/03/26 Ref 119390319698 Not You? Call 18002586161/SMS BLOCK UPI to 7308080808",
            "Available Bal in HDFC Bank A/c XX1931 as on yesterday:02-MAR-26 is INR 45,60,910.75. Cheques are subject to clearing.For real time A/C Bal dial 18002703333.",
        ]
        for t in test_cases:
            predict_pytorch(t, OUTPUT_DIR)

    if args.action in ("export", "all"):
        export_tflite(OUTPUT_DIR)