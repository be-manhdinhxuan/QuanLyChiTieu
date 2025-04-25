package com.example.quanlychitieu.domain.usecase.auth;

import com.example.quanlychitieu.domain.repository.AuthRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class LoginUseCase {
    private final AuthRepository repository;

    public LoginUseCase(AuthRepository repository) {
        this.repository = repository;
    }

    public Task<AuthResult> execute(String email, String password) {
        return repository.loginWithEmailAndPassword(email, password);
    }
}