package com.example.shopdemo.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal

class SessionRepository(private val auth: AuthService) {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    val isLoggedIn: Boolean get() = _user.value != null

    suspend fun signIn(email: String, password: String) {
        _user.value = auth.signIn(email, password)
    }

    fun signOut() {
        _user.value = null
    }

    /** Test hook: start a session without going through the login UI. */
    fun forceSignIn(user: User) {
        _user.value = user
    }
}

class CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    val count: Int get() = _items.value.sumOf { it.quantity }
    val subtotal: BigDecimal get() = _items.value.subtotal()

    fun add(product: Product) = _items.update { items ->
        if (items.any { it.id == product.id }) {
            items.map { if (it.id == product.id) it.copy(quantity = it.quantity + 1) else it }
        } else {
            items + CartItem(product, 1)
        }
    }

    fun increment(id: String) = _items.update { items ->
        items.map { if (it.id == id) it.copy(quantity = it.quantity + 1) else it }
    }

    fun decrement(id: String) = _items.update { items ->
        items.mapNotNull {
            when {
                it.id != id -> it
                it.quantity > 1 -> it.copy(quantity = it.quantity - 1)
                else -> null
            }
        }
    }

    fun clear() {
        _items.value = emptyList()
    }
}

fun List<CartItem>.subtotal(): BigDecimal = fold(BigDecimal.ZERO) { acc, item -> acc + item.lineTotal }
