package com.example.quanlychitieu.domain.usecase.user;

import android.net.Uri;
import com.example.quanlychitieu.domain.model.user.User;
import com.example.quanlychitieu.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import java.io.File;

public class UpdateUserInfoUseCase {
    private final UserRepository repository;

    public UpdateUserInfoUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Task<Void> execute(User user, File avatarImage) {
        Uri imageUri = avatarImage != null ? Uri.fromFile(avatarImage) : null;
        return repository.updateUser(user, imageUri);
    }
}