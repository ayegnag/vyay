"""
SMS NER Data Augmentation & Annotation Toolkit
==============================================
Helps you build a training dataset large enough to generalize.
Run: python data_toolkit.py
"""

import json
import random
import re
from itertools import product
from typing import List, Dict, Tuple

# ─────────────────────────────────────────────
# 1. TEMPLATE ENGINE
#    Define parameterized SMS templates. The {SLOT}
#    markers are what the NER model will learn to find.
# ─────────────────────────────────────────────

TEMPLATES = [
    # Debit templates
    "Your a/c {ACCOUNT} has been debited for {CURRENCY}{AMOUNT} on {DATE}. Avl Bal: {CURRENCY}{BALANCE}",
    "Txn of {CURRENCY}{AMOUNT} done from {BANK} a/c XX{ACCOUNT} at {MERCHANT} on {DATE}. Bal {CURRENCY}{BALANCE}",
    "Alert: {CURRENCY} {AMOUNT} debited from acct ending {ACCOUNT} at {MERCHANT}. Available balance is {CURRENCY} {BALANCE}",
    "Debit of {CURRENCY}{AMOUNT} from {BANK} a/c {ACCOUNT} at {MERCHANT} on {DATE}. Avail bal: {CURRENCY}{BALANCE}",
    "{BANK}: A/C XX{ACCOUNT} debited {CURRENCY} {AMOUNT} for {MERCHANT}. Avl Bal {CURRENCY} {BALANCE}.",
    "{BANK}: {CURRENCY} {AMOUNT} debited from a/c *{ACCOUNT} on {DATE} to a/c **{RECEIVERACCOUNT} (UPI Ref No. {REF}) Not You? Call {CUSTCARECALL} to report.",
    "Transaction alert: {CURRENCY} {AMOUNT} withdrawn from your account {ACCOUNT}. Balance: {CURRENCY} {BALANCE}",
    "Sent {CURRENCY}{AMOUNT} to {MERCHANT} from a/c **{ACCOUNT}. Ref: {REF}. Bal: {CURRENCY}{BALANCE}",
    "Sent {CURRENCY}{AMOUNT} from {BANK} A/C *{ACCOUNT} To {MERCHANT} On {DATE} Ref {REF} Not You? Call {CUSTCARECALL}/SMS BLOCK UPI to {CUSTCARESMS}",
    "PAYMENT ALERT! {CURRENCY} {AMOUNT} deducted from {BANK} A/C No {ACCOUNT} towards {MERCHANT} UMRN:{UMRN}",

    # Credit templates
    "{CURRENCY} {AMOUNT} credited to your account **{ACCOUNT} from {MERCHANT} on {DATE}. Balance: {CURRENCY} {BALANCE}",
    "Money received! {CURRENCY}{AMOUNT} added to a/c {ACCOUNT} from {MERCHANT}. New Bal: {CURRENCY}{BALANCE}",
    "{BANK}: {CURRENCY}{AMOUNT} received in a/c XX{ACCOUNT}. Avl bal {CURRENCY}{BALANCE}",

    # UPI-style
    "UPI: {CURRENCY}{AMOUNT} paid to {MERCHANT}. UPI Ref:{REF}. A/c XX{ACCOUNT} debited. Bal:{CURRENCY}{BALANCE}",
    "UPI txn of {CURRENCY}{AMOUNT} to {MERCHANT} successful. Acct {ACCOUNT}. Bal {CURRENCY}{BALANCE}",

    # Balance-only templates (no transaction — TX_BALANCE)
    "Balance Enquiry: Your {BANK} a/c {ACCOUNT} has available balance of {CURRENCY}{BALANCE} as on {DATE}.",
    "Acct bal alert: Avl Bal in {BANK} a/c {ACCOUNT} as on {DATE} is {CURRENCY}{BALANCE}.",
    "Your {BANK} account {ACCOUNT} balance is {CURRENCY}{BALANCE}. For help call {CUSTCARECALL}.",
]

# ─────────────────────────────────────────────
# 2. SLOT FILLERS
#    Vary these to create diverse, realistic SMS
# ─────────────────────────────────────────────

SLOT_VALUES = {
    "ACCOUNT":         ["1234", "5678", "9012", "3344", "7890", "4567", "6677", "2233", "8899", "1122"],
    "RECEIVERACCOUNT": ["3434", "5558", "9242", "6345", "7760", "4597", "2677", "2453", "8539", "1789"],
    "CURRENCY":        ["Rs.", "Rs", "INR ", "INR", "₹", "$", "USD "],
    "MERCHANT":        ["Amazon", "Swiggy", "Netflix", "Zomato", "Flipkart", "Uber", "PhonePe Merchant",
                        "BigBazaar", "Myntra", "Ola", "BookMyShow", "MakeMyTrip", "Dunzo", "Blinkit"],
    "BANK":            ["HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Kotak Bank", "Yes Bank", "IDFC Bank"],
    "DATE":            ["01-Jan-2024", "15/02/2024", "20-Mar-24", "Apr 5 2024", "12-12-2023",
                        "05 Jun 2024", "2024-03-15", "31/03/24"],
    "REF":             ["123456789", "987654321", "UPI2024031500001", "TXN87654"],
    "CUSTCARECALL":    ["18002586161", "18001234", "18004253800", "18001080", "18601231122"],
    "CUSTCARESMS":     ["7308080808", "9542000030", "9215676766", "1600315800", "1600300800"],
    # Note: leading spaces stripped from UMRN values (would corrupt span detection)
    "UMRN":            ["SBIN00000044749887", "HDFCN5345345345345", "ICICIN0000003453455", "PNB00000075674988"],
}

# Maps slot name → the NER label assigned to it.
# Slots absent from this dict are filled (so templates render cleanly)
# but never annotated — they're noise the model should learn to ignore.
SLOT_TO_LABEL = {
    "ACCOUNT":         "ACCOUNT",
    "RECEIVERACCOUNT": "RECEIVER_ACCOUNT",
    "AMOUNT":          "AMOUNT",
    "BALANCE":         "BALANCE",
    "MERCHANT":        "MERCHANT",
    "DATE":            "DATE",
    # BANK, CURRENCY, REF, CUSTCARECALL, CUSTCARESMS, UMRN → intentionally absent
}

# ─────────────────────────────────────────────
# TX TYPE TRIGGER PATTERNS
#
# Each entry is (regex_pattern, label).
# Patterns are matched case-insensitively against the rendered SMS.
# The matched span is tagged as B-TX_* / I-TX_* so the model learns
# WHICH words signal transaction type, not just that "there is a type".
#
# Design notes:
#  • Longer / more specific patterns are listed first — matched first-wins.
#  • TX_BALANCE patterns only appear in balance-only templates; in mixed
#    templates (debit/credit + balance shown), only TX_DEBIT/TX_CREDIT fires.
#  • The model learns from full context so even templates without a textbook
#    trigger word (e.g. "Sent Rs.X to Y") will generalise correctly.
# ─────────────────────────────────────────────

TX_PATTERNS: List[Tuple[str, str]] = [
    # ── Debit ──────────────────────────────────
    (r'\bpayment\s+alert\b',  "TX_DEBIT"),
    (r'\bdeducted\b',         "TX_DEBIT"),
    (r'\bwithdrawn\b',        "TX_DEBIT"),
    (r'\bdebited\b',          "TX_DEBIT"),
    (r'\bdebit\b',            "TX_DEBIT"),
    (r'\bsent\b',             "TX_DEBIT"),
    (r'\bpaid\b',             "TX_DEBIT"),

    # ── Credit ─────────────────────────────────
    (r'\bcredited\b',         "TX_CREDIT"),
    (r'\breceived\b',         "TX_CREDIT"),
    (r'\badded\b',            "TX_CREDIT"),
    (r'\bcredit\b',           "TX_CREDIT"),

    # ── Balance-only ───────────────────────────
    # Only present in balance-only templates; in mixed templates these
    # phrases appear but the debit/credit pattern fires first.
    (r'\bbalance\s+enquiry\b', "TX_BALANCE"),
    (r'\bacct\s+bal\s+alert\b',"TX_BALANCE"),
    (r'\baccount\s+balance\b', "TX_BALANCE"),
]


def annotate_tx_type(text: str, existing_ranges: List[Tuple[int, int]]) -> List[Dict]:
    """
    Scan rendered SMS text for transaction-type trigger words/phrases.
    Returns entity spans for the first matching pattern, skipping any
    positions already occupied by another entity (e.g. don't tag 'received'
    if it was already claimed as part of a MERCHANT name).

    Only ONE TX_* entity is emitted per SMS — the first (leftmost) match.
    That's enough: a single message has one transaction type.
    """
    lower = text.lower()

    for pattern, label in TX_PATTERNS:
        for m in re.finditer(pattern, lower):
            start, end = m.start(), m.end()
            # Skip if this span overlaps an already-annotated entity
            overlap = any(not (end <= r[0] or start >= r[1]) for r in existing_ranges)
            if not overlap:
                return [{"start": start, "end": end, "label": label}]

    return []   # No trigger found — model infers from context alone

def generate_amount() -> Tuple[str, str]:
    """Returns (display_amount, clean_amount)"""
    choices = [
        ("299", "299"),
        ("500.00", "500.00"),
        ("1,500", "1500"),
        ("2,300.00", "2300.00"),
        ("10,000.00", "10000.00"),
        ("3,499", "3499"),
        ("120.50", "120.50"),
        ("5,000", "5000"),
        ("750", "750"),
    ]
    d, c = random.choice(choices)
    return d, c

def generate_balance() -> str:
    amounts = ["5,230", "12,500.00", "45,000.00", "8,750", "14,501", "90,450.00",
               "22,341", "15,600", "2,890.00", "3,200", "1,05,320.00"]
    return random.choice(amounts)


# ─────────────────────────────────────────────
# 3. GENERATE ANNOTATED EXAMPLES
# ─────────────────────────────────────────────

def fill_template(template: str) -> Dict:
    """
    Fill a template and return text + entity spans.

    Key design points:
      1. Fill ALL slots first → get the final string (no span drift)
      2. Only annotate slots present in SLOT_TO_LABEL
      3. Non-annotated slots (CUSTCARECALL, UMRN, etc.) are filled so the
         rendered SMS looks realistic, but the model learns to ignore them
      4. Search for each value in the final string → correct spans
    """
    # All slots that can appear across any template — must be exhaustive
    slot_order = [
        "CURRENCY", "MERCHANT", "ACCOUNT", "RECEIVERACCOUNT",
        "BALANCE", "AMOUNT", "BANK", "DATE", "REF",
        "CUSTCARECALL", "CUSTCARESMS", "UMRN",
    ]

    # ── Step 1: Decide values for every slot present in this template ─────
    filled = {}
    amount_str, _ = generate_amount()
    filled["AMOUNT"]  = amount_str
    filled["BALANCE"] = generate_balance()

    for slot in slot_order:
        if slot in ("AMOUNT", "BALANCE"):
            continue
        if slot in SLOT_VALUES and f"{{{slot}}}" in template:
            filled[slot] = random.choice(SLOT_VALUES[slot])

    # ── Step 2: Single-pass substitution ──────────────────────────────────
    result = template
    for slot in slot_order:
        value = filled.get(slot, "")
        if value:
            result = result.replace(f"{{{slot}}}", value)

    # Sanity check: no unfilled placeholders remain
    if re.search(r'\{[A-Z]+\}', result):
        raise ValueError(f"Unfilled slots in: {result}")

    # ── Step 3: Find each ANNOTATED value in the final string ─────────────
    # Only slots in SLOT_TO_LABEL get entity annotations.
    entities = []
    to_find = []

    for slot, label in SLOT_TO_LABEL.items():
        value = filled.get(slot)
        if not value:
            continue
        idx = result.find(value)
        if idx != -1:
            to_find.append((idx, label, value))

    # Sort left-to-right so overlap resolution is deterministic
    to_find.sort(key=lambda x: x[0])
    used_ranges = []

    for _, label, value in to_find:
        search_pos = 0
        while True:
            idx = result.find(value, search_pos)
            if idx == -1:
                break
            end = idx + len(value)
            overlap = any(not (end <= r[0] or idx >= r[1]) for r in used_ranges)
            if not overlap:
                entities.append({"start": idx, "end": end, "label": label})
                used_ranges.append((idx, end))
                break
            search_pos = idx + 1

    # Final safety: drop any entity whose span exceeds text length
    text_len = len(result)
    entities = [e for e in entities if e["start"] < text_len and e["end"] <= text_len]

    # ── Step 4: Annotate transaction type trigger word ────────────────────
    occupied = [(e["start"], e["end"]) for e in entities]
    tx_entities = annotate_tx_type(result, occupied)
    entities.extend(tx_entities)

    return {"text": result, "entities": entities}


def generate_dataset(n: int = 500) -> List[Dict]:
    examples = []
    for _ in range(n):
        template = random.choice(TEMPLATES)
        try:
            example = fill_template(template)
            if example["entities"]:  # Only keep if we extracted something
                examples.append(example)
        except (ValueError, KeyError):
            pass  # Skip malformed fills
    return examples


# ─────────────────────────────────────────────
# 4. CONVERT TO TOKEN-LEVEL BIO TAGS
#    This is the format models actually train on.
#    B = Beginning of entity, I = Inside, O = Outside
# ─────────────────────────────────────────────

def char_to_bio(example: Dict) -> Dict:
    """Convert character-level spans to token-level BIO tags."""
    text = example["text"]
    tokens = []
    bio_tags = []
    char_tags = ["O"] * len(text)

    # Mark character positions
    for ent in example["entities"]:
        label = ent["label"]
        start = ent["start"]
        end   = min(ent["end"], len(text))   # Clamp to text length (defensive)
        if start >= len(text) or start >= end:
            continue                          # Skip malformed span entirely
        for i in range(start, end):
            if i == start:
                char_tags[i] = f"B-{label}"
            else:
                char_tags[i] = f"I-{label}"

    # Simple whitespace tokenizer (wordpiece tokenizer used during actual training)
    for match in re.finditer(r'\S+', text):
        token = match.group()
        token_start = match.start()
        tokens.append(token)

        # Assign tag based on first character of token
        tag = char_tags[token_start]
        # Normalize: if it's I- but previous was O, upgrade to B-
        if tag.startswith("I-") and (not bio_tags or bio_tags[-1] == "O"):
            tag = "B-" + tag[2:]
        bio_tags.append(tag)

    return {"tokens": tokens, "tags": bio_tags, "text": text}


# ─────────────────────────────────────────────
# 5. INTERACTIVE ANNOTATION HELPER
#    For SMS types the template can't cover,
#    use this to annotate manually.
# ─────────────────────────────────────────────

def annotate_interactively(sms_text: str) -> Dict:
    """
    Walks you through annotating an SMS manually.
    Usage: paste in your SMS, then highlight entities.
    """
    entities = []
    labels = ["AMOUNT", "ACCOUNT", "RECEIVER_ACCOUNT", "BALANCE", "MERCHANT", "DATE"]
    print(f"\nSMS: {sms_text}\n")

    for label in labels:
        print(f"Enter the {label} substring (or press Enter to skip):")
        value = input("  > ").strip()
        if value and value in sms_text:
            start = sms_text.index(value)
            entities.append({"start": start, "end": start + len(value), "label": label})
            print(f"  ✓ Tagged '{value}' as {label}")

    return {"text": sms_text, "entities": entities}


# ─────────────────────────────────────────────
# 6. VALIDATION HELPERS
# ─────────────────────────────────────────────

def validate_dataset(dataset: List[Dict]) -> None:
    issues = 0
    for i, ex in enumerate(dataset):
        text = ex["text"]
        for ent in ex["entities"]:
            actual = text[ent["start"]:ent["end"]]
            # Check span makes sense
            if len(actual) < 1:
                print(f"Example {i}: empty span for {ent['label']}")
                issues += 1

    label_dist = {}
    for ex in dataset:
        for ent in ex["entities"]:
            label_dist[ent["label"]] = label_dist.get(ent["label"], 0) + 1

    print(f"\n✅ Validation complete. Issues: {issues}")
    print(f"📊 Label distribution: {label_dist}")
    print(f"📦 Total examples: {len(dataset)}")


# ─────────────────────────────────────────────
# MAIN
# ─────────────────────────────────────────────

if __name__ == "__main__":
    print("Generating augmented dataset...")
    dataset = generate_dataset(n=500)

    # Load and merge the manually annotated samples
    with open("sample_data.json") as f:
        manual = json.load(f)

    dataset = manual + dataset  # Manual samples first (higher quality)
    validate_dataset(dataset)

    # Convert to BIO token format
    bio_dataset = [char_to_bio(ex) for ex in dataset]

    # Save both formats
    with open("dataset_char_spans.json", "w") as f:
        json.dump(dataset, f, indent=2)

    with open("dataset_bio.json", "w") as f:
        json.dump(bio_dataset, f, indent=2)

    print(f"\n✅ Saved {len(dataset)} examples to:")
    print("   • dataset_char_spans.json (for BERT-style fine-tuning)")
    print("   • dataset_bio.json        (for BiLSTM training)")
