package com.usman.smartads.Admob;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Window;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.usman.smartads.BuildConfig;
import com.usman.smartads.R;

public class AdmobInterstitial {
    private static InterstitialAd preLoadedAdmobInterstitial = null;

    public static void preLoadInterstitialAd(Context activity, String id) {
        if (preLoadedAdmobInterstitial == null && !id.isEmpty()) {
            if (BuildConfig.DEBUG)
                id = "ca-app-pub-3940256099942544/1033173712";
            AdRequest adRequest = new AdRequest.Builder().build();
            String finalId = id;
            InterstitialAd.load(activity, id, adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            preLoadedAdmobInterstitial = interstitialAd;
                            Log.i("ADS_UTILS", "onAdPreLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.i("PreInterstitialError", loadAdError.getMessage() + finalId);
                        }
                    });
        }
    }

    public static void showInterstitialAd(Activity activity, String id, boolean reload, boolean showLoading, Runnable runnable) {
        if (BuildConfig.DEBUG)
            id = "ca-app-pub-3940256099942544/1033173712";
        Dialog addProgressDialog = new Dialog(activity);
        addProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addProgressDialog.setContentView(R.layout.loading_layout);
        addProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addProgressDialog.setCanceledOnTouchOutside(false);
        String finalId = id;
        CountDownTimer cTimer = new CountDownTimer(1000, 500) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                preLoadedAdmobInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            preLoadedAdmobInterstitial = null;
                            if (reload)
                                preLoadInterstitialAd(activity, finalId);
                            if (runnable!=null)
                                runnable.run();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            try {
                                addProgressDialog.hide();
                                addProgressDialog.dismiss();
                            } catch (Exception e) {
                            }
                            preLoadedAdmobInterstitial = null;
                            if (reload)
                                preLoadInterstitialAd(activity, finalId);
                            if (runnable!=null)
                                runnable.run();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            try {
                                addProgressDialog.hide();
                                addProgressDialog.dismiss();
                            } catch (Exception e) {
                            }
                        }

                    });
                preLoadedAdmobInterstitial.show(activity);
            }
        };
       if (preLoadedAdmobInterstitial != null) {
           if (showLoading) {
               cTimer.start();
               addProgressDialog.show();
           }
           else cTimer.onFinish();
       } else {
           if (reload)
               preLoadInterstitialAd(activity, finalId);
           if (runnable!=null)
               runnable.run();
       }
    }

}
