package com.example.shopdemo.benchmark

import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

const val PACKAGE = "com.example.shopdemo"
private const val TIMEOUT = 5_000L

/**
 * UI Automator journeys shared by the baseline profile generator and the macrobenchmarks.
 * Compose test tags are visible as resource IDs because the app sets `testTagsAsResourceId`.
 */
fun Intent.signedInFast(): Intent = apply {
    putExtra("uitest.loggedIn", true)
    putExtra("uitest.latencyMs", 0L)
}

/** Waits for a resource ID and clicks it, failing with a clear message instead of an NPE. */
fun MacrobenchmarkScope.waitAndClick(res: String) {
    val node = device.wait(Until.findObject(By.res(res)), TIMEOUT)
        ?: error("UI Automator: '$res' not found within ${TIMEOUT}ms")
    node.click()
}

fun MacrobenchmarkScope.startSignedIn() {
    pressHome()
    startActivityAndWait { it.signedInFast() }
    device.wait(Until.hasObject(By.res("discover.product.p1")), TIMEOUT)
}

fun MacrobenchmarkScope.browseCatalog() {
    waitAndClick("discover.category.audio")
    waitAndClick("discover.category.all")
    val list = device.wait(Until.findObject(By.res("discover.product.p6")), TIMEOUT).parent
    list.scroll(Direction.DOWN, 1f)
    list.scroll(Direction.UP, 1f)
}

fun MacrobenchmarkScope.addToCartAndOpenCheckout() {
    waitAndClick("discover.product.p1")
    waitAndClick("product.addToCart")
    waitAndClick("product.back")
    waitAndClick("tab.cart")
    waitAndClick("cart.checkout")
    device.wait(Until.hasObject(By.res("checkout.screen")), TIMEOUT)
}
