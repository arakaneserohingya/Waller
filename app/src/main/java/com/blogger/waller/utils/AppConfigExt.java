package com.blogger.waller.utils;

import com.blogger.waller.AppConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import dreamspace.ads.sdk.data.AdNetworkType;

public class AppConfigExt {

    /* --------------- DONE EDIT CODE BELOW ------------------------------------------------------ */

    // define static variable for all config class
    public static AppConfig.General general = new AppConfig.General();
    public static AppConfig.Ads ads = new AppConfig.Ads();
    public static AppConfig.Notification notification = new AppConfig.Notification();

    // Set data from remote config
    public static void setFromRemoteConfig(FirebaseRemoteConfig remote) {

        // fetch General Config with data from remote config
        if (!remote.getString("blogger_url").isEmpty())
            AppConfig.general.blogger_url = remote.getString("blogger_url");
        if (!remote.getString("access_key").isEmpty())
            AppConfig.general.access_key = remote.getString("access_key");

        if (!remote.getString("sort_category_alphabetically").isEmpty()) {
            AppConfig.general.sort_category_alphabetically = Boolean.parseBoolean(remote.getString("sort_category_alphabetically"));
        }

        if (!remote.getString("non_playstore_market_android").isEmpty()) {
            AppConfig.general.non_playstore_market_android = remote.getString("non_playstore_market_android");
        }

        if (!remote.getString("open_link_in_app").isEmpty()) {
            AppConfig.general.open_link_in_app = Boolean.parseBoolean(remote.getString("open_link_in_app"));
        }

        if (!remote.getString("listing_pagination_count").isEmpty()) {
            try {
                AppConfig.general.listing_pagination_count = Integer.parseInt(remote.getString("listing_pagination_count"));
            } catch (Exception e) {
            }
        }

        if (!remote.getString("privacy_policy_url").isEmpty()) {
            AppConfig.general.privacy_policy_url = remote.getString("privacy_policy_url");
        }
        if (!remote.getString("more_apps_url").isEmpty()) {
            AppConfig.general.more_apps_url = remote.getString("more_apps_url");
        }
        if (!remote.getString("contact_us_url").isEmpty()) {
            AppConfig.general.contact_us_url = remote.getString("contact_us_url");
        }

        // fetch Ads Config with data from remote config
        if (!remote.getString("ad_enable").isEmpty()) {
            AppConfig.ads.ad_enable = Boolean.parseBoolean(remote.getString("ad_enable"));
        }
        if (!remote.getString("ad_network").isEmpty()) {
            try {
                AppConfig.ads.ad_network = AdNetworkType.valueOf(remote.getString("ad_network"));
            } catch (Exception e) {
            }
        }
        if (!remote.getString("ad_enable_gdpr").isEmpty()) {
            AppConfig.ads.ad_enable_gdpr = Boolean.parseBoolean(remote.getString("ad_enable_gdpr"));
        }

        if (!remote.getString("ad_main_banner").isEmpty()) {
            AppConfig.ads.ad_main_banner = Boolean.parseBoolean(remote.getString("ad_main_banner"));
        }
        if (!remote.getString("ad_main_interstitial").isEmpty()) {
            AppConfig.ads.ad_main_interstitial = Boolean.parseBoolean(remote.getString("ad_main_interstitial"));
        }
        if (!remote.getString("ad_listing_details_banner").isEmpty()) {
            AppConfig.ads.ad_listing_details_banner = Boolean.parseBoolean(remote.getString("ad_listing_details_banner"));
        }
        if (!remote.getString("ad_news_details_banner").isEmpty()) {
            AppConfig.ads.ad_news_details_banner = Boolean.parseBoolean(remote.getString("ad_news_details_banner"));
        }
        if (!remote.getString("ad_category_details_banner").isEmpty()) {
            AppConfig.ads.ad_category_details_banner = Boolean.parseBoolean(remote.getString("ad_category_details_banner"));
        }
        if (!remote.getString("ad_search_banner").isEmpty()) {
            AppConfig.ads.ad_search_banner = Boolean.parseBoolean(remote.getString("ad_search_banner"));
        }

        if (!remote.getString("ad_inters_interval").isEmpty()) {
            try {
                AppConfig.ads.ad_inters_interval = Integer.parseInt(remote.getString("ad_inters_interval"));
            } catch (Exception e) {
            }
        }

        if (!remote.getString("ad_admob_publisher_id").isEmpty()) {
            AppConfig.ads.ad_admob_publisher_id = remote.getString("ad_admob_publisher_id");
        }
        if (!remote.getString("ad_admob_banner_unit_id").isEmpty()) {
            AppConfig.ads.ad_admob_banner_unit_id = remote.getString("ad_admob_banner_unit_id");
        }
        if (!remote.getString("ad_admob_interstitial_unit_id").isEmpty()) {
            AppConfig.ads.ad_admob_interstitial_unit_id = remote.getString("ad_admob_interstitial_unit_id");
        }

        if (!remote.getString("ad_fan_banner_unit_id").isEmpty()) {
            AppConfig.ads.ad_fan_banner_unit_id = remote.getString("ad_fan_banner_unit_id");
        }
        if (!remote.getString("ad_fan_interstitial_unit_id").isEmpty()) {
            AppConfig.ads.ad_fan_banner_unit_id = remote.getString("ad_fan_banner_unit_id");
        }

        if (!remote.getString("ad_ironsource_app_key").isEmpty()) {
            AppConfig.ads.ad_ironsource_app_key = remote.getString("ad_ironsource_app_key");
        }
        if (!remote.getString("ad_ironsource_banner_unit_id").isEmpty()) {
            AppConfig.ads.ad_ironsource_banner_unit_id = remote.getString("ad_ironsource_banner_unit_id");
        }
        if (!remote.getString("ad_ironsource_interstitial_unit_id").isEmpty()) {
            AppConfig.ads.ad_ironsource_interstitial_unit_id = remote.getString("ad_ironsource_interstitial_unit_id");
        }

        if (!remote.getString("ad_unity_game_id").isEmpty()) {
            AppConfig.ads.ad_unity_game_id = remote.getString("ad_unity_game_id");
        }
        if (!remote.getString("ad_unity_banner_unit_id").isEmpty()) {
            AppConfig.ads.ad_unity_banner_unit_id = remote.getString("ad_unity_banner_unit_id");
        }
        if (!remote.getString("ad_unity_interstitial_unit_id").isEmpty()) {
            AppConfig.ads.ad_unity_interstitial_unit_id = remote.getString("ad_unity_interstitial_unit_id");
        }

        if (!remote.getString("ad_applovin_banner_unit_id").isEmpty()) {
            AppConfig.ads.ad_applovin_banner_unit_id = remote.getString("ad_applovin_banner_unit_id");
        }
        if (!remote.getString("ad_applovin_interstitial_unit_id").isEmpty()) {
            AppConfig.ads.ad_applovin_interstitial_unit_id = remote.getString("ad_applovin_interstitial_unit_id");
        }

        // fetch Notification Config with data from remote config
        if (!remote.getString("notif_one_signal_appid").isEmpty()) {
            AppConfig.notification.notif_one_signal_appid = remote.getString("notif_one_signal_appid");
        }
    }
}
