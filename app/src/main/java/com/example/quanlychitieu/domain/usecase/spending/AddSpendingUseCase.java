package com.example.quanlychitieu.domain.usecase.spending;

import android.net.Uri;
import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;

public class AddSpendingUseCase {
    private final SpendingRepository repository;

    public AddSpendingUseCase(SpendingRepository repository) {
        this.repository = repository;
    }

    public Task<String> execute(Spending spending) {
        return repository.addSpending(spending);
    }
}