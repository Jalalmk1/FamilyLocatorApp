//package com.usman.smartads.Applovin;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//
//import com.applovin.mediation.ads.MaxAdView;
//import com.usman.smartads.BuildConfig;
//
//public class ApplovinBanner {
//    public static MaxAdView preLoadedBanner ;
//    @SuppressLint("MissingPermission")
//    public static void preloadBannerAd(Activity context, String id) {
//        if (BuildConfig.DEBUG)
//            id = "YOUR_AD_UNIT_ID";
//        preLoadedBanner = new MaxAdView(id, context);
//        preLoadedBanner.loadAd();
//    }
//
//    @SuppressLint("MissingPermission")
//    public static void showBannerAd(Activity context, FrameLayout bannerAdLayout, String id, boolean reload) {
//        if (preLoadedBanner!=null) {
//            MaxAdView bannerAd = preLoadedBanner;
//            bannerAdLayout.setVisibility(View.VISIBLE);
//            int width = ViewGroup.LayoutParams.MATCH_PARENT;
//            TypedValue tv = new TypedValue();
//            int actionBarHeight = 100;
//            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
//            bannerAd.setLayoutParams(new FrameLayout.LayoutParams(width, actionBarHeight));
//            bannerAd.setGravity(Gravity.CENTER);
//            bannerAd.setBackgroundColor(context.getColor(android.R.color.transparent));
//
//            try {
//                if (bannerAd != null) {
//                    if (bannerAd.getParent() != null) {
//                        ((ViewGroup) bannerAd.getParent()).removeView(bannerAd);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            bannerAdLayout.removeAllViews();
//            bannerAdLayout.addView(bannerAd);
//
//            if (reload)
//                preloadBannerAd(context,id);
//        }
//    }
//
//    public static void loadBannerAd(LinearLayout bannerAdLayout, Activity context, String id) {
//        if (BuildConfig.DEBUG)
//            id = "YOUR_AD_UNIT_ID";
//        MaxAdView mAdView = new MaxAdView(id, context);
//        mAdView.setListener(null);
//        int width = ViewGroup.LayoutParams.MATCH_PARENT;
//        TypedValue tv = new TypedValue();
//        int actionBarHeight = 100;
//        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
//        mAdView.setLayoutParams(new FrameLayout.LayoutParams(width, actionBarHeight));
//        mAdView.setGravity(Gravity.CENTER);
//        mAdView.setBackgroundColor(context.getColor(android.R.color.transparent));
//        mAdView.loadAd();
//
//        try {
//            if (mAdView != null) {
//                if (mAdView.getParent() != null) {
//                    ((ViewGroup) mAdView.getParent()).removeView(mAdView);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        bannerAdLayout.addView(mAdView);
//    }
//
//}
