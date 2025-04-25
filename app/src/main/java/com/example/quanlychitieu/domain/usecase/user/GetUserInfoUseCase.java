package com.example.quanlychitieu.domain.usecase.user;

import com.example.quanlychitieu.domain.model.user.User;
import com.example.quanlychitieu.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;

public class GetUserInfoUseCase {
    private final UserRepository repository;

    public GetUserInfoUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Task<User> execute() {
        return repository.getCurrentUser();
    }
}