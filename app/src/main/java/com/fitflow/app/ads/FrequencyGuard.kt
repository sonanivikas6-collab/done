package com.fitflow.app.ads

import android.content.Context
import android.content.SharedPreferences

class FrequencyGuard(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitflow_ad_frequency", Context.MODE_PRIVATE)

    fun canShowInterstitial(minGapMillis: Long = DEFAULT_INTERSTITIAL_GAP_MS): Boolean {
        val last = prefs.getLong(KEY_LAST_INTERSTITIAL, 0L)
        val now = System.currentTimeMillis()
        return (now - last) >= minGapMillis
    }

    fun recordInterstitialShown() {
        prefs.edit().putLong(KEY_LAST_INTERSTITIAL, System.currentTimeMillis()).apply()
    }

    companion object {
        const val DEFAULT_INTERSTITIAL_GAP_MS = 3 * 60 * 1000L
        const val NATIVE_AD_EVERY_N_ITEMS = 6
        private const val KEY_LAST_INTERSTITIAL = "last_interstitial_shown_at"
    }
}
