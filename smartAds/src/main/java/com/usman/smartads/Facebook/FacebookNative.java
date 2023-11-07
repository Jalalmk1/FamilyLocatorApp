package com.usman.smartads.Facebook;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;

import com.usman.smartads.BuildConfig;
import com.usman.smartads.Placements;
import com.usman.smartads.R;

import java.util.ArrayList;
import java.util.List;

public class FacebookNative {
    private static NativeAd preLoadedFacebookNative = null;
    public static String TAG = "facebook_ads";
    public static boolean loadingNative = false;
    public static void preLoadNativeAd(Activity activity, String id, Runnable runnable) {
        if (!loadingNative && !id.isEmpty()) {
            loadingNative = true;
            if (BuildConfig.DEBUG)
                id = "YOUR_PLACEMENT_ID";

            preLoadedFacebookNative = new NativeAd(activity, id);
            NativeAdListener nativeAdListener = new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                    Log.e(TAG, "Native ad finished downloading all assets.");
                }
                @Override
                public void onError(Ad ad, AdError adError) {
                    loadingNative = false;
                    if (runnable!=null)
                        runnable.run();
                    Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
                }
                @Override
                public void onAdLoaded(Ad ad) {
                    loadingNative = false;
                    if (runnable!=null)
                        runnable.run();
                    Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                }
                @Override
                public void onAdClicked(Ad ad) {
                    Log.d(TAG, "Native ad clicked!");
                }
                @Override
                public void onLoggingImpression(Ad ad) {
                    Log.d(TAG, "Native ad impression logged!");
                }
            };

            preLoadedFacebookNative.loadAd(
                    preLoadedFacebookNative.buildLoadAdConfig()
                            .withAdListener(nativeAdListener)
                            .build());
        }
    }

    public static void showNativeAd(Activity activity, FrameLayout native_ad_frame, String id, boolean showMedia, Boolean reload) {
        if (preLoadedFacebookNative != null) {
            native_ad_frame.removeAllViews();
            native_ad_frame.setVisibility(View.VISIBLE);
            preLoadedFacebookNative.unregisterView();

            NativeAdLayout nativeAdLayout = new NativeAdLayout(activity);
            nativeAdLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            nativeAdLayout.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(activity);
            LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.fb_native_layout, nativeAdLayout, false);
            native_ad_frame.addView(adView);

            LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
            AdOptionsView adOptionsView = new AdOptionsView(activity, preLoadedFacebookNative, nativeAdLayout);
            adChoicesContainer.removeAllViews();
            adChoicesContainer.addView(adOptionsView, 0);

            MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
            TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
            MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
            TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
            TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
            TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
            Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
            nativeAdTitle.setText(preLoadedFacebookNative.getAdvertiserName());
            nativeAdBody.setText(preLoadedFacebookNative.getAdBodyText());
            nativeAdSocialContext.setText(preLoadedFacebookNative.getAdSocialContext());
            nativeAdCallToAction.setVisibility(preLoadedFacebookNative.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
            nativeAdCallToAction.setText(preLoadedFacebookNative.getAdCallToAction());
            sponsoredLabel.setText(preLoadedFacebookNative.getSponsoredTranslation());

            try {
                if (Placements.CTA_BTN_COLOR != null && !Placements.CTA_BTN_COLOR.isEmpty()) {
                    int color = Color.parseColor(Placements.CTA_BTN_COLOR);
                    nativeAdCallToAction.setBackgroundTintList(ColorStateList.valueOf(color));
                }
            }catch (Exception e){}


            List<View> clickableViews = new ArrayList<>();
            clickableViews.add(nativeAdTitle);
            clickableViews.add(nativeAdCallToAction);

            preLoadedFacebookNative.registerViewForInteraction(adView, nativeAdMedia, nativeAdIcon, clickableViews);
            if (reload) {
                preLoadNativeAd(activity, id, null);
            }
        }
    }

}
