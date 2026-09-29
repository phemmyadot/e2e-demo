package com.example.shopdemo.data

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * All backend calls are mocked in-process with a small artificial delay so tests exercise
 * real loading states without any network dependency.
 */
object MockLatency {
    @Volatile var millis: Long = 600
    suspend fun await() = delay(millis)
}

// region Auth

sealed class AuthException(message: String) : Exception(message) {
    class InvalidCredentials : AuthException("Invalid email or password")
    class Locked : AuthException("This account is locked")
}

interface AuthService {
    suspend fun signIn(email: String, password: String): User
}

class MockAuthService : AuthService {
    override suspend fun signIn(email: String, password: String): User {
        MockLatency.await()
        val normalized = email.trim().lowercase()
        if (ACCOUNTS[normalized] != password) throw AuthException.InvalidCredentials()
        if (normalized == LOCKED_EMAIL) throw AuthException.Locked()
        return User.DEMO.copy(email = normalized)
    }

    companion object {
        const val LOCKED_EMAIL = "locked@shop.com"
        val ACCOUNTS = mapOf(
            "demo@shop.com" to "password123",
            LOCKED_EMAIL to "password123",
        )
    }
}

// endregion

// region Catalog

interface CatalogService {
    suspend fun categories(): List<Category>
    suspend fun products(query: String, categoryId: String?): List<Product>
}

class MockCatalogService : CatalogService {
    override suspend fun categories(): List<Category> = CATEGORIES

    override suspend fun products(query: String, categoryId: String?): List<Product> {
        MockLatency.await()
        val q = query.trim().lowercase()
        return PRODUCTS.filter { p ->
            (categoryId == null || categoryId == "all" || p.categoryId == categoryId) &&
                (q.isEmpty() || p.name.lowercase().contains(q))
        }
    }

    companion object {
        val CATEGORIES = listOf(
            Category("all", "All"),
            Category("audio", "Audio"),
            Category("wearables", "Wearables"),
            Category("home", "Home"),
        )

        val PRODUCTS = listOf(
            Product("p1", "Wireless Headphones", "audio", "199.99".toBigDecimal(),
                "Noise-cancelling over-ear headphones with 30h battery."),
            Product("p2", "Bluetooth Speaker", "audio", "89.00".toBigDecimal(),
                "Portable waterproof speaker with deep bass."),
            Product("p3", "Smart Watch", "wearables", "249.00".toBigDecimal(),
                "Fitness tracking, notifications and GPS."),
            Product("p4", "Fitness Band", "wearables", "59.50".toBigDecimal(),
                "Lightweight tracker for steps, sleep and heart rate."),
            Product("p5", "Desk Lamp", "home", "45.00".toBigDecimal(),
                "Dimmable LED lamp with warm and cool modes."),
            Product("p6", "Coffee Maker", "home", "129.00".toBigDecimal(),
                "Programmable drip coffee maker, 12 cups."),
        )
    }
}

// endregion

// region Checkout

sealed class CheckoutException(message: String) : Exception(message) {
    class MissingShipping : CheckoutException("Please complete your shipping details")
    class InvalidCard : CheckoutException("Please enter a valid card")
    class Declined : CheckoutException("Your card was declined")
}

interface CheckoutService {
    suspend fun placeOrder(items: List<CartItem>, shipping: ShippingAddress, card: PaymentCard): Order
}

/** Order numbers come from a static generator (the kind of code PowerMock was used for). */
object OrderIds {
    @JvmStatic
    fun next(): String = "ORD-${Random.nextInt(100_000, 1_000_000)}"
}

class MockCheckoutService : CheckoutService {
    override suspend fun placeOrder(
        items: List<CartItem>,
        shipping: ShippingAddress,
        card: PaymentCard,
    ): Order {
        val shippingFields = listOf(shipping.fullName, shipping.address, shipping.city, shipping.zip)
        if (shippingFields.any { it.isBlank() }) throw CheckoutException.MissingShipping()

        val digits = card.number.filter(Char::isDigit)
        if (digits.length != 16 || card.expiry.length < 4 || card.cvc.length < 3) {
            throw CheckoutException.InvalidCard()
        }

        MockLatency.await()
        if (digits == DECLINED_CARD) throw CheckoutException.Declined()

        val total = items.fold(java.math.BigDecimal.ZERO) { acc, item -> acc + item.lineTotal }
        return Order(id = OrderIds.next(), total = total)
    }

    companion object {
        const val SUCCESS_CARD = "4242424242424242"
        const val DECLINED_CARD = "4000000000000002"
    }
}

// endregion
