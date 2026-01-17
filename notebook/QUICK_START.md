# 🚀 Quick Start Guide - Dermatology Model Optimization

## 📋 Checklist Trước Khi Chạy

- [ ] Google Colab với GPU (T4/V100/A100)
- [ ] Google Drive đã mount
- [ ] Dataset ở đúng path: `/content/drive/MyDrive/ThangNQ-NamBH/fitzpatrick_dataset`
- [ ] Có file `metadata.csv` và folder `images/`

## ⚡ Chạy Nhanh (3 Bước)

### Bước 1: Upload File
```python
# Upload dermatology_16k_optimized.py lên Colab
```

### Bước 2: Kiểm Tra GPU
```python
!nvidia-smi
# Đảm bảo có GPU available
```

### Bước 3: Chạy
```python
!python dermatology_16k_optimized.py
```

## 🎛️ Điều Chỉnh Nhanh (Nếu Cần)

### Nếu Bị Out of Memory (OOM):
```python
# Trong file .py, tìm và sửa:
BATCH_SIZE = 16  # Giảm từ 24
IMG_SIZE = 320   # Giảm từ 384
```

### Nếu Muốn Training Nhanh Hơn:
```python
NUM_EPOCHS = 40  # Giảm từ 60
EARLY_STOP_PATIENCE = 10  # Giảm từ 15
```

### Nếu Muốn Accuracy Cao Hơn:
```python
NUM_EPOCHS = 80  # Tăng từ 60
IMG_SIZE = 448   # Tăng từ 384 (cần GPU mạnh)
MIXUP_ALPHA = 0.3  # Tăng từ 0.2
```

## 📊 Monitoring Training

### Xem Progress:
- **tqdm bars**: Hiển thị loss và learning rate realtime
- **Epoch summary**: Train/Val loss và accuracy
- **Best model**: Tự động save khi val_acc tăng

### Dấu Hiệu Tốt:
✅ Val accuracy tăng dần
✅ Train/Val loss giảm
✅ Gap giữa train/val không quá lớn (<5%)

### Dấu Hiệu Xấu:
❌ Val accuracy dao động mạnh → Giảm learning rate
❌ Train acc >> Val acc (>10%) → Overfitting, tăng augmentation
❌ Loss = NaN → Giảm learning rate hoặc batch size

## 🎯 Expected Timeline

| GPU Type | Time/Epoch | Total Time (60 epochs) |
|----------|------------|------------------------|
| A100 | 6-8 min | 6-8 hours |
| V100 | 10-12 min | 10-12 hours |
| T4 | 15-20 min | 15-20 hours |

## 📈 Expected Results

### Sau 20 Epochs:
- Train Acc: ~70-75%
- Val Acc: ~65-70%

### Sau 40 Epochs:
- Train Acc: ~80-85%
- Val Acc: ~75-80%

### Sau 60 Epochs (Best):
- Train Acc: ~85-90%
- Val Acc: ~82-87%
- Test Acc (with TTA): ~80-85%

## 🔧 Troubleshooting

### Problem: "CUDA out of memory"
**Solution:**
```python
BATCH_SIZE = 12  # Giảm xuống
IMG_SIZE = 256   # Giảm xuống
# Hoặc restart runtime và clear cache
```

### Problem: "Loss không giảm"
**Solution:**
```python
LEARNING_RATE = 0.0001  # Giảm LR
# Kiểm tra data augmentation có quá mạnh không
```

### Problem: "Overfitting (train >> val)"
**Solution:**
```python
MIXUP_ALPHA = 0.3  # Tăng mixup
LABEL_SMOOTHING = 0.15  # Tăng smoothing
# Tăng dropout trong model
```

### Problem: "Training quá chậm"
**Solution:**
```python
BATCH_SIZE = 32  # Tăng nếu GPU cho phép
IMG_SIZE = 256   # Giảm image size
# Giảm số augmentations trong TTA
```

## 💾 Output Files

Sau khi training xong, bạn sẽ có:

1. **best_optimized_model.pth** - Model tốt nhất (theo val_acc)
2. **skin_disease_optimized_final.pth** - Model cuối + metadata
3. **optimized_training_history.png** - Đồ thị loss/acc/lr
4. **optimized_confusion_matrix.png** - Confusion matrix

Tất cả được backup vào:
```
/content/drive/MyDrive/ThangNQ-NamBH/fitzpatrick_dataset/optimized_models/
```

## 🎓 Tips Để Đạt Accuracy Cao Nhất

1. **Chạy đủ epochs**: Đừng dừng sớm, để early stopping tự quyết định
2. **Monitor learning rate**: Nếu LR giảm quá nhanh, tăng T_0 trong scheduler
3. **Check confusion matrix**: Xem class nào bị nhầm nhiều, có thể cần thêm data
4. **Use TTA**: Test Time Augmentation tăng 1-3% accuracy
5. **Ensemble**: Nếu cần thêm, train nhiều models và average predictions

## 📞 Support

Nếu gặp vấn đề:
1. Check error message
2. Xem OPTIMIZATION_SUMMARY.md để hiểu từng technique
3. Điều chỉnh hyperparameters theo troubleshooting guide

## 🎉 Success Criteria

Model được coi là thành công khi:
- ✅ Val Accuracy > 82%
- ✅ Test Accuracy > 80%
- ✅ F1-Score > 0.78
- ✅ Không overfitting (train-val gap < 5%)

Good luck! 🚀
