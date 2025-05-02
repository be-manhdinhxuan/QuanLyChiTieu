package com.example.quanlychitieu.data.repository.adapter;

import android.net.Uri;
import android.content.Context;
import android.util.Log; // Import Log

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthConfig;
import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreSpendingDataSource; // Import interface
import com.example.quanlychitieu.data.datasource.firebase.firestore.impl.FirestoreSpendingDataSourceImpl;
import com.example.quanlychitieu.data.datasource.firebase.storage.FirebaseStorageDataSource;
import com.example.quanlychitieu.data.datasource.firebase.storage.impl.FirebaseStorageDataSourceImpl;
import com.example.quanlychitieu.data.datasource.local.AppDatabase; // Assuming you have AppDatabase
import com.example.quanlychitieu.data.datasource.local.impl.LocalSpendingDataSourceImpl;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.data.repository.impl.SpendingRepositoryImpl;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository; // Import interface
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Date;
import java.util.List;

/**
 * Adapter to maintain compatibility with older code using SpendingRepository directly.
 * This adapter delegates calls to the actual SpendingRepository implementation.
 */
public class LegacySpendingRepositoryAdapter {
  private static final String TAG = "LegacySpendingAdapter"; // TAG for logging
  private final SpendingRepository repository;
  // Context might not be needed here if not used directly
  // private final Context context;

  // Constructor initializes the real repository with its dependencies
  public LegacySpendingRepositoryAdapter(Context context, FirebaseFirestore firestore) {
    // this.context = context; // Store if needed elsewhere

    // Initialize dependencies
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    SpendingMapper mapper = new SpendingMapper(); // Assuming SpendingMapper has a default constructor

    // Initialize data sources
    // Use interfaces for better practice
    FirestoreSpendingDataSource remoteDataSource = new FirestoreSpendingDataSourceImpl(
            firestore,
            firebaseStorage,
            firebaseAuth,
            context.getApplicationContext()); // Use application context

    // Assuming you have Room setup for local data source
    // AppDatabase database = AppDatabase.getDatabase(context.getApplicationContext());
    // SpendingDao spendingDao = database.spendingDao();
    // LocalSpendingDataSourceImpl localDataSource = new LocalSpendingDataSourceImpl(spendingDao, mapper);
    // If no local source, pass null or a dummy implementation if the Repo requires it

    FirebaseAuthDataSource authDataSource = FirebaseAuthConfig.getAuthDataSource(); // Assuming this provides the interface

    // Initialize storage data source (constructor seems empty, might need context or FirebaseStorage instance)
    FirebaseStorageDataSource storageDataSource = new FirebaseStorageDataSourceImpl(/* Pass dependencies if needed */);

    // Initialize the actual repository implementation
    this.repository = new SpendingRepositoryImpl(
            remoteDataSource,
            null, // Replace with localDataSource if using Room
            authDataSource,
            storageDataSource,
            mapper);
  }

  /**
   * Updates a spending record, handling image updates or removal.
   * @param spending The Spending object with updated data (ID should be populated from Firestore).
   * @param newImageUri The URI of the new image, or null if no image change.
   * @param isImageRemoved True if the existing image should be removed.
   * @return Task<Void> indicating completion.
   */
  public Task<Void> updateSpending(Spending spending, Uri newImageUri, boolean isImageRemoved) {
    if (spending == null || spending.getId() == null || spending.getId().isEmpty()) {
      Log.e(TAG, "updateSpending: Invalid Spending object or ID is null/empty.");
      return Tasks.forException(new IllegalArgumentException("Invalid Spending object or ID for update."));
    }
    Log.d(TAG, "updateSpending called for ID: " + spending.getId() + ", newImageUri: " + newImageUri + ", isImageRemoved: " + isImageRemoved);

    if (isImageRemoved) {
      // If the user wants to remove the image
      Log.d(TAG, "Removing image for spending ID: " + spending.getId());
      // Assuming deleteSpendingImage handles null/empty current image gracefully
      return repository.deleteSpendingImage(spending.getId())
              .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                  // Log error but still try to update spending data
                  Log.e(TAG, "Failed to delete image for " + spending.getId() + ", but proceeding with data update.", task.getException());
                } else {
                  Log.d(TAG, "Successfully deleted image (if existed) for " + spending.getId());
                }
                // Clear the image field before updating
                spending.setImage(null);
                return repository.updateSpending(spending); // Use the existing update method
              });
    } else if (newImageUri != null) {
      // If the user wants to upload a new image (this might replace an old one)
      Log.d(TAG, "Uploading new image for spending ID: " + spending.getId());
      return repository.uploadSpendingImage(spending.getId(), newImageUri)
              .continueWithTask(uploadTask -> {
                if (!uploadTask.isSuccessful()) {
                  Log.e(TAG, "Failed to upload new image for " + spending.getId(), uploadTask.getException());
                  // Decide if you still want to update the rest of the data or fail completely
                  throw uploadTask.getException(); // Fail if image upload fails
                }
                String imageUrl = uploadTask.getResult();
                Log.d(TAG, "Successfully uploaded new image for " + spending.getId() + ". URL: " + imageUrl);
                spending.setImage(imageUrl); // Set the new image URL
                return repository.updateSpending(spending); // Use the existing update method
              });
    } else {
      // If no image change is requested
      Log.d(TAG, "Updating spending data without image changes for ID: " + spending.getId());
      // Ensure the existing image URL is preserved if not changing
      // The 'spending' object passed in should already have the correct image URL if no change
      return repository.updateSpending(spending); // Use the existing update method
    }
  }

  /**
   * Adds a new spending record and optionally uploads an image.
   * @param spending The Spending object to add (ID field will be ignored).
   * @param imageUri The URI of the image to upload, or null if no image.
   * @return Task<Void> indicating completion.
   */
  public Task<Void> addSpending(Spending spending, Uri imageUri) {
    if (spending == null) {
      Log.e(TAG, "addSpending: Spending object cannot be null.");
      return Tasks.forException(new IllegalArgumentException("Spending object cannot be null."));
    }
    Log.d(TAG, "addSpending called. Has image: " + (imageUri != null));

    // First, add the spending data to get the new ID
    return repository.addSpending(spending)
            .continueWithTask(task -> {
              if (task.isSuccessful()) {
                String spendingId = task.getResult(); // Get the ID generated by Firestore
                if (spendingId == null || spendingId.isEmpty()) {
                  Log.e(TAG, "addSpending: Repository returned null or empty ID after adding spending.");
                  throw new Exception("Failed to get spending ID after adding.");
                }
                Log.d(TAG, "Successfully added spending data, received ID: " + spendingId);

                if (imageUri != null) {
                  // If there's an image, upload it using the new ID
                  Log.d(TAG, "Uploading image for new spending ID: " + spendingId);
                  return repository.uploadSpendingImage(spendingId, imageUri)
                          .continueWithTask(uploadTask -> {
                            if (!uploadTask.isSuccessful()) {
                              Log.e(TAG, "Failed to upload image for new spending " + spendingId + ", but spending data was saved.", uploadTask.getException());
                              // Let the overall task succeed but log the image error.
                              return Tasks.forResult(null); // Indicate overall success despite image failure
                            }
                            String imageUrl = uploadTask.getResult();
                            Log.d(TAG, "Successfully uploaded image for " + spendingId + ". URL: " + imageUrl);

                            // *** SỬA ĐỔI Ở ĐÂY ***
                            // Now update the spending record with the image URL using the existing updateSpending method.
                            // WARNING: This is inefficient as it requires fetching the object again.
                            // It's better to add an updateSpendingImage or partial update method to the repository.
                            Log.w(TAG, "Fetching spending " + spendingId + " again to update image URL (inefficient).");
                            return repository.getSpendingById(spendingId).continueWithTask(getTask -> {
                              if (!getTask.isSuccessful() || getTask.getResult() == null) {
                                Log.e(TAG, "Failed to fetch spending " + spendingId + " after image upload to update URL.", getTask.getException());
                                // Fail the task if we can't fetch to update image URL
                                throw new Exception("Failed to fetch spending after image upload.", getTask.getException());
                              }
                              Spending fetchedSpending = getTask.getResult();
                              fetchedSpending.setImage(imageUrl); // Set the image URL on the fetched object
                              Log.d(TAG, "Updating spending " + spendingId + " with new image URL.");
                              return repository.updateSpending(fetchedSpending); // Call the standard update method
                            });
                            // *** KẾT THÚC SỬA ĐỔI ***
                          });
                } else {
                  // No image to upload, task is complete
                  Log.d(TAG, "No image to upload for spending ID: " + spendingId);
                  return Tasks.forResult(null); // Indicate success
                }
              } else {
                Log.e(TAG, "Failed to add spending data.", task.getException());
                // Propagate the exception
                throw task.getException();
              }
            });
  }

  /**
   * Deletes a spending record and its associated image (if any).
   * @param spending The Spending object to delete (must have a valid ID).
   * @return Task<Void> indicating completion.
   */
  public Task<Void> deleteSpending(Spending spending) {
    if (spending == null || spending.getId() == null || spending.getId().isEmpty()) {
      Log.e(TAG, "deleteSpending: Invalid Spending object or ID is null/empty.");
      return Tasks.forException(new IllegalArgumentException("Invalid Spending object or ID for delete."));
    }
    String spendingId = spending.getId();
    Log.d(TAG, "deleteSpending called for ID: " + spendingId);

    // First, try to delete the image (handles cases where image is null/empty)
    return repository.deleteSpendingImage(spendingId)
            .continueWithTask(deleteImageTask -> {
              if (!deleteImageTask.isSuccessful()) {
                // Log error but continue to delete the spending data
                Log.e(TAG, "Failed to delete image for " + spendingId + ", but proceeding with data deletion.", deleteImageTask.getException());
              } else {
                Log.d(TAG, "Successfully deleted image (if existed) for " + spendingId);
              }
              // Now delete the spending data document
              return repository.deleteSpending(spendingId);
            });
  }

  // --- Other delegated methods ---

  public Task<List<Spending>> getAllSpendings() {
    Log.d(TAG, "getAllSpendings called");
    return repository.getAllSpendings();
  }

  public Task<List<Spending>> getSpendingsByDate(Date startDate, Date endDate) {
    Log.d(TAG, "getSpendingsByDate called: " + startDate + " to " + endDate);
    return repository.getSpendingsByDate(startDate, endDate);
  }

  public Task<Integer> getTotalSpending(Date startDate, Date endDate) {
    Log.d(TAG, "getTotalSpending called: " + startDate + " to " + endDate);
    return repository.getTotalSpending(startDate, endDate);
  }

  public Task<String> getSpendingImageUrl(String spendingId) {
    if (spendingId == null || spendingId.isEmpty()) {
      Log.e(TAG, "getSpendingImageUrl: spendingId is null or empty.");
      return Tasks.forException(new IllegalArgumentException("Spending ID cannot be null or empty."));
    }
    Log.d(TAG, "getSpendingImageUrl called for ID: " + spendingId);
    return repository.getSpendingImageUrl(spendingId);
  }

  // Bỏ phương thức này vì interface không có
     /*
     // Add a method to update only the image URL if needed after upload in addSpending
     // This assumes SpendingRepository has such a method
     public Task<Void> updateSpendingImage(String spendingId, String imageUrl) {
         Log.d(TAG, "Updating image URL for spending ID: " + spendingId);
         // This method needs to be implemented in SpendingRepository and its datasources
         // For example:
         // return remoteDataSource.updateSpendingField(spendingId, "image", imageUrl);
         // Placeholder:
         if (repository instanceof SpendingRepositoryImpl) {
             // Ideally, SpendingRepository interface should have this method
             return ((SpendingRepositoryImpl) repository).updateSpendingImage(spendingId, imageUrl);
         } else {
              Log.e(TAG, "updateSpendingImage not implemented for this repository type");
              return Tasks.forException(new UnsupportedOperationException("updateSpendingImage not implemented"));
         }
     }
     */
}