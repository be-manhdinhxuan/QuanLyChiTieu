package com.example.quanlychitieu.data.datasource.local.datasource;

import androidx.annotation.Nullable; // Thêm Nullable nếu cần
import com.example.quanlychitieu.domain.model.spending.Spending;

import java.util.Date;
import java.util.List;

/**
 * Interface defining operations for the local spending cache (e.g., Room database).
 * Note: Current implementation assumes soft-deleted items are REMOVED from this cache.
 */
public interface LocalSpendingDataSource {

  /**
   * Get a specific spending record from the local cache by its ID.
   * Might return null if the spending is not cached or was removed (soft-deleted).
   *
   * @param id The unique ID of the spending.
   * @return The Spending object or null if not found.
   */
  @Nullable // Thêm Nullable để rõ ràng hơn
  Spending getSpending(String id);

  /**
   * Get all spending records for a specific user from the local cache.
   * This should only return items that are currently cached (i.e., not soft-deleted).
   *
   * @param userId The ID of the user.
   * @return A list of Spending objects. Returns an empty list if none found.
   */
  List<Spending> getAllSpendings(String userId);

  /**
   * Get spending records for a specific user and month from the local cache.
   * This should only return items that are currently cached.
   *
   * @param userId The ID of the user.
   * @param date   A date within the desired month.
   * @return A list of Spending objects for that month.
   */
  List<Spending> getSpendingsByMonth(String userId, Date date); // Xem xét dùng monthKey

  /**
   * Get spending records for a specific user within a date range from the local cache.
   * This should only return items that are currently cached.
   *
   * @param userId    The ID of the user.
   * @param startDate The start date of the range.
   * @param endDate   The end date of the range.
   * @return A list of Spending objects within the date range.
   */
  List<Spending> getSpendingsByDateRange(String userId, Date startDate, Date endDate);

  /**
   * Get spending records for a specific user and type from the local cache.
   * This should only return items that are currently cached.
   *
   * @param userId The ID of the user.
   * @param type   The spending type code.
   * @return A list of Spending objects of the specified type.
   */
  List<Spending> getSpendingsByType(String userId, int type);

  /**
   * Get spending records from the local cache that haven't been synced yet.
   * (Logic for synchronization, likely unrelated to soft delete).
   *
   * @return A list of unsynced Spending objects.
   */
  List<Spending> getUnsyncedSpendings(); // Giữ nguyên cho logic sync

  /**
   * Save or update a single spending record in the local cache.
   * Used for adding new spendings, caching fetched/updated spendings,
   * and caching restored spendings.
   * Assumes the Spending object contains its correct ID.
   *
   * @param spending The Spending object to save/update.
   * @return The row ID of the inserted/updated record (specific to Room/SQLite).
   */
  long saveSpending(Spending spending);

  /**
   * Save or update a list of spending records in the local cache.
   * Typically used during synchronization.
   *
   * @param spendings The list of Spending objects to save/update.
   */
  void saveSpendings(List<Spending> spendings);

  /**
   * Update an existing spending record in the local cache.
   * (Có thể trùng lặp với saveSpending nếu saveSpending thực hiện 'insert or replace').
   * Giữ lại nếu implementation cần phương thức update riêng.
   *
   * @param spending The updated Spending object.
   */
  void updateSpending(Spending spending);

  /**
   * Delete a spending record from the local cache by its ID.
   * This is called by the Repository during both soft delete (to remove from active view)
   * and permanent delete operations.
   *
   * @param id The unique ID of the spending to delete from the cache.
   */
  void deleteSpending(String id);

  /**
   * Mark a locally cached spending record as synced.
   * (Logic for synchronization).
   *
   * @param id The unique ID of the spending to mark as synced.
   */
  void markAsSynced(String id); // Giữ nguyên cho logic sync

  /* --- CÁC PHƯƠNG THỨC CÓ THỂ THÊM TRONG TƯƠNG LAI (NẾU CẦN) --- */

  /**
   * (Future) Get all soft-deleted spending records stored locally (if caching strategy changes).
   *
   * @param userId The ID of the user.
   * @return A list of locally cached, soft-deleted Spending objects.
   */
  // List<Spending> getLocalDeletedSpendings(String userId);

  /**
   * (Future) Update only the image URL in the local cache.
   *
   * @param spendingId The ID of the spending.
   * @param imageUrl   The new image URL.
   */
  // void updateSpendingImage(String spendingId, @Nullable String imageUrl);

  /**
   * (Future) Delete all cached spendings for a specific user. Useful for logout or full sync.
   * @param userId The ID of the user whose cache should be cleared.
   */
  // void deleteAllSpendingsByUser(String userId);

}
