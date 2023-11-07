//package com.usman.smartads.Applovin;
//
//import android.app.Activity;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import com.applovin.mediation.MaxAd;
//import com.applovin.mediation.MaxError;
//import com.applovin.mediation.nativeAds.MaxNativeAdListener;
//import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
//import com.applovin.mediation.nativeAds.MaxNativeAdView;
//import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder;
//import com.usman.smartads.BuildConfig;
//import com.usman.smartads.R;
//
//public class ApplovinNative {
//    public static MaxNativeAdView preLoadedNative = null;
//    public static boolean loadingNative = false;
//    public static void preLoadNativeAd(Activity activity, String id, Runnable runnable) {
//        if (!loadingNative) {
//            loadingNative = true;
//            if (BuildConfig.DEBUG)
//                id = "YOUR_AD_UNIT_ID";
//
//            MaxNativeAdViewBinder binder = new MaxNativeAdViewBinder.Builder( R.layout.native_ad_layout_lovin )
//                    .setTitleTextViewId( R.id.native_ad_title )
//                    .setBodyTextViewId( R.id.native_ad_body )
//                    .setAdvertiserTextViewId( R.id.native_ad_advertiser )
//                    .setIconImageViewId( R.id.native_ad_icon )
//                    .setMediaContentViewGroupId( R.id.native_ad_media )
//                    .setOptionsContentViewGroupId( R.id.ad_choices_container )
//                    .setCallToActionButtonId( R.id.native_ad_call_to_action)
//                    .build();
//
//            MaxNativeAdLoader adLoader = new MaxNativeAdLoader( id, activity );
//            String finalId = id;
//            adLoader.setNativeAdListener(new MaxNativeAdListener() {
//                @Override
//                public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
//                    preLoadedNative = nativeAdView;
//                    if (runnable != null)
//                        runnable.run();
//                    loadingNative = false;
//
//                    Log.i("ADS_UTILS", "onAdPreLoadedNAtive Applovin "+ finalId);
//
//                }
//
//                @Override
//                public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
//                    if (runnable != null)
//                        runnable.run();
//
//                    loadingNative = false;
//
//                }
//
//                @Override
//                public void onNativeAdClicked(final MaxAd ad) {
//                }
//            } );
//            adLoader.loadAd(new MaxNativeAdView( binder, activity ));
//        }
//    }
//    public static void showNativeAd(Activity activity,FrameLayout native_ad_frame, String id, boolean reload) {
//        if (preLoadedNative != null) {
//            native_ad_frame.setVisibility(View.VISIBLE);
//            MaxNativeAdView adView = preLoadedNative;
//            FrameLayout.LayoutParams params;
//            params = new FrameLayout.LayoutParams(
//                    FrameLayout.LayoutParams.MATCH_PARENT,
//                    FrameLayout.LayoutParams.MATCH_PARENT
//            );
//            params.setMargins(0, 0, 0, 8);
//            adView.setLayoutParams(params);
//
//            try {
//                if (adView.getParent() != null) {
//                    ((ViewGroup) adView.getParent()).removeView(adView);
//                }
//            }catch (Exception e){}
//
//            native_ad_frame.removeAllViews();
//            native_ad_frame.addView(adView);
//
//            if (reload)
//                preLoadNativeAd(activity,id,null);
//        }
//    }
//
//}
