package com.example.shopdemo.data

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

data class User(val id: String, val name: String, val email: String) {
    companion object {
        val DEMO = User(id = "u_1", name = "Demo Shopper", email = "demo@shop.com")
    }
}

data class Category(val id: String, val name: String)

data class Product(
    val id: String,
    val name: String,
    val categoryId: String,
    val price: BigDecimal,
    val summary: String,
)

data class CartItem(val product: Product, val quantity: Int) {
    val id: String get() = product.id
    val lineTotal: BigDecimal get() = product.price * quantity.toBigDecimal()
}

data class ShippingAddress(
    val fullName: String = "",
    val address: String = "",
    val city: String = "",
    val zip: String = "",
)

data class PaymentCard(val number: String = "", val expiry: String = "", val cvc: String = "")

data class Order(val id: String, val total: BigDecimal)

fun BigDecimal.toCurrency(): String = NumberFormat.getCurrencyInstance(Locale.US).format(this)
