package com.example.quanlychitieu.domain.usecase.spending;

import com.example.quanlychitieu.domain.model.spending.Spending;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.Date;

public class GetSpendingsUseCase {
    private final SpendingRepository repository;

    public GetSpendingsUseCase(SpendingRepository repository) {
        this.repository = repository;
    }

    public Task<List<Spending>> execute(Date startDate, Date endDate) {
        return repository.getSpendingsByDate(startDate, endDate);
    }
}