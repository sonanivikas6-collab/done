package com.fitflow.app.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fitflow.app.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, AdConfig.NATIVE_AD_UNIT_ID)
            .forNativeAd { ad -> nativeAd = ad }
            .build()
        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    val ad = nativeAd
    if (ad != null) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            factory = { ctx -> buildNativeAdView(ctx, ad) }
        )
    }
}

private fun buildNativeAdView(context: Context, nativeAd: NativeAd): NativeAdView {
    val inflater = LayoutInflater.from(context)
    val adView = inflater.inflate(R.layout.native_ad_card, null) as NativeAdView

    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
    val ctaView = adView.findViewById<Button>(R.id.ad_call_to_action)

    headlineView.text = nativeAd.headline
    adView.headlineView = headlineView

    if (nativeAd.body != null) {
        bodyView.text = nativeAd.body
        bodyView.visibility = View.VISIBLE
    } else {
        bodyView.visibility = View.GONE
    }
    adView.bodyView = bodyView

    if (nativeAd.icon != null) {
        iconView.setImageDrawable(nativeAd.icon?.drawable)
        iconView.visibility = View.VISIBLE
    } else {
        iconView.visibility = View.GONE
    }
    adView.iconView = iconView

    if (nativeAd.callToAction != null) {
        ctaView.text = nativeAd.callToAction
        ctaView.visibility = View.VISIBLE
    } else {
        ctaView.visibility = View.GONE
    }
    adView.callToActionView = ctaView

    adView.setNativeAd(nativeAd)

    return adView
}
