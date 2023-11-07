package com.usman.smartads.Facebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.view.Window;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.usman.smartads.BuildConfig;
import com.usman.smartads.R;

public class FacebookInterstitial {
    private static InterstitialAd preLoadedFacebookInterstitial = null;
    public static boolean reload = false;
    public static Runnable onAdClosed = new Runnable() {
        @Override
        public void run() {

        }
    };

    public static void preLoadInterstitialAd(Context activity, String id) {
        if (preLoadedFacebookInterstitial == null && !id.isEmpty()) {
            if (BuildConfig.DEBUG)
                id = "YOUR_PLACEMENT_ID";
            preLoadedFacebookInterstitial = new InterstitialAd(activity, id);
            String finalId = id;
            preLoadedFacebookInterstitial.loadAd(preLoadedFacebookInterstitial.buildLoadAdConfig()
                            .withAdListener(new InterstitialAdListener() {
                                @Override
                                public void onInterstitialDisplayed(Ad ad) {

                                }

                                @Override
                                public void onInterstitialDismissed(Ad ad) {
                                    preLoadedFacebookInterstitial = null;
                                    if (reload)
                                        preLoadInterstitialAd(activity, finalId);
                                    if (onAdClosed!=null)
                                        onAdClosed.run();
                                }

                                @Override
                                public void onError(Ad ad, AdError adError) {

                                }

                                @Override
                                public void onAdLoaded(Ad ad) {

                                }

                                @Override
                                public void onAdClicked(Ad ad) {

                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {

                                }
                            })
                            .build());
        }
    }

    public static void showInterstitialAd(Activity activity, String id, boolean reload, boolean showLoading, Runnable runnable) {
        if (BuildConfig.DEBUG)
            id = "YOUR_PLACEMENT_ID";

        FacebookInterstitial.onAdClosed = runnable;
        FacebookInterstitial.reload = reload;

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
                try {
                    addProgressDialog.hide();
                    addProgressDialog.dismiss();
                } catch (Exception e) {
                }
                preLoadedFacebookInterstitial.show();
            }
        };
       if (preLoadedFacebookInterstitial != null && preLoadedFacebookInterstitial.isAdLoaded()) {
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
