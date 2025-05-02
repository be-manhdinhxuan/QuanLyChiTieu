package com.example.quanlychitieu.di;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreSpendingDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.data.datasource.firebase.storage.FirebaseStorageDataSource;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalSpendingDataSource;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.data.mapper.UserMapper;
import com.example.quanlychitieu.data.repository.impl.UserRepositoryImpl;
import com.example.quanlychitieu.data.repository.impl.SpendingRepositoryImpl;
import com.example.quanlychitieu.domain.repository.UserRepository;
import com.example.quanlychitieu.domain.repository.SpendingRepository;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public UserRepository provideUserRepository(
            FirestoreUserDataSource firestoreUserDataSource,
            LocalUserDataSource localUserDataSource,
            FirebaseAuthDataSource firebaseAuthDataSource,
            UserMapper userMapper) {
        return new UserRepositoryImpl(
            firestoreUserDataSource,
            localUserDataSource,
            firebaseAuthDataSource,
            userMapper
        );
    }

    @Provides
    @Singleton
    public SpendingRepository provideSpendingRepository(
            FirestoreSpendingDataSource firestoreSpendingDataSource,
            LocalSpendingDataSource localSpendingDataSource,
            FirebaseAuthDataSource firebaseAuthDataSource,
            FirebaseStorageDataSource firebaseStorageDataSource,
            SpendingMapper spendingMapper) {
        return new SpendingRepositoryImpl(
                firestoreSpendingDataSource,
                localSpendingDataSource,
                firebaseAuthDataSource,
                firebaseStorageDataSource,
                spendingMapper);
    }
}