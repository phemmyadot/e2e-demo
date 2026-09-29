package com.example.shopdemo.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates `app/src/release/generated/baselineProfiles/baseline-prof.txt`.
 * Run: ./gradlew :app:generateReleaseBaselineProfile  (needs an API 33+ emulator or device)
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE, includeInStartupProfile = true) {
        startSignedIn()
        browseCatalog()
        addToCartAndOpenCheckout()
    }
}
