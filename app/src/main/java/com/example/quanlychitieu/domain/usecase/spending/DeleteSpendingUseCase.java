package com.example.quanlychitieu.domain.usecase.spending;

import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;

public class DeleteSpendingUseCase {
    private final SpendingRepository repository;

    public DeleteSpendingUseCase(SpendingRepository repository) {
        this.repository = repository;
    }

    public Task<Void> execute(Spending spending) {
        return repository.deleteSpending(spending.getId());
    }
}