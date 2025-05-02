package com.example.quanlychitieu.domain.adapter.spending;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R;
import com.example.quanlychitieu.domain.constants.SpendingType; // Đảm bảo import đúng
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors; // Import Collectors

public class SpendingAdapter extends RecyclerView.Adapter<SpendingAdapter.ViewHolder> {
    private static final String TAG = "SpendingAdapter";
    private List<TypeSpending> groupedSpendingList; // Danh sách các nhóm chi tiêu
    private OnSpendingClickListener onSpendingClickListener;
    private final NumberFormat numberFormat;
    // Các biến date format không còn cần thiết nếu chỉ hiển thị nhóm theo loại
    // private final SimpleDateFormat dateFormat;
    // private final Calendar calendar;
    // private final String[] dayOfWeek;

    public interface OnSpendingClickListener {
        void onTypeGroupClick(TypeSpending typeSpending);
    }

    // Constructor mặc định
    public SpendingAdapter() {
        this(new ArrayList<>()); // Gọi constructor chính với list rỗng
    }

    // Constructor nhận danh sách ban đầu
    public SpendingAdapter(List<Spending> initialSpendingList) {
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // Không cần khởi tạo các biến date format nữa
        // this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // this.calendar = Calendar.getInstance();
        // this.dayOfWeek = new String[]{"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        // Nhóm danh sách ban đầu (chỉ lấy chi tiêu)
        this.groupedSpendingList = groupSpendingsByType(initialSpendingList != null ? initialSpendingList : new ArrayList<>());
    }

    public void setOnSpendingClickListener(OnSpendingClickListener listener) {
        this.onSpendingClickListener = listener;
    }

    /**
     * Cập nhật dữ liệu adapter với danh sách spending mới.
     * Chỉ các khoản chi tiêu sẽ được nhóm và hiển thị.
     * @param spendingList Danh sách Spending mới (bao gồm cả thu và chi).
     */
    public void setItems(List<Spending> spendingList) {
        Log.d(TAG, "Setting items in adapter, list size: " + (spendingList != null ? spendingList.size() : 0));
        this.groupedSpendingList = groupSpendingsByType(spendingList != null ? spendingList : new ArrayList<>());
        Log.d(TAG, "Grouped list size: " + groupedSpendingList.size());
        notifyDataSetChanged();
    }

    /**
     * Nhóm các khoản CHI TIÊU theo loại.
     * @param spendingList Danh sách Spending (có thể lẫn thu và chi).
     * @return Danh sách các nhóm TypeSpending (chỉ chứa chi tiêu), sắp xếp theo ngày mới nhất.
     */
    private List<TypeSpending> groupSpendingsByType(List<Spending> spendingList) {
        if (spendingList == null || spendingList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, TypeSpending> groupMap = new LinkedHashMap<>();

        // Nhóm tất cả các khoản chi tiêu theo loại
        for (Spending spending : spendingList) {
            int type = spending.getType();
            String typeName = spending.getTypeName();
            if (typeName == null || typeName.isEmpty()) {
                typeName = getTypeNameDefault(type);
            }

            TypeSpending typeGroup = groupMap.get(type);
            if (typeGroup == null) {
                typeGroup = new TypeSpending(type, typeName);
                groupMap.put(type, typeGroup);
            }
            typeGroup.addSpending(spending);

            Log.d(TAG, "Grouped spending: type=" + type
                + ", typeName=" + typeName
                + ", money=" + spending.getMoney());
        }

        List<TypeSpending> sortedList = new ArrayList<>(groupMap.values());

        // Sắp xếp theo ngày mới nhất
        try {
            sortedList.sort((t1, t2) -> {
                Date latest1 = t1.getLatestSpendingDate();
                Date latest2 = t2.getLatestSpendingDate();
                return latest2.compareTo(latest1);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error sorting grouped spendings", e);
        }

        Log.d(TAG, "Grouping complete. Number of expense groups: " + sortedList.size());
        return sortedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TypeSpending typeSpending = groupedSpendingList.get(position);
        holder.bind(typeSpending);
    }

    @Override
    public int getItemCount() {
        int count = groupedSpendingList != null ? groupedSpendingList.size() : 0;
        Log.d(TAG, "getItemCount called: " + count);
        return count;
    }

    // --- ViewHolder ---
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageIcon;
        private final TextView textTypeName;
        private final TextView textAmount;
        private final ImageView imageArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.imageIcon);
            textTypeName = itemView.findViewById(R.id.textTypeName);
            textAmount = itemView.findViewById(R.id.textAmount);
            imageArrow = itemView.findViewById(R.id.imageArrow);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onSpendingClickListener != null) {
                    if (position >= 0 && position < groupedSpendingList.size()) {
                        TypeSpending typeSpending = groupedSpendingList.get(position);
                        onSpendingClickListener.onTypeGroupClick(typeSpending);
                    } else {
                        Log.e(TAG, "Invalid position clicked: " + position + ", list size: " + groupedSpendingList.size());
                    }
                }
            });
        }

        void bind(TypeSpending typeSpending) {
            if (typeSpending == null) return;

            imageIcon.setImageResource(getDefaultIconResource(typeSpending.getType()));
            textTypeName.setText(typeSpending.getTypeName());

            // *** SỬA LOGIC HIỂN THỊ TỔNG TIỀN ***
            // Vì giờ chỉ có chi tiêu, tổng luôn âm hoặc 0
            double totalAmount = typeSpending.getTotalAmount(); // totalAmount giờ chỉ là tổng chi tiêu (số âm)
            String formattedAmount = numberFormat.format(Math.abs(totalAmount)); // Lấy giá trị tuyệt đối
            Context context = itemView.getContext();

            // Luôn hiển thị màu đỏ và dấu trừ (trừ khi bằng 0)
            textAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));
            if (totalAmount == 0) {
                textAmount.setText(formattedAmount); // Hiển thị 0 không có dấu
            } else {
                textAmount.setText("-" + formattedAmount); // Luôn hiển thị dấu trừ
            }
            // *** KẾT THÚC SỬA ĐỔI ***

            if (imageArrow != null) {
                imageArrow.setImageResource(R.drawable.ic_arrow_forward);
            }
        }
    }

    // --- Lớp TypeSpending ---
    public static class TypeSpending {
        private final int type;
        private final String typeName;
        private final List<Spending> spendings;
        private double totalAmount;
        private Date latestDate;

        public TypeSpending(int type, String typeName) {
            this.type = type;
            this.typeName = typeName;
            this.spendings = new ArrayList<>();
            this.totalAmount = 0;
            this.latestDate = null;
        }

        public void addSpending(Spending spending) {
            if (spending == null) return;

            Log.d(TAG, "Adding spending: type=" + spending.getType()
                + ", typeName=" + spending.getTypeName()
                + ", money=" + spending.getMoney());

            spendings.add(spending);
            totalAmount += spending.getMoney(); // Cộng dồn số tiền (luôn là số âm vì là chi tiêu)

            if (spending.getDateTime() != null) {
                if (latestDate == null || spending.getDateTime().after(latestDate)) {
                    latestDate = spending.getDateTime();
                }
            }

            Log.d(TAG, "Updated totalAmount for type " + type + ": " + totalAmount);
        }

        // Getters giữ nguyên
        public int getType() { return type; }
        public String getTypeName() { return typeName; }
        public List<Spending> getSpendings() { return spendings; }
        public double getTotalAmount() { return totalAmount; }
        public Date getLatestSpendingDate() { return latestDate != null ? latestDate : new Date(0); }
    }

    // --- Hàm Helper của Adapter ---
    private int getDefaultIconResource(int type) {
        // ... (Giữ nguyên logic switch case trả về R.drawable.*) ...
        switch (type) {
            // Chi tiêu hàng tháng
            case SpendingType.EATING:           return R.drawable.ic_eat;
            case SpendingType.TRANSPORTATION:   return R.drawable.ic_taxi;
            case SpendingType.HOUSING:          return R.drawable.ic_house;
            case SpendingType.UTILITIES:        return R.drawable.ic_water;
            case SpendingType.PHONE:            return R.drawable.ic_phone;
            case SpendingType.ELECTRICITY:      return R.drawable.ic_electricity;
            case SpendingType.GAS:              return R.drawable.ic_gas;
            case SpendingType.TV:               return R.drawable.ic_tv;
            case SpendingType.INTERNET:         return R.drawable.ic_internet;

            // Chi tiêu cần thiết
            case SpendingType.FAMILY:           return R.drawable.ic_family;
            case SpendingType.HOME_REPAIR:      return R.drawable.ic_house_2;
            case SpendingType.VEHICLE:          return R.drawable.ic_tools;
            case SpendingType.HEALTHCARE:       return R.drawable.ic_doctor;
            case SpendingType.INSURANCE:        return R.drawable.ic_health_insurance;
            case SpendingType.EDUCATION:        return R.drawable.ic_education;
            case SpendingType.HOUSEWARES:       return R.drawable.ic_armchair;
            case SpendingType.PERSONAL:         return R.drawable.ic_toothbrush;
            case SpendingType.PET:              return R.drawable.ic_pet;

            // Giải trí
            case SpendingType.SPORTS:           return R.drawable.ic_sports;
            case SpendingType.BEAUTY:           return R.drawable.ic_diamond;
            case SpendingType.GIFTS:            return R.drawable.ic_give_love;
            case SpendingType.ENTERTAINMENT:    return R.drawable.ic_game_pad;
            case SpendingType.SHOPPING:      return R.drawable.ic_shopping;

            // Khác
            case SpendingType.OTHER:            return R.drawable.ic_box;

            // Trường hợp mặc định
            default:                            return R.drawable.ic_question_mark; // Default icon                           return R.drawable.ic_other;
        }
    }

    // Hàm này chỉ dùng để lấy tên mặc định nếu spending.getTypeName() bị null/rỗng
    private String getTypeNameDefault(int type) {
        // ... (Giữ nguyên logic switch case trả về tên mặc định) ...
        switch (type) {
            case SpendingType.EATING:           return "Ăn uống";
            case SpendingType.TRANSPORTATION:   return "Di chuyển";
            case SpendingType.HOUSING:          return "Thuê nhà";
            case SpendingType.UTILITIES:        return "Tiền nước";
            case SpendingType.PHONE:            return "Tiền điện thoại";
            case SpendingType.ELECTRICITY:      return "Tiền điện";
            case SpendingType.GAS:              return "Tiền gas";
            case SpendingType.TV:               return "Tiền TV";
            case SpendingType.INTERNET:         return "Tiền internet";
            case SpendingType.FAMILY:           return "Dịch vụ gia đình";
            case SpendingType.HOME_REPAIR:      return "Sửa chữa nhà cửa";
            case SpendingType.VEHICLE:          return "Vé xe";
            case SpendingType.HEALTHCARE:       return "Khám chữa bệnh";
            case SpendingType.INSURANCE:        return "Bảo hiểm";
            case SpendingType.EDUCATION:        return "Học tập";
            case SpendingType.HOUSEWARES:       return "Mua sắm";
            case SpendingType.PERSONAL:         return "Sinh hoạt cá nhân";
            case SpendingType.PET:              return "Thú cưng";

            case SpendingType.SPORTS:           return "Thể thao";
            case SpendingType.BEAUTY:           return "Chăm sóc sắc đẹp";
            case SpendingType.GIFTS:            return "Quà tặng";
            case SpendingType.ENTERTAINMENT:    return "Giải trí";
            case SpendingType.SHOPPING:      return "Mua sắm";
            case SpendingType.OTHER:            return "Chi tiêu khác";


            // ... các case khác ...
            default:                            return "Chi tiêu khác";
        }
    }
}
