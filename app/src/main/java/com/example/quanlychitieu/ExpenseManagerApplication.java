package com.example.quanlychitieu;

import android.app.Application;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class ExpenseManagerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}