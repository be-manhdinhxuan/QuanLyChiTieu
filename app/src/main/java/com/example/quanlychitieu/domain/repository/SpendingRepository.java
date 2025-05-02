package com.example.quanlychitieu.domain.repository;

import android.net.Uri;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.google.android.gms.tasks.Task;
import java.util.Date;
import java.util.List;

/**
 * Interface defining operations related to spending management
 */
public interface SpendingRepository {
    /**
     * Get all spendings for the current user
     */
    Task<List<Spending>> getAllSpendings();

    /**
     * Get spendings within a date range
     */
    Task<List<Spending>> getSpendingsByDate(Date startDate, Date endDate);

    /**
     * Get spending details by ID
     */
    Task<Spending> getSpendingById(String spendingId);

    /**
     * Add a new spending
     *
     * @param spending The spending information
     * @return Task containing the ID of the new spending
     */
    Task<String> addSpending(Spending spending);

    /**
     * Add a new spending with an image
     *
     * @param spending The spending information
     * @param imageUri The URI of the image
     * @return Task containing the ID of the new spending
     */
    Task<String> addSpendingWithImage(Spending spending, Uri imageUri);

    /**
     * Update an existing spending
     */
    Task<Void> updateSpending(Spending spending);

    /**
     * Update a spending with image handling
     *
     * @param spending The spending information to update
     * @param newImageUri The URI of the new image (null if unchanged)
     * @param isImageRemoved true if the existing image should be deleted
     */
    Task<Void> updateSpendingWithImage(Spending spending, Uri newImageUri, boolean isImageRemoved);

    /**
     * Delete a spending by ID
     */
    Task<Void> deleteSpending(String spendingId);

    /**
     * Synchronize spendings from remote to local storage
     */
    Task<Void> syncSpendings();

    /**
     * Calculate total spending within a date range
     */
    Task<Integer> getTotalSpending(Date startDate, Date endDate);

    /**
     * Get the URL of a spending's image
     */
    Task<String> getSpendingImageUrl(String spendingId);

    /**
     * Delete a spending's image
     */
    Task<Void> deleteSpendingImage(String spendingId);

    /**
     * Upload a new image for a spending
     */
    Task<String> uploadSpendingImage(String spendingId, Uri imageUri);

    /**
     * Soft delete a spending by ID
     * @param spendingId ID of the spending to soft delete
     * @return Task<Void>
     */
    Task<Void> softDeleteSpending(String spendingId);

    /**
     * Get all soft-deleted spendings
     * @return Task containing list of deleted spendings
     */
    Task<List<Spending>> getDeletedSpendings();

    /**
     * Restore a soft-deleted spending
     * @param spendingId ID of the spending to restore
     * @return Task<Void>
     */
    Task<Void> restoreSpending(String spendingId);
}
