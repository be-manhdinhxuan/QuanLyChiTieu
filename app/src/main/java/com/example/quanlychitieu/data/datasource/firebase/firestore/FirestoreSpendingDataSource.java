package com.example.quanlychitieu.data.datasource.firebase.firestore;

import android.net.Uri;

import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.android.gms.tasks.Task;

import java.util.Date;
import java.util.List;

/**
 * Interface for spending related operations in Firestore
 */
public interface FirestoreSpendingDataSource {
    /**
     * Add a new spending record to Firestore
     *
     * @param spending Spending object to add
     * @return Task with the created spending ID
     */
    Task<String> addSpending(Spending spending);

    /**
     * Upload spending image (e.g., to Cloudinary via helper or Firebase Storage)
     *
     * @param spendingId ID of the spending
     * @param imageUri   URI of the image to upload
     * @return Task containing the download URL of the uploaded image
     */
    Task<String> uploadSpendingImage(String spendingId, Uri imageUri);

    /**
     * Get a specific spending record by ID
     *
     * @param spendingId ID of the spending to retrieve
     * @return Task containing the Spending object
     */
    Task<Spending> getSpending(String spendingId);

    /**
     * Get all active (non-deleted) spending records for the current user
     *
     * @return Task containing a list of active Spending objects
     */
    Task<List<Spending>> getAllActiveSpendings();

    /**
     * Get all deleted spending records for the current user
     *
     * @return Task containing a list of deleted Spending objects
     */
    Task<List<Spending>> getDeletedSpendings();

    /**
     * Get all spending records (both active and deleted) for the current user
     *
     * @return Task containing a list of all Spending objects
     */
    Task<List<Spending>> getAllSpendings();

    /**
     * Get active spending records for a specific month
     *
     * @param date Date within the month to get spendings for
     * @return Task containing a list of active Spending objects
     */
    Task<List<Spending>> getSpendingsByMonth(Date date);

    /**
     * Get active spendings by date range
     *
     * @param startDate Start date of the range
     * @param endDate   End date of the range
     * @return Task containing a list of active Spending objects
     */
    Task<List<Spending>> getSpendingsByDateRange(Date startDate, Date endDate);

    /**
     * Update an existing spending record
     *
     * @param spending Updated Spending object (must contain the ID)
     * @return Task for operation completion
     */
    Task<Void> updateSpending(Spending spending);

    /**
     * Soft delete a spending record by marking it as deleted
     *
     * @param spendingId ID of the spending to soft delete
     * @return Task for operation completion
     */
    Task<Void> softDeleteSpending(String spendingId);

    /**
     * Restore a soft-deleted spending record
     *
     * @param spendingId ID of the spending to restore
     * @return Task for operation completion
     */
    Task<Void> restoreSpending(String spendingId);

    /**
     * Permanently delete a spending record from Firestore
     *
     * @param spendingId ID of the spending to permanently delete
     * @return Task for operation completion
     */
    Task<Void> hardDeleteSpending(String spendingId);

    /**
     * Get total active spending amount for a user within a date range
     *
     * @param userId    ID of the user
     * @param startDate Start date of the range
     * @param endDate   End date of the range
     * @return Task containing the total amount (only expenses)
     */
    Task<Integer> getTotalSpending(String userId, Date startDate, Date endDate);

    /**
     * Update only the image URL field for a specific spending record
     *
     * @param spendingId ID of the spending to update
     * @param imageUrl   The new image URL (can be null to remove the image)
     * @return Task for operation completion
     */
    Task<Void> updateSpendingImage(String spendingId, String imageUrl);

    /**
     * Get count of deleted spendings for a user
     *
     * @return Task containing the count of deleted spendings
     */
    Task<Integer> getDeletedSpendingsCount();

    /**
     * Permanently delete all soft-deleted spendings older than the specified date
     *
     * @param date Spendings deleted before this date will be permanently removed
     * @return Task for operation completion
     */
    Task<Void> purgeOldDeletedSpendings(Date date);
}
