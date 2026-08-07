package com.fitflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitflow.app.ads.InterstitialAdManager
import com.fitflow.app.ui.AppViewModel
import com.fitflow.app.ui.FitFlowNavHost
import com.fitflow.app.ui.OnboardingViewModel
import com.fitflow.app.ui.SettingsViewModel
import com.fitflow.app.ui.ThemeMode
import com.fitflow.app.ui.theme.FitFlowTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()
    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    lateinit var interstitialAdManager: InterstitialAdManager
        private set

    /**
     * Requests the step-counter sensor permission and (on Android 13+) the
     * notification permission the first time the app runs, right after the
     * user finishes onboarding. Results are ignored - the app degrades
     * gracefully (dashboard shows "sensor not available") if denied.
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results ignored: app degrades gracefully if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        interstitialAdManager = InterstitialAdManager(this)
        interstitialAdManager.preload()

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val darkTheme = when (settingsState.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            FitFlowTheme(darkTheme = darkTheme) {
                FitFlowNavHost(
                    viewModel = viewModel,
                    onboardingViewModel = onboardingViewModel,
                    settingsViewModel = settingsViewModel,
                    activity = this,
                    onOnboardingFinished = { requestNeededPermissions() }
                )
            }
        }
    }

    fun requestNeededPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
