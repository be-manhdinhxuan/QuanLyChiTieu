package com.example.quanlychitieu.domain.usecase.spending;

import android.net.Uri;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;

public class UpdateSpendingUseCase {
    private final SpendingRepository repository;

    public UpdateSpendingUseCase(SpendingRepository repository) {
        this.repository = repository;
    }

    public Task<Void> execute(Spending spending, Uri newImageUri, boolean isImageRemoved) {
        return repository.updateSpending(spending);
    }
}