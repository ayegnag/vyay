# Local Training on AMD GPU (ROCm)
## Complete Setup Guide

AMD GPUs use ROCm instead of CUDA. PyTorch has first-class ROCm support —
the training script requires zero changes. You just install the right PyTorch build.

---

## Step 1: Check if Your GPU is Supported

ROCm supports most AMD GPUs from the last 4 years, but not all.

**Confirmed supported (training-viable):**
| Series | Cards |
|--------|-------|
| RX 7000 | 7600, 7700 XT, 7800 XT, 7900 XT, 7900 XTX |
| RX 6000 | 6600, 6600 XT, 6700 XT, 6800, 6800 XT, 6900 XT |
| RX 5000 | 5700, 5700 XT (ROCm 5.x only, not 6.x) |
| Radeon Pro | W6800, W7900 |

**Not supported:** RX 580, 590, Vega 56/64 (too old for ROCm 6.x).

Check your GPU: `rocminfo | grep "Marketing Name"` (after ROCm install)

---

## Step 2: Install ROCm

### Ubuntu 22.04 / 24.04 (recommended)
```bash
# Add AMD ROCm repo
sudo apt update
wget https://repo.radeon.com/amdgpu-install/6.1.3/ubuntu/jammy/amdgpu-install_6.1.60103-1_all.deb
sudo apt install ./amdgpu-install_6.1.60103-1_all.deb
sudo amdgpu-install --usecase=rocm

# Add yourself to render and video groups
sudo usermod -aG render,video $USER

# Reboot
sudo reboot
```

### Verify ROCm works
```bash
rocminfo           # Should list your GPU with its GFX version (e.g. gfx1100 for RX 7000)
rocm-smi           # GPU utilization monitor (like nvidia-smi)
```

---

## Step 3: Install PyTorch with ROCm

**Do NOT install the default `pip install torch` — that's CUDA only.**

```bash
# For ROCm 6.1 (matches ROCm install above)
pip install torch torchvision torchaudio \
    --index-url https://download.pytorch.org/whl/rocm6.1

# Verify GPU is visible to PyTorch
python3 -c "import torch; print(torch.cuda.is_available()); print(torch.cuda.get_device_name(0))"
# Should print: True  followed by your GPU name
```

PyTorch maps ROCm through the CUDA API — `torch.cuda.is_available()` returns `True`
on ROCm. The training script works without modification.

---

## Step 4: Install Training Dependencies

```bash
pip install transformers datasets scikit-learn seqeval "optimum[exporters]"
```

---

## Step 5: Handle the GFX Version Override (if needed)

Some GPUs (particularly RX 6000 series on newer ROCm) need a hint to use
the right kernel. If your training crashes immediately with a HIP error:

```bash
# RX 6700 XT, 6800, 6800 XT, 6900 XT
export HSA_OVERRIDE_GFX_VERSION=10.3.0

# RX 6600, 6600 XT
export HSA_OVERRIDE_GFX_VERSION=10.3.2

# RX 7900 XTX, 7900 XT
export HSA_OVERRIDE_GFX_VERSION=11.0.0

# Then run training
python train_sms_ner.py --action all --data dataset_char_spans.json
```

Add the relevant line to your `~/.bashrc` so you don't need to set it every time:
```bash
echo 'export HSA_OVERRIDE_GFX_VERSION=10.3.0' >> ~/.bashrc
```

---

## Step 6: Run Training

```bash
cd training/
python data_toolkit.py                    # Generate dataset (if not done already)
python train_sms_ner.py --action all      # Train + test + export TF Lite
```

Expected output with GPU active:
```
GPU detected: AMD Radeon RX 7800 XT
Using fp16 mixed precision
Epoch 1/20: loss=1.42  val_f1=0.61
Epoch 2/20: loss=0.89  val_f1=0.74
...
Epoch 9/20: loss=0.18  val_f1=0.91   ← Early stopping triggers ~here
✅ Saved model to ./model_output
Exporting to TF Lite with INT8 quantization...
✅ TF Lite model saved to ./sms_ner_model.tflite
```

Approximate training time by GPU:
| GPU | Time |
|-----|------|
| RX 7900 XTX | ~6 min |
| RX 7800 XT / 6800 XT | ~10 min |
| RX 7600 / 6600 XT | ~18 min |

---

## Monitoring GPU During Training

```bash
# In a separate terminal — like nvidia-smi but for AMD
watch -n 1 rocm-smi

# Or for a live graph
rocm-smi --showuse --showmemuse
```

---

## Troubleshooting

**`torch.cuda.is_available()` returns False after ROCm install**
```bash
# Check ROCm is on the library path
echo $LD_LIBRARY_PATH | grep rocm
# If missing:
export LD_LIBRARY_PATH=/opt/rocm/lib:$LD_LIBRARY_PATH
```

**`HIPErrorNoBinaryForGpu` crash on first training step**
→ Set `HSA_OVERRIDE_GFX_VERSION` as described in Step 5.

**Out of memory with BATCH_SIZE=16**
→ Reduce batch size in `train_sms_ner.py`: `BATCH_SIZE = 8`
→ SMS NER is a small task; batch size of 8 trains fine.

**TF Lite export fails (`optimum` errors)**
→ The export step runs on CPU and doesn't need ROCm.
→ If optimum fails: `pip install --upgrade optimum[exporters]`

---

## Windows Note

ROCm on Windows is experimental and only supports a limited set of GPUs
(RX 7000 series only, via HIP SDK). If you're on Windows, the easiest path
is WSL2 with Ubuntu 22.04 — ROCm works well in WSL2.
Check: https://rocm.docs.amd.com/en/latest/deploy/windows/index.html
