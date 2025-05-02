package com.example.quanlychitieu.data.datasource.firebase.firestore;

/**
 * Contains all Firestore collection and field names used in the application.
 * Centralizing these values helps maintain consistency and makes future updates
 * easier.
 */
public class FirestoreConstants {
  // Collections
  public static final String COLLECTION_USERS = "users";
  public static final String COLLECTION_SPENDINGS = "spending"; // Tên collection chi tiêu
  public static final String COLLECTION_BUDGETS = "budgets";
  public static final String COLLECTION_FRIENDS = "friends";
  public static final String COLLECTION_NOTIFICATIONS = "notifications";
  public static final String COLLECTION_DATA = "data"; // Collection chứa ID theo tháng (nếu vẫn dùng)
  public static final String COLLECTION_WALLET = "wallet";
  public static final String COLLECTION_INFO = "info";

  // User fields
  public static final String FIELD_USER_ID = "userId"; // Thường dùng trong các collection khác để liên kết
  public static final String FIELD_USER_NAME = "name";
  public static final String FIELD_USER_EMAIL = "email";
  public static final String FIELD_USER_AVATAR = "avatar";
  public static final String FIELD_USER_BIRTHDAY = "birthday";
  public static final String FIELD_USER_GENDER = "gender";
  public static final String FIELD_USER_MONEY = "money"; // Có thể là tổng tiền trong ví?

  // Spending fields
  // public static final String FIELD_SPENDING_ID = "id"; // Không cần nếu dùng @DocumentId
  public static final String FIELD_SPENDING_USER_ID = "userId"; // ID của người tạo chi tiêu
  public static final String FIELD_SPENDING_MONEY = "money";
  public static final String FIELD_SPENDING_TYPE = "type";
  public static final String FIELD_SPENDING_TYPE_NAME = "typeName";
  public static final String FIELD_SPENDING_NOTE = "note";
  public static final String FIELD_SPENDING_DATE_TIME = "dateTime"; // Tên trường ngày giờ
  public static final String FIELD_SPENDING_IMAGE = "image"; // *** ĐÃ THÊM LẠI ***
  public static final String FIELD_SPENDING_LOCATION = "location";
  public static final String FIELD_SPENDING_FRIENDS = "friends";
  public static final String FIELD_MONTH_KEY = "monthKey";

  // Budget fields
  // public static final String FIELD_BUDGET_ID = "id"; // Không cần nếu dùng @DocumentId
  public static final String FIELD_BUDGET_USER_ID = "userId";
  public static final String FIELD_BUDGET_AMOUNT = "amount";
  public static final String FIELD_BUDGET_START_DATE = "startDate";
  public static final String FIELD_BUDGET_END_DATE = "endDate";
  // Thêm các trường khác cho budget nếu cần (ví dụ: category, currentSpending)

  // Common fields
  public static final String FIELD_CREATED_AT = "createdAt"; // Thời gian tạo
  public static final String FIELD_UPDATED_AT = "updatedAt"; // Thời gian cập nhật cuối
  public static final String FIELD_DELETED_AT = "deletedAt"; // Trường cho soft delete
  public static final String FIELD_IS_DELETED = "isDeleted";

  // Storage paths (Ví dụ)
  public static final String STORAGE_AVATAR_PATH = "avatars/";
  public static final String STORAGE_SPENDING_IMAGES_PATH = "spending_images/"; // Có thể không dùng nếu dùng Cloudinary

  // Query parameters (Ví dụ, có thể không cần nếu query bằng Date/Timestamp)
  public static final String PARAM_YEAR = "year";
  public static final String PARAM_MONTH = "month";
  public static final String PARAM_DAY = "day";

  // Maximum query limits (Ví dụ)
  public static final int MAX_QUERY_LIMIT = 100;

  // Private constructor to prevent instantiation
  private FirestoreConstants() {}
}
