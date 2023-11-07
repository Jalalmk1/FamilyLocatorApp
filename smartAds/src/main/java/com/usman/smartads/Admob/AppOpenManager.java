package com.usman.smartads.Admob;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.usman.smartads.BuildConfig;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks,LifecycleObserver {
    private static final String LOG_TAG = "AppOpenManager";
    public static AppOpenAd appOpenAd = null;
    private Activity currentActivity;
    private static boolean isShowingAd = false;
    private final Application myApplication;
    public static String id;

    public AppOpenManager(Application myApplication, String id) {
        AppOpenManager.id = BuildConfig.DEBUG? "ca-app-pub-3940256099942544/3419835294": id;
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void fetchAd() {
        if (isAdAvailable()) {
            Log.d(LOG_TAG, "Ad already available");
        }
        else {
            try {
                AppOpenAd.AppOpenAdLoadCallback loadCallback = new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.d(LOG_TAG, "error in loading\n" + loadAdError.getMessage());
                    }

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        super.onAdLoaded(appOpenAd);
                        appOpenAd = ad;
                        Log.d(LOG_TAG, "ad loaded");
                    }
                };
                AdRequest request = getAdRequest();
                AppOpenAd.load(myApplication,id, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void showAdIfAvailable() {{
            if (!isShowingAd && isAdAvailable()) {
                Log.d(LOG_TAG, "Will show ad.");
                appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null;
                        isShowingAd = false;
                        Log.d(LOG_TAG, "Ad Dismissed");
                        fetchAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.d(LOG_TAG, "failed to show full screen\n" + adError.getMessage());
                        Log.d(LOG_TAG, "current activity "+currentActivity.getClass().getName());
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        isShowingAd = true;
                        Log.d(LOG_TAG, "Ad showing");
                    }
                });
                appOpenAd.show(currentActivity);
            }
            else {
                fetchAd();
            }
        }
    }

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public boolean isAdAvailable() {
        return appOpenAd != null;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;

//        fetchAd();
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
//        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }
    /**
     * LifecycleObserver methods
     */
    @SuppressLint("SuspiciousIndentation")
    @OnLifecycleEvent(ON_START)
    public void onStart() {
        Log.d(LOG_TAG, "App onStart");
        showAdIfAvailable();
    }
}
