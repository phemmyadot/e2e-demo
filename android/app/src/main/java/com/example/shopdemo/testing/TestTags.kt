package com.example.shopdemo.testing

import android.content.Intent
import com.example.shopdemo.data.MockLatency
import com.example.shopdemo.data.ServiceLocator
import com.example.shopdemo.data.User

/**
 * Stable Compose test tags, shared by Compose UI tests and UI Automator (exposed as resource
 * IDs via `testTagsAsResourceId`). Same names as the iOS `AID` enum.
 * The View-based login screen uses regular view IDs (R.id.login_*) instead.
 */
object TestTags {
    object Discover {
        const val SCREEN = "discover.screen"
        const val SEARCH = "discover.search"
        const val EMPTY = "discover.empty"
        const val LOADING = "discover.loading"
        fun category(id: String) = "discover.category.$id"
        fun product(id: String) = "discover.product.$id"
    }

    object Product {
        const val SCREEN = "product.screen"
        const val NAME = "product.name"
        const val PRICE = "product.price"
        const val DESCRIPTION = "product.description"
        const val ADD_TO_CART = "product.addToCart"
        const val ADDED_TOAST = "product.addedToast"
        const val BACK = "product.back"
    }

    object Cart {
        const val SCREEN = "cart.screen"
        const val EMPTY = "cart.empty"
        const val SUBTOTAL = "cart.subtotal"
        const val CHECKOUT = "cart.checkout"
        fun row(id: String) = "cart.row.$id"
        fun qty(id: String) = "cart.qty.$id"
        fun increment(id: String) = "cart.increment.$id"
        fun decrement(id: String) = "cart.decrement.$id"
    }

    object Checkout {
        const val SCREEN = "checkout.screen"
        const val FULL_NAME = "checkout.fullName"
        const val ADDRESS = "checkout.address"
        const val CITY = "checkout.city"
        const val ZIP = "checkout.zip"
        const val CARD_NUMBER = "checkout.cardNumber"
        const val EXPIRY = "checkout.expiry"
        const val CVC = "checkout.cvc"
        const val TOTAL = "checkout.total"
        const val PLACE_ORDER = "checkout.placeOrder"
        const val ERROR = "checkout.error"
    }

    object Confirmation {
        const val SCREEN = "confirmation.screen"
        const val ORDER_NUMBER = "confirmation.orderNumber"
        const val DONE = "confirmation.done"
    }

    object Profile {
        const val SCREEN = "profile.screen"
        const val EMAIL = "profile.email"
        const val SIGN_OUT = "profile.signOut"
    }

    object Tab {
        const val DISCOVER = "tab.discover"
        const val CART = "tab.cart"
        const val PROFILE = "tab.profile"
    }
}

/**
 * Intent extras that put the app in a known state — the Android equivalent of the iOS
 * `-uitest.*` launch arguments. Used by macrobenchmarks, which run in a separate process.
 */
object LaunchConfig {
    const val EXTRA_LOGGED_IN = "uitest.loggedIn"
    const val EXTRA_LATENCY_MS = "uitest.latencyMs"

    fun apply(intent: Intent) {
        if (intent.hasExtra(EXTRA_LATENCY_MS)) {
            MockLatency.millis = intent.getLongExtra(EXTRA_LATENCY_MS, MockLatency.millis)
        }
        if (intent.getBooleanExtra(EXTRA_LOGGED_IN, false)) {
            ServiceLocator.session.forceSignIn(User.DEMO)
        }
    }
}
