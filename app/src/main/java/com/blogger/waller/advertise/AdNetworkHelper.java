package com.blogger.waller.advertise;

import android.app.Activity;
import android.content.Context;

import com.blogger.waller.AppConfig;
import com.blogger.waller.BuildConfig;
import com.blogger.waller.R;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.gdpr.GDPR;
import dreamspace.ads.sdk.gdpr.LegacyGDPR;
import dreamspace.ads.sdk.listener.AdBannerListener;

public class AdNetworkHelper {

    private Activity activity;
    private AdNetwork adNetwork;
    private LegacyGDPR legacyGDPR;
    private GDPR gdpr;

    public AdNetworkHelper(Activity activity) {
        this.activity = activity;
        adNetwork = new AdNetwork(activity);
        legacyGDPR = new LegacyGDPR(activity);
        gdpr = new GDPR(activity);
    }

    public void updateConsentStatus() {
        if (!AppConfig.ads.ad_enable || !AppConfig.ads.ad_enable_gdpr) return;
        gdpr.updateGDPRConsentStatus();
    }

    public static void init(Context context) {
        AdConfig.ad_enable = AppConfig.ads.ad_enable;
        AdConfig.debug_mode = BuildConfig.DEBUG;
        AdConfig.enable_gdpr = true;
        AdConfig.ad_network = AppConfig.ads.ad_network;
        AdConfig.ad_inters_interval = AppConfig.ads.ad_inters_interval;

        AdConfig.ad_admob_publisher_id = AppConfig.ads.ad_admob_publisher_id;
        AdConfig.ad_admob_banner_unit_id = AppConfig.ads.ad_admob_banner_unit_id;
        AdConfig.ad_admob_interstitial_unit_id = AppConfig.ads.ad_admob_interstitial_unit_id;

        AdConfig.ad_fan_banner_unit_id = AppConfig.ads.ad_fan_banner_unit_id;
        AdConfig.ad_fan_interstitial_unit_id = AppConfig.ads.ad_fan_interstitial_unit_id;

        AdConfig.ad_ironsource_app_key = AppConfig.ads.ad_ironsource_app_key;
        AdConfig.ad_ironsource_banner_unit_id = AppConfig.ads.ad_ironsource_banner_unit_id;
        AdConfig.ad_ironsource_interstitial_unit_id = AppConfig.ads.ad_ironsource_interstitial_unit_id;

        AdConfig.ad_unity_game_id = AppConfig.ads.ad_unity_game_id;
        AdConfig.ad_unity_banner_unit_id = AppConfig.ads.ad_unity_banner_unit_id;
        AdConfig.ad_unity_interstitial_unit_id = AppConfig.ads.ad_unity_interstitial_unit_id;

        AdConfig.ad_applovin_banner_unit_id = AppConfig.ads.ad_applovin_banner_unit_id;
        AdConfig.ad_applovin_interstitial_unit_id = AppConfig.ads.ad_applovin_interstitial_unit_id;

        AdNetwork.init(context);
    }

    public void loadBannerAd(boolean enable, AdBannerListener listener) {
        adNetwork.loadBannerAd(enable, activity.findViewById(R.id.ad_container), listener);
    }

    public void loadBannerAd(boolean enable) {
        adNetwork.loadBannerAd(enable, activity.findViewById(R.id.ad_container));
    }

    public void loadInterstitialAd(boolean enable) {
        adNetwork.loadInterstitialAd(enable);
    }

    public boolean showInterstitialAd(boolean enable) {
        return adNetwork.showInterstitialAd(enable);
    }

}
