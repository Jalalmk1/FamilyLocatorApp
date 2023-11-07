package com.usman.smartads.Facebook;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.usman.smartads.BuildConfig;

public class FacebookMREC {
    private static AdView adView = null;
    public static void preLoadMRECAd(Activity context, String id) {
        if (BuildConfig.DEBUG)
            id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
        adView = new AdView(context, id, AdSize.RECTANGLE_HEIGHT_250);
        adView.loadAd();
    }

    public static void showMRECAd(Activity context, FrameLayout bannerAdLayout, String id, boolean reload) {
        if (adView!=null) {
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
                preLoadMRECAd(context,id);
        }
    }

}