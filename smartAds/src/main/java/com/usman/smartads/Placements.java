package com.usman.smartads;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

public class Placements {
    public static Ad_ID APP_OPEN_ID ;
    public static Ad_ID BANNER_ID_ADMOB ;
    public static Ad_ID COLAPSABLE_BANNER_ID_ADMOB ;
    public static Ad_ID INTERSTITIAL_ID_ADMOB ;
    public static Ad_ID NATIVE_ID_ADMOB ;
    public static Ad_ID BANNER_ID_APPLOVIN  ;
    public static Ad_ID MREC_ID_APPLOVIN  ;
    public static Ad_ID INTERSTITIAL_ID_APPLOVIN ;
    public static Ad_ID NATIVE_ID_APPLOVIN ;
    public static Ad_ID BANNER_ID_FACEBOOK  ;
    public static Ad_ID MREC_ID_FACEBOOK  ;
    public static Ad_ID INTERSTITIAL_ID_FACEBOOK ;
    public static Ad_ID NATIVE_ID_FACEBOOK ;

    public static String CTA_BTN_COLOR ;

    public static FirebaseRemoteConfig REMOTE_CONFIG;

    public static void getRemote(FirebaseRemoteConfig fbRemoteConfig, Runnable runnable){
        FirebaseRemoteConfigSettings.Builder configBuilder = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1);
        fbRemoteConfig.setConfigSettingsAsync(configBuilder.build());
        fbRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Boolean> task) {
                try {
                    APP_OPEN_ID = new Gson().fromJson(fbRemoteConfig.getString("APP_OPEN_ID"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    BANNER_ID_APPLOVIN = new Gson().fromJson(fbRemoteConfig.getString("BANNER_ID_APPLOVIN"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    MREC_ID_APPLOVIN = new Gson().fromJson(fbRemoteConfig.getString("MREC_ID_APPLOVIN"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    INTERSTITIAL_ID_APPLOVIN = new Gson().fromJson(fbRemoteConfig.getString("INTERSTITIAL_ID_APPLOVIN"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    NATIVE_ID_APPLOVIN = new Gson().fromJson(fbRemoteConfig.getString("NATIVE_ID_APPLOVIN"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    BANNER_ID_FACEBOOK = new Gson().fromJson(fbRemoteConfig.getString("BANNER_ID_FACEBOOK"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    MREC_ID_FACEBOOK = new Gson().fromJson(fbRemoteConfig.getString("MREC_ID_FACEBOOK"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    INTERSTITIAL_ID_FACEBOOK = new Gson().fromJson(fbRemoteConfig.getString("INTERSTITIAL_ID_FACEBOOK"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    NATIVE_ID_FACEBOOK = new Gson().fromJson(fbRemoteConfig.getString("NATIVE_ID_FACEBOOK"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    BANNER_ID_ADMOB = new Gson().fromJson(fbRemoteConfig.getString("BANNER_ID_ADMOB"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    COLAPSABLE_BANNER_ID_ADMOB = new Gson().fromJson(fbRemoteConfig.getString("COLAPSABLE_BANNER_ID_ADMOB"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    INTERSTITIAL_ID_ADMOB = new Gson().fromJson(fbRemoteConfig.getString("INTERSTITIAL_ID_ADMOB"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    NATIVE_ID_ADMOB = new Gson().fromJson(fbRemoteConfig.getString("NATIVE_ID_ADMOB"), Ad_ID.class);
                }catch (Exception e){}
                try {
                    CTA_BTN_COLOR = fbRemoteConfig.getString("CTA_BTN_COLOR");
                }catch (Exception e){}

                Placements.REMOTE_CONFIG = fbRemoteConfig;
                if (runnable!=null)
                    runnable.run();
            }
        });
    }

    public static Placement getPlacementByName(String name){
        if (REMOTE_CONFIG==null)
            return new Placement();
       return new Gson().fromJson(REMOTE_CONFIG.getString(name), Placement.class);
    }

}
