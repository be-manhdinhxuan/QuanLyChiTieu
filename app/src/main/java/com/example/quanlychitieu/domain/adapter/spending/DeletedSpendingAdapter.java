package com.example.quanlychitieu.domain.adapter.spending; // Đặt đúng package của bạn

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlychitieu.R; // Đảm bảo import R đúng
import com.example.quanlychitieu.domain.constants.SpendingType; // Import nếu dùng enum/constants
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DeletedSpendingAdapter extends RecyclerView.Adapter<DeletedSpendingAdapter.DeletedViewHolder> {

    private List<Spending> deletedSpendings;
    // Sử dụng Set để lưu ID của các item được chọn, hiệu quả cho việc kiểm tra và thêm/xóa
    private Set<String> selectedSpendingIds = new HashSet<>();
    private Context context;
    private NumberFormat numberFormat;
    private SimpleDateFormat dateFormat; // Format cho tvDate

    // Interfaces cho callbacks về Activity
    private OnItemCheckChangedListener checkChangedListener;
    private OnItemClickListener itemClickListener; // Listener cho việc click vào item (nếu cần)

    // Interface để Activity lắng nghe sự kiện thay đổi checkbox
    public interface OnItemCheckChangedListener {
        void onItemCheckChanged(Spending spending, boolean isChecked, int selectedCount);
    }

    // Interface để Activity lắng nghe sự kiện click vào item (toàn bộ row hoặc mũi tên cũ)
    public interface OnItemClickListener {
        void onItemClick(Spending spending);
    }

    // Constructor
    public DeletedSpendingAdapter(Context context, List<Spending> initialList, OnItemCheckChangedListener listener) {
        this.context = context;
        // Tạo bản sao để tránh tham chiếu trực tiếp đến list gốc từ Activity
        this.deletedSpendings = new ArrayList<>(initialList);
        this.checkChangedListener = listener;
        // Khởi tạo định dạng tiền và ngày
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // Chọn định dạng ngày bạn muốn hiển thị (ví dụ: dd/MM/yyyy HH:mm)
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    // Setter cho item click listener (nếu bạn cần)
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public DeletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkbox_history, parent, false);
        return new DeletedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeletedViewHolder holder, int position) {
        Spending currentSpending = deletedSpendings.get(position);
        if (currentSpending == null) return;

        // 1. Bind dữ liệu vào Views
        // *** LUÔN HIỂN THỊ NGÀY GIỜ GỐC (dateTime) ***
        if (currentSpending.getDateTime() != null) {
            // Sử dụng dateFormat đã định nghĩa (ví dụ: "dd/MM/yyyy HH:mm")
            holder.tvDate.setText(dateFormat.format(currentSpending.getDateTime()));
        } else {
            holder.tvDate.setText(context.getString(R.string.date_not_available)); // Hoặc một chuỗi báo lỗi khác
        }

        // Icon
        holder.ivIcon.setImageResource(getSpendingTypeIconResId(currentSpending.getType()));

        // Tên loại
        holder.tvCode.setText(currentSpending.getTypeName() != null ? currentSpending.getTypeName() : context.getString(R.string.other));

        // Số tiền (Hiển thị giá trị tuyệt đối hoặc có dấu âm tùy ý)
        holder.tvAmount.setText(numberFormat.format(currentSpending.getMoney())); // Giữ dấu âm
        // Đặt màu chữ dựa trên loại tiền (giả sử chỉ có chi tiêu ở đây)
        holder.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.expense_red));


        // 2. Xử lý Checkbox
        // Bỏ listener cũ trước khi đặt trạng thái và listener mới để tránh trigger không mong muốn
        holder.checkbox.setOnCheckedChangeListener(null);

        // Đặt trạng thái checked dựa trên Set selectedSpendingIds
        holder.checkbox.setChecked(selectedSpendingIds.contains(currentSpending.getId()));

        // Đặt listener mới
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String id = currentSpending.getId();
            if (id != null) {
                if (isChecked) {
                    selectedSpendingIds.add(id);
                } else {
                    selectedSpendingIds.remove(id);
                }
                // Thông báo cho Activity biết trạng thái thay đổi và số lượng item đang chọn
                if (checkChangedListener != null) {
                    checkChangedListener.onItemCheckChanged(currentSpending, isChecked, selectedSpendingIds.size());
                }
            }
        });

        // 3. Xử lý Item Click (Nếu cần)
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(currentSpending);
            }
            // Hoặc bạn có thể làm cho việc click vào item cũng thay đổi trạng thái checkbox
            // holder.checkbox.toggle();
        });
    }

    @Override
    public int getItemCount() {
        return deletedSpendings.size();
    }

    // --- Các phương thức tiện ích ---

    // Cập nhật dữ liệu cho Adapter
    public void updateData(List<Spending> newList) {
        this.deletedSpendings.clear();
        if (newList != null) {
            this.deletedSpendings.addAll(newList);
        }
        // Khi cập nhật dữ liệu mới, reset trạng thái chọn
        clearSelectionInternal();
        notifyDataSetChanged();
    }

    // Lấy danh sách các ID đang được chọn
    public Set<String> getSelectedSpendingIds() {
        return new HashSet<>(selectedSpendingIds); // Trả về bản sao để tránh sửa đổi từ bên ngoài
    }

    // Xóa trạng thái chọn của tất cả item
    public void clearSelection() {
        clearSelectionInternal();
        notifyDataSetChanged(); // Cập nhật RecyclerView để bỏ check
    }

    // Hàm nội bộ để xóa selection mà không trigger notifyDataSetChanged ngay lập tức
    private void clearSelectionInternal() {
        selectedSpendingIds.clear();
    }

    // Chọn tất cả item
    public void selectAll() {
        clearSelectionInternal(); // Xóa cái cũ trước
        for(Spending s : deletedSpendings) {
            if(s.getId() != null) {
                selectedSpendingIds.add(s.getId());
            }
        }
        notifyDataSetChanged(); // Cập nhật RecyclerView để check tất cả
        // Thông báo cho activity về số lượng mới
        if(checkChangedListener != null && !deletedSpendings.isEmpty()){
            // Gọi callback với item cuối cùng và trạng thái true, cùng số lượng tổng
            checkChangedListener.onItemCheckChanged(deletedSpendings.get(deletedSpendings.size()-1), true, selectedSpendingIds.size());
        }
    }


    // --- ViewHolder ---
    public static class DeletedViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvCode, tvAmount;
        ImageView ivIcon;
        CheckBox checkbox;

        public DeletedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }

    // --- Helper lấy Icon ---
    // (Bạn có thể copy hàm này từ AnalyticFragment hoặc tạo class Util riêng)
    private int getSpendingTypeIconResId(int type) {
        switch (type) {
            case SpendingType.EATING: return R.drawable.ic_eat;
            case SpendingType.TRANSPORTATION: return R.drawable.ic_taxi;
            case SpendingType.HOUSING: return R.drawable.ic_house;
            case SpendingType.UTILITIES: return R.drawable.ic_water;
            case SpendingType.PHONE: return R.drawable.ic_phone;
            case SpendingType.ELECTRICITY: return R.drawable.ic_electricity;
            case SpendingType.GAS: return R.drawable.ic_gas;
            case SpendingType.TV: return R.drawable.ic_tv;
            case SpendingType.INTERNET: return R.drawable.ic_internet;
            case SpendingType.FAMILY: return R.drawable.ic_family;
            case SpendingType.HOME_REPAIR: return R.drawable.ic_house_2;
            case SpendingType.VEHICLE: return R.drawable.ic_tools;
            case SpendingType.HEALTHCARE: return R.drawable.ic_doctor;
            case SpendingType.INSURANCE: return R.drawable.ic_health_insurance;
            case SpendingType.EDUCATION: return R.drawable.ic_education;
            case SpendingType.HOUSEWARES: return R.drawable.ic_armchair;
            case SpendingType.PERSONAL: return R.drawable.ic_toothbrush;
            case SpendingType.PET: return R.drawable.ic_pet;
            case SpendingType.SPORTS: return R.drawable.ic_sports;
            case SpendingType.BEAUTY: return R.drawable.ic_diamond;
            case SpendingType.GIFTS: return R.drawable.ic_give_love;
            case SpendingType.ENTERTAINMENT: return R.drawable.ic_game_pad;
            case SpendingType.OTHER:
            default:
                return R.drawable.ic_box; // Icon mặc định
        }
    }
}