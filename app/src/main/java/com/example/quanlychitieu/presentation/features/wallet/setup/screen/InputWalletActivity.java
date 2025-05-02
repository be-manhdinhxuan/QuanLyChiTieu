package com.example.quanlychitieu.presentation.features.wallet.setup.screen;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlychitieu.databinding.ActivityInputWalletBinding;
import com.example.quanlychitieu.domain.service.SpendingFirebase;
import com.example.quanlychitieu.presentation.features.main.navigation.screen.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class InputWalletActivity extends AppCompatActivity {
  private ActivityInputWalletBinding binding;
  private NumberFormat numberFormat;
  private SpendingFirebase spendingFirebase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityInputWalletBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    spendingFirebase = SpendingFirebase.getInstance();

    setupBackPressHandling();
    setupViews();
  }

  private void setupBackPressHandling() {
    // Xử lý nút back theo phương pháp mới được khuyến nghị
    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        handleBackPress();
      }
    });
  }

  private void setupViews() {
    // Xử lý sự kiện nút Back trên toolbar
    binding.buttonBack.setOnClickListener(v -> handleBackPress());

    // Xử lý định dạng tiền tệ khi nhập
    binding.editMoney.addTextChangedListener(new TextWatcher() {
      private String current = ""; // Lưu chuỗi hiện tại để tránh vòng lặp
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        String userInput = s.toString();
        // Nếu chuỗi mới giống chuỗi đã định dạng trước đó -> không làm gì cả (tránh vòng lặp)
        if (userInput.equals(current)) {
          return;
        }

        // Lấy vị trí con trỏ hiện tại
        int initialCursorPos = binding.editMoney.getSelectionStart();
        // Đếm số lượng dấu phân cách trước con trỏ ban đầu
        int initialSeparatorsBeforeCursor = 0;
        for (int i = 0; i < initialCursorPos; i++) {
          if (userInput.length() > i && (userInput.charAt(i) == '.' || userInput.charAt(i) == ',')) {
            initialSeparatorsBeforeCursor++;
          }
        }

        binding.editMoney.removeTextChangedListener(this);

        try {
          // 1. Xóa tất cả ký tự không phải số
          String cleanString = userInput.replaceAll("[^0-9]", "");

          if (!cleanString.isEmpty()) {
            double parsed = Double.parseDouble(cleanString);
            String formatted = numberFormat.format(parsed);
            // Bỏ ký hiệu tiền tệ và khoảng trắng thừa (nếu có)
            // Lưu ý: numberFormat của Việt Nam thường không có ký hiệu tiền tệ kèm theo
            // nên việc replace có thể không cần thiết hoặc cần điều chỉnh
            formatted = formatted.replace(numberFormat.getCurrency().getSymbol(), "").trim();

            current = formatted; // Cập nhật chuỗi hiện tại
            binding.editMoney.setText(formatted);

            // --- Tính toán vị trí con trỏ mới ---
            int newCursorPos = initialCursorPos;
            // Đếm số lượng dấu phân cách trước con trỏ trong chuỗi mới
            int newSeparatorsBeforeCursor = 0;
            for (int i = 0; i < formatted.length() && i < newCursorPos ; i++) {
              if (formatted.charAt(i) == '.' || formatted.charAt(i) == ',') {
                newSeparatorsBeforeCursor++;
              }
            }
            // Điều chỉnh vị trí con trỏ dựa trên sự thay đổi số lượng dấu phân cách
            newCursorPos += (newSeparatorsBeforeCursor - initialSeparatorsBeforeCursor);

            // Đảm bảo con trỏ không vượt quá độ dài chuỗi mới
            newCursorPos = Math.min(newCursorPos, formatted.length());
            // Đảm bảo con trỏ không âm
            newCursorPos = Math.max(0, newCursorPos);

            binding.editMoney.setSelection(newCursorPos);


          } else {
            // Nếu cleanString rỗng -> xóa trắng EditText
            current = "";
            binding.editMoney.setText("");
          }

        } catch (NumberFormatException nfe) {
          Log.e(TAG, "NumberFormatException parsing money input", nfe);
          current = "";
          binding.editMoney.setText("");
        } catch (Exception e) {
          Log.e(TAG, "Exception formatting money input", e);
          current = "";
          binding.editMoney.setText("");
        }

        binding.editMoney.addTextChangedListener(this);
      }
    });

    // Xử lý nút Confirm
    binding.buttonOk.setOnClickListener(v -> {
      try {
        String moneyText = binding.editMoney.getText().toString().replaceAll("[^0-9]", "");
        if (!moneyText.isEmpty()) {
          long money = Long.parseLong(moneyText);
          if (money > 0) {
            saveWalletData(money);
          } else {
            Toast.makeText(InputWalletActivity.this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
          }
        } else {
          Toast.makeText(InputWalletActivity.this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
        }
      } catch (NumberFormatException e) {
        Toast.makeText(InputWalletActivity.this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void saveWalletData(long money) {
    binding.buttonOk.setEnabled(false);
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // Cập nhật money trong document của user
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .update("money", money)
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(InputWalletActivity.this, "Đã lưu số tiền thành công", Toast.LENGTH_SHORT).show();
            navigateToHome();
        })
        .addOnFailureListener(e -> {
            binding.buttonOk.setEnabled(true);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("permission_denied")) {
                Toast.makeText(InputWalletActivity.this, 
                    "Không có quyền truy cập. Vui lòng đăng nhập lại.", 
                    Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InputWalletActivity.this, 
                    "Lỗi: " + errorMessage, 
                    Toast.LENGTH_SHORT).show();
            }
        });
  }

  private void navigateToHome() {
    Intent intent = new Intent(this, MainActivity.class);
    // Xóa tất cả activity trong stack để không thể back lại
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }

  private void handleBackPress() {
    // Kiểm tra xem đã nhập dữ liệu gì chưa
    if (!binding.editMoney.getText().toString().isEmpty()) {
      // Nếu đã nhập dữ liệu, hiển thị thông báo xác nhận
      showExitConfirmDialog();
    } else {
      // Nếu chưa nhập gì, thoát luôn
      finish();
    }
  }

  /**
   * Hiển thị hộp thoại xác nhận khi người dùng muốn thoát mà chưa lưu dữ liệu
   */
  private void showExitConfirmDialog() {
    new AlertDialog.Builder(this)
        .setTitle("Xác nhận thoát")
        .setMessage("Dữ liệu chưa được lưu. Bạn có muốn thoát?")
        .setPositiveButton("Có", (dialog, which) -> {
          finish();
        })
        .setNegativeButton("Không", (dialog, which) -> {
          dialog.dismiss();
        })
        .show();
  }
}