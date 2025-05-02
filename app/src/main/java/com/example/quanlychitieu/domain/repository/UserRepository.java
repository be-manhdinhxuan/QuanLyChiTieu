package com.example.quanlychitieu.domain.repository;

import android.net.Uri;
import com.example.quanlychitieu.domain.model.user.User; // Đảm bảo import đúng model User
import com.google.android.gms.tasks.Task;

public interface UserRepository {

    // Giữ lại getUserById vì nó rõ ràng hơn getUser
    // Task<User> getUser(String userId); // Có thể bỏ nếu getUserById đủ dùng

    /**
     * Lấy thông tin người dùng đang đăng nhập hiện tại.
     * @return Task chứa User object hoặc null/exception nếu lỗi.
     */
    Task<User> getCurrentUser();

    /**
     * Tạo một bản ghi người dùng mới trong nguồn dữ liệu (ví dụ: Firestore).
     * Quan trọng: Phương thức này phải trả về User object đã được tạo thành công
     * (bao gồm cả ID mới được sinh ra bởi Firestore).
     * @param user Đối tượng User chứa thông tin cần tạo (ID có thể null hoặc rỗng).
     * @return Task chứa User object đã được tạo thành công với ID hợp lệ,
     * hoặc null/exception nếu lỗi.
     */
    Task<User> createUser(User user); // <-- THAY ĐỔI KIỂU TRẢ VỀ TỪ Task<Void> SANG Task<User>

    /**
     * Cập nhật thông tin người dùng và ảnh đại diện (nếu có).
     * @param user Đối tượng User chứa thông tin cập nhật (phải có ID).
     * @param imageUri Uri của ảnh đại diện mới (null nếu không thay đổi ảnh).
     * @return Task<Void> báo hiệu hoàn thành hoặc lỗi.
     */
    Task<Void> updateUser(User user, Uri imageUri);

    /**
     * Xóa người dùng hiện tại (cần xử lý cẩn thận).
     * @return Task<Void> báo hiệu hoàn thành hoặc lỗi.
     */
    Task<Void> deleteUser(); // Xem xét lại tính cần thiết và bảo mật của hàm này

    /**
     * Cập nhật số tiền của người dùng hiện tại.
     * @param money Số tiền mới.
     * @return Task<Void> báo hiệu hoàn thành hoặc lỗi.
     */
    Task<Void> updateUserMoney(int money);

    /**
     * Kiểm tra xem một email đã tồn tại trong hệ thống chưa.
     * @param email Email cần kiểm tra.
     * @return Task chứa giá trị Boolean (true nếu tồn tại, false nếu không).
     */
    Task<Boolean> isEmailExists(String email);

    /**
     * Lấy thông tin người dùng dựa trên ID.
     * @param userId ID của người dùng cần lấy.
     * @return Task chứa User object hoặc null/exception nếu không tìm thấy hoặc lỗi.
     */
    Task<User> getUserById(String userId);

    // Có thể thêm các phương thức khác nếu cần
    // Ví dụ: Task<List<User>> searchUsersByName(String nameQuery);
}