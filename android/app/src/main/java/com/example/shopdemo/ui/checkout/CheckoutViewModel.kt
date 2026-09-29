package com.example.shopdemo.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopdemo.data.CartRepository
import com.example.shopdemo.data.CheckoutService
import com.example.shopdemo.data.Order
import com.example.shopdemo.data.PaymentCard
import com.example.shopdemo.data.ShippingAddress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val shipping: ShippingAddress = ShippingAddress(),
    val card: PaymentCard = PaymentCard(),
    val isPlacing: Boolean = false,
    val error: String? = null,
    val placedOrder: Order? = null,
)

class CheckoutViewModel(
    private val checkout: CheckoutService,
    private val cart: CartRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CheckoutUiState())
    val state: StateFlow<CheckoutUiState> = _state.asStateFlow()

    fun updateShipping(transform: (ShippingAddress) -> ShippingAddress) =
        _state.update { it.copy(shipping = transform(it.shipping)) }

    fun updateCard(transform: (PaymentCard) -> PaymentCard) =
        _state.update { it.copy(card = transform(it.card)) }

    fun placeOrder() {
        if (_state.value.isPlacing) return
        _state.update { it.copy(isPlacing = true, error = null) }
        viewModelScope.launch {
            try {
                val current = _state.value
                val order = checkout.placeOrder(cart.items.value, current.shipping, current.card)
                cart.clear()
                _state.update { it.copy(isPlacing = false, placedOrder = order) }
            } catch (e: Exception) {
                _state.update { it.copy(isPlacing = false, error = e.message) }
            }
        }
    }
}
