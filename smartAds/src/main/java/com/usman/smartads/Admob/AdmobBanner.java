package com.usman.smartads.Admob;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.usman.smartads.BuildConfig;

public class AdmobBanner {
    private static AdView adView = null;
    public static void preLoadBannerAd(Activity context, String id) {
        if (BuildConfig.DEBUG)
            id = "ca-app-pub-3940256099942544/6300978111";
        adView = new AdView(context);
        adView.setAdUnitId(id);
        adView.setAdSize(AdSize.BANNER);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void showBannerAd(Activity context,FrameLayout bannerAdLayout, String id, boolean reload) {
        if (adView!=null) {
            if (BuildConfig.DEBUG)
                id = "ca-app-pub-3940256099942544/6300978111";
            bannerAdLayout.setVisibility(View.VISIBLE);
            try {
                if (adView != null) {
                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            bannerAdLayout.removeAllViews();
            bannerAdLayout.addView(adView);
            if (reload)
                preLoadBannerAd(context,id);
        }
    }
    public static void loadBannerAd(FrameLayout bannerAdLayout, Activity context, String id, String position) {
        if (BuildConfig.DEBUG)
            id = "ca-app-pub-3940256099942544/2014213617";
        bannerAdLayout.setVisibility(View.VISIBLE);
        AdView adView = new AdView(context);
        adView.setAdSize(getAdSize(context, bannerAdLayout));
        adView.setAdUnitId(id);

        AdRequest.Builder builder = new AdRequest.Builder();
        Bundle extras = new Bundle();
        extras.putString("collapsible", position);
        builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);

        AdRequest adRequest = builder.build();
        adView.loadAd(adRequest);

        adView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                bannerAdLayout.removeAllViews();
                bannerAdLayout.addView(adView);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                bannerAdLayout.setVisibility(View.GONE);
            }
        });
    }

    private static AdSize getAdSize(Activity activity, FrameLayout bannerAdLayout) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = outMetrics.density;
        float adWidthPixels = bannerAdLayout.getWidth();
        if (adWidthPixels == 0) {
            adWidthPixels = outMetrics.widthPixels;
        }
        int adWidth = (int) (adWidthPixels / density);

        return com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

}