package com.example.shopdemo

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.example.shopdemo.data.MockLatency
import com.example.shopdemo.data.ServiceLocator
import com.example.shopdemo.testing.LaunchConfig
import com.example.shopdemo.ui.login.LoginActivity
import com.example.shopdemo.ui.main.MainActivity

/** Seed data from data/Services.kt. */
object TestData {
    const val EMAIL = "demo@shop.com"
    const val PASSWORD = "password123"
    const val LOCKED_EMAIL = "locked@shop.com"
    const val SUCCESS_CARD = "4242424242424242"
    const val DECLINED_CARD = "4000000000000002"
}

/** Instrumented tests share the app process: reset in-memory state before every test. */
fun resetApp(latencyMs: Long = 0) {
    ServiceLocator.reset()
    MockLatency.millis = latencyMs
}

fun launchLogin(): ActivityScenario<LoginActivity> = ActivityScenario.launch(LoginActivity::class.java)

/** Starts signed in, skipping the login screen (same extras the macrobenchmarks use). */
fun launchStoreSignedIn(): ActivityScenario<MainActivity> = ActivityScenario.launch(
    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .putExtra(LaunchConfig.EXTRA_LOGGED_IN, true)
        .putExtra(LaunchConfig.EXTRA_LATENCY_MS, 0L),
)
