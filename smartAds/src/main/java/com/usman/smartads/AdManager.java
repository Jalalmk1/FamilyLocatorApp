package com.usman.smartads;

import android.app.Activity;
import android.app.Application;
import android.widget.FrameLayout;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.usman.smartads.Admob.AdmobBanner;
import com.usman.smartads.Admob.AdmobInterstitial;
import com.usman.smartads.Admob.AdmobNative;
import com.usman.smartads.Facebook.FacebookBanner;
import com.usman.smartads.Facebook.FacebookInterstitial;
import com.usman.smartads.Facebook.FacebookMREC;
import com.usman.smartads.Facebook.FacebookNative;

public class AdManager {
    public static void init(Application application, Activity activity, FirebaseRemoteConfig remoteConfig, String firstPlacement, Runnable runnable) {
        MobileAds.initialize(application);
        //  AppLovinSdk.initializeSdk(activity);
        AudienceNetworkAds.initialize(application);
        AdSettings.setDataProcessingOptions(new String[]{});
        AdSettings.setTestMode(BuildConfig.DEBUG);
        Placements.getRemote(remoteConfig, new Runnable() {
            @Override
            public void run() {
                setAppOpenManager(application);
                preLoads(activity, runnable, firstPlacement);
            }
        });
    }

    public static void setAppOpenManager(Application application) {
        if (Placements.APP_OPEN_ID != null && Placements.APP_OPEN_ID.isPRELOAD() && Placements.APP_OPEN_ID.getID() != null && !Placements.APP_OPEN_ID.getID().isEmpty())
            new com.usman.smartads.Admob.AppOpenManager(application, Placements.APP_OPEN_ID.getID());
    }

    public static void preLoads(Activity activity, Runnable runnable, String firstPlacement) {
        if (Placements.INTERSTITIAL_ID_ADMOB != null && Placements.INTERSTITIAL_ID_ADMOB.getID() != null && Placements.INTERSTITIAL_ID_ADMOB.isPRELOAD())
            AdmobInterstitial.preLoadInterstitialAd(activity, Placements.INTERSTITIAL_ID_ADMOB.getID());

        if (Placements.NATIVE_ID_ADMOB != null && Placements.NATIVE_ID_ADMOB.getID() != null && Placements.NATIVE_ID_ADMOB.isPRELOAD()) {
            AdmobNative.preLoadNativeAd(activity, Placements.NATIVE_ID_ADMOB.getID(), (Placements.getPlacementByName(firstPlacement) != null && Placements.getPlacementByName(firstPlacement).getAD_NETWORK().equalsIgnoreCase("admob")) ? runnable : null);
        }

        if (Placements.BANNER_ID_ADMOB != null && Placements.BANNER_ID_ADMOB.getID() != null && Placements.BANNER_ID_ADMOB.isPRELOAD())
            AdmobBanner.preLoadBannerAd(activity, Placements.BANNER_ID_ADMOB.getID());

        if (Placements.INTERSTITIAL_ID_FACEBOOK != null && Placements.INTERSTITIAL_ID_FACEBOOK.getID() != null && Placements.INTERSTITIAL_ID_FACEBOOK.isPRELOAD())
            FacebookInterstitial.preLoadInterstitialAd(activity, Placements.INTERSTITIAL_ID_FACEBOOK.getID());


        if (Placements.NATIVE_ID_FACEBOOK != null && Placements.NATIVE_ID_FACEBOOK.getID() != null && Placements.NATIVE_ID_FACEBOOK.isPRELOAD()) {
            FacebookNative.preLoadNativeAd(activity, Placements.NATIVE_ID_FACEBOOK.getID(), (Placements.getPlacementByName(firstPlacement) != null && Placements.getPlacementByName(firstPlacement).getAD_NETWORK().equalsIgnoreCase("facebook")) ? runnable : null);
        }

        if (Placements.BANNER_ID_FACEBOOK != null && Placements.BANNER_ID_FACEBOOK.isPRELOAD())
            FacebookBanner.preLoadBannerAd(activity, Placements.BANNER_ID_FACEBOOK.getID());

        if (Placements.MREC_ID_FACEBOOK != null && Placements.MREC_ID_FACEBOOK.isPRELOAD())
            FacebookMREC.preLoadMRECAd(activity, Placements.MREC_ID_FACEBOOK.getID());
    }

    public static void showInterstitialAd(Activity activity, String placementName, Runnable runnable) {
        Placement placement = Placements.getPlacementByName(placementName);
        if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("admob") && Placements.INTERSTITIAL_ID_ADMOB.getID() != null)
            AdmobInterstitial.showInterstitialAd(activity, Placements.INTERSTITIAL_ID_ADMOB.getID(), placement.isRELOAD(), placement.isSHOW_LOADING(), runnable);
//        else if (placement!=null && placement.getAD_NETWORK()!=null && placement.getAD_NETWORK().equalsIgnoreCase("applovin") && Placements.INTERSTITIAL_ID_APPLOVIN.getID()!=null)
//            ApplovinInterstitial.showInterstitialAd(activity,Placements.INTERSTITIAL_ID_APPLOVIN.getID(),placement.isRELOAD(),placement.isSHOW_LOADING(),runnable);
        else if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("facebook") && Placements.INTERSTITIAL_ID_FACEBOOK.getID() != null)
            FacebookInterstitial.showInterstitialAd(activity, Placements.INTERSTITIAL_ID_FACEBOOK.getID(), placement.isRELOAD(), placement.isSHOW_LOADING(), runnable);
        else if (runnable != null)
            runnable.run();
    }

    public static void showNativeAd(Activity activity, FrameLayout frameLayout, String placementName, boolean showMedia) {
        Placement placement = Placements.getPlacementByName(placementName);
        if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("admob") && Placements.NATIVE_ID_ADMOB.getID() != null)
            AdmobNative.showNativeAd(activity, frameLayout, Placements.NATIVE_ID_ADMOB.getID(), showMedia, placement.isRELOAD());
//        else if (placement!=null && placement.getAD_NETWORK()!=null && placement.getAD_NETWORK().equalsIgnoreCase("applovin") && Placements.NATIVE_ID_APPLOVIN.getID()!=null)
//            ApplovinNative.showNativeAd(activity,frameLayout,Placements.NATIVE_ID_APPLOVIN.getID(),placement.isRELOAD());
        else if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("facebook") && Placements.NATIVE_ID_FACEBOOK.getID() != null)
            FacebookNative.showNativeAd(activity, frameLayout, Placements.NATIVE_ID_FACEBOOK.getID(), showMedia, placement.isRELOAD());
    }

    public static void showBannerAd(Activity activity, FrameLayout frameLayout, String placementName) {
        Placement placement = Placements.getPlacementByName(placementName);
        if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("admob") && Placements.BANNER_ID_ADMOB.getID() != null)
            AdmobBanner.showBannerAd(activity, frameLayout, Placements.BANNER_ID_ADMOB.getID(), placement.isRELOAD());
        else if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("collapse") && Placements.COLAPSABLE_BANNER_ID_ADMOB.getID() != null)
            AdmobBanner.loadBannerAd(frameLayout, activity, Placements.COLAPSABLE_BANNER_ID_ADMOB.getID(), placement.getCTR());

//        else if (placement!=null && placement.getAD_NETWORK()!=null && placement.getAD_NETWORK().equalsIgnoreCase("applovin") && Placements.BANNER_ID_APPLOVIN.getID()!=null)
//            ApplovinBanner.showBannerAd(activity,frameLayout,Placements.BANNER_ID_APPLOVIN.getID(),placement.isRELOAD());
        else if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("facebook") && Placements.BANNER_ID_FACEBOOK.getID() != null)
            FacebookBanner.showBannerAd(activity, frameLayout, Placements.BANNER_ID_FACEBOOK.getID(), placement.isRELOAD());
    }

    public static void showMRECAd(Activity activity, FrameLayout frameLayout, String placementName) {
        Placement placement = Placements.getPlacementByName(placementName);
//        if (placement!=null && placement.getAD_NETWORK()!=null && placement.getAD_NETWORK().equalsIgnoreCase("applovin") && Placements.MREC_ID_APPLOVIN.getID()!=null)
//            ApplovinBanner.showBannerAd(activity,frameLayout,Placements.MREC_ID_APPLOVIN.getID(),placement.isRELOAD());
        //       else
        if (placement != null && placement.getAD_NETWORK() != null && placement.getAD_NETWORK().equalsIgnoreCase("facebook") && Placements.MREC_ID_FACEBOOK.getID() != null)
            FacebookMREC.showMRECAd(activity, frameLayout, Placements.MREC_ID_FACEBOOK.getID(), placement.isRELOAD());

    }
}
