package com.example.quanlychitieu.presentation.features.wallet.currency.screen;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.databinding.ActivityCurrencyExchangeRateBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrencyExchangeRateActivity extends AppCompatActivity {
  private ActivityCurrencyExchangeRateBinding binding;
  private CurrencyExchangeAdapter adapter;
  private List<Map<String, Object>> allCurrencies = new ArrayList<>();
  private List<Map<String, Object>> filteredCurrencies = new ArrayList<>();
  private String selectedCurrency = "VND";
  private NumberFormat currencyFormat;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityCurrencyExchangeRateBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    currencyFormat.setMaximumFractionDigits(2);

    setupToolbar();
    initializeCurrencies();
    setupSpinner();
    setupRecyclerView();
    setupSearch();
    setupAmountInput();
  }

  private void setupToolbar() {
    // Sử dụng nút back trong layout mới
    binding.buttonBack.setOnClickListener(v -> finish());
  }

  private void initializeCurrencies() {
    // Khởi tạo danh sách các loại tiền tệ
    addCurrency("VND", "₫", "Việt Nam", 1.0, R.drawable.ic_flag_vietnam);
    addCurrency("USD", "$", "Hoa Kỳ", 0.000043, R.drawable.ic_flag_us);

  }

  private void addCurrency(String code, String symbol, String countryName, double rate, int flagResourceId) {
    Map<String, Object> currency = new HashMap<>();
    currency.put("code", code);
    currency.put("symbol", symbol);
    currency.put("countryName", countryName);
    currency.put("rate", rate);
    currency.put("amount", 0.0);
    currency.put("flagResourceId", flagResourceId);
    allCurrencies.add(currency);
  }

  private void setupSpinner() {
    List<String> currencyCodes = allCurrencies.stream()
        .map(currency -> (String) currency.get("code"))
        .collect(Collectors.toList());

    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_item, currencyCodes);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.spinnerCurrency.setAdapter(spinnerAdapter);

    binding.spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedCurrency = currencyCodes.get(position);
        updateCurrencies();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void setupRecyclerView() {
    adapter = new CurrencyExchangeAdapter(filteredCurrencies);
    binding.rvCurrencies.setLayoutManager(new LinearLayoutManager(this));
    binding.rvCurrencies.setAdapter(adapter);
    updateCurrencies();
  }

  private void setupSearch() {
    binding.etSearch.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        filterCurrencies();
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
  }

  private void setupAmountInput() {
    binding.etAmount.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateCurrencies();
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
  }

  private void filterCurrencies() {
    String searchText = binding.etSearch.getText().toString().toLowerCase();
    filteredCurrencies = allCurrencies.stream()
        .filter(currency -> {
          String code = (String) currency.get("code");
          String countryName = (String) currency.get("countryName");
          return code.toLowerCase().contains(searchText) ||
              countryName.toLowerCase().contains(searchText);
        })
        .collect(Collectors.toList());
    adapter.updateList(filteredCurrencies);
  }

  private void updateCurrencies() {
    String amountText = binding.etAmount.getText().toString();
    if (amountText.isEmpty()) {
      amountText = "0";
    }
    double amount = Double.parseDouble(amountText);

    // Tìm tỉ giá của đồng tiền được chọn
    double selectedRate = 1.0;
    for (Map<String, Object> currency : allCurrencies) {
      if (currency.get("code").equals(selectedCurrency)) {
        selectedRate = (double) currency.get("rate");
        break;
      }
    }

    // Cập nhật số tiền cho các loại tiền tệ
    for (Map<String, Object> currency : allCurrencies) {
      double rate = (double) currency.get("rate");
      // Công thức: amount * (rate / selectedRate)
      double convertedAmount = amount * (rate / selectedRate);
      currency.put("amount", convertedAmount);
    }

    filterCurrencies();
  }

  // Adapter cho RecyclerView
  private class CurrencyExchangeAdapter extends RecyclerView.Adapter<CurrencyExchangeAdapter.ViewHolder> {
    private List<Map<String, Object>> currencies;

    public CurrencyExchangeAdapter(List<Map<String, Object>> currencies) {
      this.currencies = currencies;
    }

    public void updateList(List<Map<String, Object>> newCurrencies) {
      this.currencies = newCurrencies;
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_currency, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      Map<String, Object> currency = currencies.get(position);
      holder.bind(currency);
    }

    @Override
    public int getItemCount() {
      return currencies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
      private TextView tvSymbol;
      private TextView tvCode;
      private TextView tvAmount;
      private TextView tvRate;
      private ImageView ivFlag;

      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        tvCode = itemView.findViewById(R.id.tvCode);
        tvAmount = itemView.findViewById(R.id.tvAmount);

      }

      public void bind(Map<String, Object> currency) {
        String code = (String) currency.get("code");
        String symbol = (String) currency.get("symbol");
        String countryName = (String) currency.get("countryName");
        double amount = (double) currency.get("amount");
        double rate = (double) currency.get("rate");
        int flagResourceId = (int) currency.get("flagResourceId");

        tvSymbol.setText(symbol);
        tvCode.setText(code);
        tvAmount.setText(currencyFormat.format(amount));

        // Hiển thị tỉ giá so với VNĐ
        String rateText = "1 " + code + " = " + currencyFormat.format(1 / rate) + " VND";
        if (code.equals("VND")) {
          rateText = countryName;
        }
        tvRate.setText(rateText);

        ivFlag.setImageResource(flagResourceId);
      }
    }
  }
}