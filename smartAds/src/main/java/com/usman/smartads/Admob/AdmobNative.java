package com.usman.smartads.Admob;

import static com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_BOTTOM_RIGHT;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.usman.smartads.BuildConfig;
import com.usman.smartads.Placements;
import com.usman.smartads.R;

public class AdmobNative {
    private static NativeAd preLoadedAdmobNative = null;
    public static boolean loadingNative = false;
    public static void preLoadNativeAd(Activity activity, String id, Runnable runnable) {
        if (!loadingNative && !id.isEmpty()) {
            loadingNative = true;
            if (BuildConfig.DEBUG)
                id = "ca-app-pub-3940256099942544/2247696110";
            VideoOptions videoOptions = new VideoOptions.Builder().setStartMuted(true).build();
            NativeAdOptions adOptions = new NativeAdOptions.Builder().setVideoOptions(videoOptions).setAdChoicesPlacement(ADCHOICES_BOTTOM_RIGHT).build();
            String finalId = id;
            AdLoader adLoader = new AdLoader.Builder(activity, finalId)
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(NativeAd NativeAd) {
                            loadingNative = false;
                            preLoadedAdmobNative = NativeAd;
                            Log.i("ADS_UTILS", "AdmobNative Loaded " + finalId);
                            if (runnable!=null)
                                runnable.run();
                        }
                    })
                    .withAdListener(new com.google.android.gms.ads.AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            loadingNative = false;
                            Log.i("ADS_UTILS", "Admob Native Failed " + finalId);
                            if (runnable!=null)
                                runnable.run();
                        }

                        @Override
                        public void onAdImpression() {
                            super.onAdImpression();
                        }
                    })
                    .withNativeAdOptions(adOptions)
                    .build();
            adLoader.loadAd(new AdRequest.Builder().build());
        }
        else {
            if (runnable!=null)
                runnable.run();
        }
    }

    public static void showNativeAd(Activity activity, FrameLayout native_ad_frame, String id, boolean showMedia, Boolean reload) {
        if (preLoadedAdmobNative != null) {
            native_ad_frame.setVisibility(View.VISIBLE);
            NativeAdView adView;
            if (showMedia) {
                adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_layout_new_withmedia, null);
                adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
                if (preLoadedAdmobNative.getMediaContent() != null)
                    adView.getMediaView().setMediaContent(preLoadedAdmobNative.getMediaContent());
            } else {
                adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.ad_layout_new_nomedia, null);
            }

            try {
                if (Placements.CTA_BTN_COLOR != null && !Placements.CTA_BTN_COLOR.isEmpty()) {
                    int color = Color.parseColor(Placements.CTA_BTN_COLOR);
                    ((Button) adView.findViewById(R.id.ad_call_to_action)).setBackgroundTintList(ColorStateList.valueOf(color));
                }
            }catch (Exception e){}

            adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
            adView.setBodyView(adView.findViewById(R.id.ad_body));
            adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
            adView.setIconView(adView.findViewById(R.id.ad_app_icon));

            ((TextView) adView.getHeadlineView()).setText(preLoadedAdmobNative.getHeadline());

            if (preLoadedAdmobNative.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(preLoadedAdmobNative.getBody());
            }
            if (preLoadedAdmobNative.getCallToAction() == null) {
                adView.getCallToActionView().setVisibility(View.GONE);
            } else {
                adView.getCallToActionView().setVisibility(View.VISIBLE);
                ((Button) adView.getCallToActionView()).setText(preLoadedAdmobNative.getCallToAction());
            }
            if (preLoadedAdmobNative.getIcon() == null) {
                adView.getIconView().setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        preLoadedAdmobNative.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
            adView.setNativeAd(preLoadedAdmobNative);
            VideoController vc = preLoadedAdmobNative.getMediaContent().getVideoController();
            if (vc.hasVideoContent()) {
                vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                    @Override
                    public void onVideoEnd() {
                        super.onVideoEnd();
                    }
                });
            }

            native_ad_frame.removeAllViews();
            native_ad_frame.addView(adView);
            native_ad_frame.setVisibility(View.VISIBLE);

            if (reload) {
                preLoadNativeAd(activity, id, null);
            }
        }
    }
}
