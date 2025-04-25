package com.example.quanlychitieu.data.datasource.firebase.firestore;

/**
 * Contains all Firestore collection and field names used in the application.
 * Centralizing these values helps maintain consistency and makes future updates
 * easier.
 */
public class FirestoreConstants {
  // Collections
  public static final String COLLECTION_USERS = "users";
  public static final String COLLECTION_SPENDINGS = "spending";
  public static final String COLLECTION_BUDGETS = "budgets";
  public static final String COLLECTION_CATEGORIES = "categories";
  public static final String COLLECTION_FRIENDS = "friends";
  public static final String COLLECTION_NOTIFICATIONS = "notifications";
  public static final String COLLECTION_DATA = "data";
  public static final String COLLECTION_WALLET = "wallet";
  public static final String COLLECTION_INFO = "info";

  // User fields
  public static final String FIELD_USER_ID = "userId";
  public static final String FIELD_USER_NAME = "name";
  public static final String FIELD_USER_EMAIL = "email";
  public static final String FIELD_USER_AVATAR = "avatar";
  public static final String FIELD_USER_BIRTHDAY = "birthday";
  public static final String FIELD_USER_GENDER = "gender";
  public static final String FIELD_USER_MONEY = "money";

  // Spending fields
  public static final String FIELD_SPENDING_ID = "id";
  public static final String FIELD_SPENDING_USER_ID = "userId";
  public static final String FIELD_SPENDING_MONEY = "money";
  public static final String FIELD_SPENDING_TYPE = "type";
  public static final String FIELD_SPENDING_TYPE_NAME = "typeName";
  public static final String FIELD_SPENDING_NOTE = "note";
  public static final String FIELD_SPENDING_DATE_TIME = "dateTime";
  public static final String FIELD_SPENDING_IMAGE = "image";
  public static final String FIELD_SPENDING_LOCATION = "location";
  public static final String FIELD_SPENDING_FRIENDS = "friends";

  // Budget fields
  public static final String FIELD_BUDGET_ID = "id";
  public static final String FIELD_BUDGET_USER_ID = "userId";
  public static final String FIELD_BUDGET_AMOUNT = "amount";
  public static final String FIELD_BUDGET_CATEGORY = "category";
  public static final String FIELD_BUDGET_START_DATE = "startDate";
  public static final String FIELD_BUDGET_END_DATE = "endDate";

  // Category fields
  public static final String FIELD_CATEGORY_ID = "id";
  public static final String FIELD_CATEGORY_NAME = "name";
  public static final String FIELD_CATEGORY_ICON = "icon";
  public static final String FIELD_CATEGORY_COLOR = "color";
  public static final String FIELD_CATEGORY_TYPE = "type";

  // Common fields
  public static final String FIELD_CREATED_AT = "createdAt";
  public static final String FIELD_UPDATED_AT = "updatedAt";

  // Storage paths
  public static final String STORAGE_AVATAR_PATH = "avatars/";
  public static final String STORAGE_SPENDING_IMAGES_PATH = "spending_images/";

  // Query parameters
  public static final String PARAM_YEAR = "year";
  public static final String PARAM_MONTH = "month";
  public static final String PARAM_DAY = "day";

  // Maximum query limits
  public static final int MAX_QUERY_LIMIT = 100;
}
