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
     * @param spending Spending object to add
     * @return Task with the created spending ID
     */
    Task<String> addSpending(Spending spending);

    /**
     * Upload spending image to Firebase Storage
     * @param spendingId ID of the spending
     * @param imageUri URI of the image to upload
     * @return Task containing the download URL of the uploaded image
     */
    Task<String> uploadSpendingImage(String spendingId, Uri imageUri);

    /**
     * Get a specific spending record by ID
     * @param spendingId ID of the spending to retrieve
     * @return Task containing the Spending object
     */
    Task<Spending> getSpending(String spendingId);

    /**
     * Get all spending records for the current user
     * @return Task containing a list of Spending objects
     */
    Task<List<Spending>> getAllSpendings();

    /**
     * Get spending records for a specific month
     * @param date Date within the month to get spendings for
     * @return Task containing a list of Spending objects
     */
    Task<List<Spending>> getSpendingsByMonth(Date date);

    /**
     * Get spendings by date range
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return Task containing a list of Spending objects
     */
    Task<List<Spending>> getSpendingsByDateRange(Date startDate, Date endDate);

    /**
     * Get spendings by category type
     * @param type Category type ID
     * @return Task containing a list of Spending objects
     */
    Task<List<Spending>> getSpendingsByType(int type);

    /**
     * Update an existing spending record
     * @param spending Updated Spending object
     * @return Task for operation completion
     */
    Task<Void> updateSpending(Spending spending);

    /**
     * Delete a spending record
     * @param spendingId ID of the spending to delete
     * @return Task for operation completion
     */
    Task<Void> deleteSpending(String spendingId);

    /**
     * Get total spending amount for a user within a date range
     * @param userId ID of the user
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return Task containing the total amount
     */
    Task<Integer> getTotalSpending(String userId, Date startDate, Date endDate);

    /**
     * Get spending statistics grouped by category
     * @param userId ID of the user
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return Task containing array of [categoryId, total amount]
     */
    Task<List<Object[]>> getSpendingGroupByCategory(String userId, Date startDate, Date endDate);
}
