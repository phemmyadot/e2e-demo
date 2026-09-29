package com.example.shopdemo

import com.example.shopdemo.data.AuthException
import com.example.shopdemo.data.CartItem
import com.example.shopdemo.data.CheckoutException
import com.example.shopdemo.data.MockAuthService
import com.example.shopdemo.data.MockCatalogService
import com.example.shopdemo.data.MockCheckoutService
import com.example.shopdemo.data.MockLatency
import com.example.shopdemo.data.OrderIds
import com.example.shopdemo.data.PaymentCard
import com.example.shopdemo.data.ShippingAddress
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class MockServicesTest {
    private val items = listOf(CartItem(MockCatalogService.PRODUCTS[0], quantity = 2))
    private val shipping = ShippingAddress("Ada", "1 Way", "London", "10001")
    private val goodCard = PaymentCard("4242 4242 4242 4242", "12/30", "123")

    @Before
    fun setUp() {
        MockLatency.millis = 0
    }

    @Test
    fun catalogFiltersByCategoryAndQuery() = runTest {
        val catalog = MockCatalogService()
        assertEquals(listOf("p1", "p2"), catalog.products("", "audio").map { it.id })
        assertEquals(listOf("p3"), catalog.products("  WATCH ", null).map { it.id })
        assertTrue(catalog.products("speaker", "home").isEmpty())
    }

    @Test
    fun authRejectsWrongPasswordAndLockedAccount() = runTest {
        val auth = MockAuthService()
        assertEquals("demo@shop.com", auth.signIn(" Demo@Shop.com", "password123").email)
        assertThrows(AuthException.InvalidCredentials::class.java) {
            kotlinx.coroutines.runBlocking { auth.signIn("demo@shop.com", "nope") }
        }
        assertThrows(AuthException.Locked::class.java) {
            kotlinx.coroutines.runBlocking { auth.signIn("locked@shop.com", "password123") }
        }
    }

    @Test
    fun checkoutValidatesShippingAndCard() = runTest {
        val checkout = MockCheckoutService()
        assertThrows(CheckoutException.MissingShipping::class.java) {
            kotlinx.coroutines.runBlocking { checkout.placeOrder(items, shipping.copy(city = " "), goodCard) }
        }
        assertThrows(CheckoutException.InvalidCard::class.java) {
            kotlinx.coroutines.runBlocking { checkout.placeOrder(items, shipping, goodCard.copy(number = "4242")) }
        }
        assertThrows(CheckoutException.Declined::class.java) {
            kotlinx.coroutines.runBlocking {
                checkout.placeOrder(items, shipping, goodCard.copy(number = MockCheckoutService.DECLINED_CARD))
            }
        }
    }

    /**
     * Static mocking with Mockito's inline mock maker (default since Mockito 5). This is the
     * modern replacement for PowerMock's `mockStatic`: PowerMock 2.x only supports Mockito 2/3
     * and can't share a classpath with Mockito 5.
     */
    @Test
    fun successfulOrderUsesGeneratedOrderId() = runTest {
        Mockito.mockStatic(OrderIds::class.java).use { orderIds ->
            orderIds.`when`<String> { OrderIds.next() }.thenReturn("ORD-123456")

            val order = MockCheckoutService().placeOrder(items, shipping, goodCard)

            assertEquals("ORD-123456", order.id)
            assertEquals("399.98".toBigDecimal(), order.total)
            orderIds.verify { OrderIds.next() }
        }
    }
}
