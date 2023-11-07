//package com.usman.smartads.Applovin;
//
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Intent;
//import android.graphics.drawable.ColorDrawable;
//import android.os.CountDownTimer;
//import android.util.Log;
//import android.view.Window;
//import android.widget.Toast;
//
//import com.applovin.mediation.MaxAd;
//import com.applovin.mediation.MaxAdListener;
//import com.applovin.mediation.MaxError;
//import com.applovin.mediation.ads.MaxInterstitialAd;
//import com.usman.smartads.BuildConfig;
//import com.usman.smartads.R;
//
//public class ApplovinInterstitial {
//    public static void preLoadInterstitialAd(Activity activity, String id)  {
//        if (preLoadedInterstitial == null && activity != null && !activity.isDestroyed() && id != null && !id.isEmpty()) {
//            if (BuildConfig.DEBUG)
//                id = "YOUR_AD_UNIT_ID";
//            preLoadedInterstitial = new MaxInterstitialAd(id, activity);
//            preLoadedInterstitial.setListener(new MaxAdListener() {
//                @Override
//                public void onAdLoaded(MaxAd ad) {
//                    Log.i("ADS_UTILS", "onAdPreLoaded Applovin");
//                }
//
//                @Override
//                public void onAdDisplayed(MaxAd ad) {
//
//                }
//
//                @Override
//                public void onAdHidden(MaxAd ad) {
//
//                }
//
//                @Override
//                public void onAdClicked(MaxAd ad) {
//
//                }
//
//                @Override
//                public void onAdLoadFailed(String adUnitId, MaxError error) {
//                    preLoadedInterstitial = null;
//                    Log.i("PreInterstitialError", error.getMessage());
//                }
//
//                @Override
//                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
//                }
//            });
//            preLoadedInterstitial.loadAd();
//        }
//    }
//
//    public static void showInterstitialAd(Activity activity, String id, boolean reload,boolean showLoading, Runnable runnable) {
//        Dialog addProgressDialog = new Dialog(activity);
//        addProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        addProgressDialog.setContentView(R.layout.loading_layout);
//        addProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//        addProgressDialog.setCanceledOnTouchOutside(false);
//        CountDownTimer cTimer = new CountDownTimer(1000, 500) {
//            public void onTick(long millisUntilFinished) {
//            }
//
//            public void onFinish() {
//                preLoadedInterstitial.setListener(new MaxAdListener() {
//                    @Override
//                    public void onAdLoaded(MaxAd ad) {
//                    }
//
//                    @Override
//                    public void onAdDisplayed(MaxAd ad) {
//                        Log.i("ADS_UTILS", "onAdPreLoadedDisplayed");
//                        try {
//                            addProgressDialog.hide();
//                            addProgressDialog.dismiss();
//                        } catch (Exception e) {
//                        }
//                    }
//
//                    @Override
//                    public void onAdHidden(MaxAd ad) {
//                        preLoadedInterstitial = null;
//                        if (reload)
//                            preLoadInterstitialAd(activity,id);
//                        if (runnable!=null)
//                            runnable.run();
//                    }
//
//                    @Override
//                    public void onAdClicked(MaxAd ad) {
//
//                    }
//
//                    @Override
//                    public void onAdLoadFailed(String adUnitId, MaxError error) {
//                    }
//
//                    @Override
//                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
//                        try {
//                            addProgressDialog.hide();
//                            addProgressDialog.dismiss();
//                        } catch (Exception e) {
//                        }
//                        preLoadedInterstitial = null;
//                        if (reload)
//                            preLoadInterstitialAd(activity,id);
//                        if (runnable!=null)
//                            runnable.run();
//                    }
//                });
//                preLoadedInterstitial.showAd();
//            }
//        };
//        if (preLoadedInterstitial != null && preLoadedInterstitial.isReady()) {
//            if (showLoading) {
//                cTimer.start();
//                addProgressDialog.show();
//            }
//            else cTimer.onFinish();
//        } else {
//            if (reload)
//                preLoadInterstitialAd(activity,id);
//            if (runnable != null)
//                runnable.run();
//        }
//    }
//    public static MaxInterstitialAd preLoadedInterstitial = null;
//
//}
