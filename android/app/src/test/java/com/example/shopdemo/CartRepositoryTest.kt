package com.example.shopdemo

import com.example.shopdemo.data.CartRepository
import com.example.shopdemo.data.MockCatalogService
import com.example.shopdemo.data.toCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartRepositoryTest {
    private val headphones = MockCatalogService.PRODUCTS[0] // $199.99
    private val speaker = MockCatalogService.PRODUCTS[1]    // $89.00
    private lateinit var cart: CartRepository

    @Before
    fun setUp() {
        cart = CartRepository()
    }

    @Test
    fun startsEmpty() {
        assertTrue(cart.items.value.isEmpty())
        assertEquals(0, cart.count)
    }

    @Test
    fun addingSameProductIncrementsQuantity() {
        cart.add(headphones)
        cart.add(headphones)

        assertEquals(1, cart.items.value.size)
        assertEquals(2, cart.count)
    }

    @Test
    fun subtotalSumsLineTotalsExactly() {
        cart.add(headphones)
        cart.add(speaker)
        cart.increment(speaker.id)

        assertEquals("377.99".toBigDecimal(), cart.subtotal)
        assertEquals("$377.99", cart.subtotal.toCurrency())
    }

    @Test
    fun decrementToZeroRemovesItem() {
        cart.add(speaker)
        cart.decrement(speaker.id)

        assertTrue(cart.items.value.isEmpty())
    }

    @Test
    fun unknownIdsAreIgnored() {
        cart.add(speaker)
        cart.increment("nope")
        cart.decrement("nope")

        assertEquals(1, cart.count)
    }
}
