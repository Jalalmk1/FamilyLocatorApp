//package com.usman.smartads.Applovin;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.os.Build;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.LinearLayout;
//
//import com.applovin.mediation.MaxAdFormat;
//import com.applovin.mediation.ads.MaxAdView;
//import com.applovin.sdk.AppLovinSdkUtils;
//import com.usman.smartads.BuildConfig;
//
//public class ApplovinMREC {
//
//    public static MaxAdView preLoadedMREC ;
//    @SuppressLint("MissingPermission")
//    public static void preloadMREC(Activity context, String id) {
//        if (BuildConfig.DEBUG)
//            id = "YOUR_AD_UNIT_ID";
//        preLoadedMREC = new MaxAdView(id, MaxAdFormat.MREC,context);
//        preLoadedMREC.setListener(null);
//        preLoadedMREC.loadAd();
//    }
//
//    @SuppressLint("MissingPermission")
//    public static void showMREC(Activity activity, FrameLayout bannerAdLayout, String id, boolean reload) {
//        if (preLoadedMREC!=null) {
//            MaxAdView mAdView = preLoadedMREC;
//            bannerAdLayout.setVisibility(View.VISIBLE);
//            int widthPx = AppLovinSdkUtils.dpToPx( activity, 300 );
//            int heightPx = AppLovinSdkUtils.dpToPx( activity, 250 );
//            mAdView.setLayoutParams( new FrameLayout.LayoutParams( widthPx, heightPx ) );
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                mAdView.setBackgroundColor(activity.getColor(android.R.color.transparent));
//            }
//            try {
//                if (mAdView != null) {
//                    if (mAdView.getParent() != null) {
//                        ((ViewGroup) mAdView.getParent()).removeView(mAdView);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            bannerAdLayout.removeAllViews();
//            bannerAdLayout.addView(mAdView);
//
//            if (reload)
//                preloadMREC(activity,id);
//        }
//    }
//
//}
