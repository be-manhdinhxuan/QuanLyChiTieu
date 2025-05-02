package com.example.quanlychitieu.di;

import com.example.quanlychitieu.data.datasource.firebase.auth.FirebaseAuthDataSource;
import com.example.quanlychitieu.data.datasource.firebase.auth.impl.FirebaseAuthDataSourceImpl;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreSpendingDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.FirestoreUserDataSource;
import com.example.quanlychitieu.data.datasource.firebase.firestore.impl.FirestoreSpendingDataSourceImpl;
import com.example.quanlychitieu.data.datasource.firebase.firestore.impl.FirestoreUserDataSourceImpl;
import com.example.quanlychitieu.data.datasource.firebase.storage.FirebaseStorageDataSource;
import com.example.quanlychitieu.data.datasource.firebase.storage.impl.FirebaseStorageDataSourceImpl;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalSpendingDataSource;
import com.example.quanlychitieu.data.datasource.local.datasource.LocalUserDataSource;
import com.example.quanlychitieu.data.datasource.local.impl.LocalSpendingDataSourceImpl;
import com.example.quanlychitieu.data.datasource.local.impl.LocalUserDataSourceImpl;
import com.example.quanlychitieu.data.mapper.SpendingMapper;
import com.example.quanlychitieu.data.mapper.UserMapper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataSourceModule {

    @Provides
    @Singleton
    public SpendingMapper provideSpendingMapper() {
        return new SpendingMapper();
    }

    @Provides
    @Singleton
    public UserMapper provideUserMapper() {
        return new UserMapper();
    }

    @Provides
    @Singleton
    public FirebaseAuthDataSource provideFirebaseAuthDataSource(FirebaseAuthDataSourceImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public FirestoreSpendingDataSource provideFirestoreSpendingDataSource(FirestoreSpendingDataSourceImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public FirestoreUserDataSource provideFirestoreUserDataSource(FirestoreUserDataSourceImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public FirebaseStorageDataSource provideFirebaseStorageDataSource(FirebaseStorageDataSourceImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public LocalSpendingDataSource provideLocalSpendingDataSource(LocalSpendingDataSourceImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    public LocalUserDataSource provideLocalUserDataSource(LocalUserDataSourceImpl impl) {
        return impl;
    }
}