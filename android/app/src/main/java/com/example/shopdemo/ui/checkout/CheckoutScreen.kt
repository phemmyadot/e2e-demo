package com.example.shopdemo.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.shopdemo.data.Order
import com.example.shopdemo.data.ServiceLocator
import com.example.shopdemo.data.toCurrency
import com.example.shopdemo.testing.TestTags
import java.math.BigDecimal

@Composable
fun CheckoutScreen(total: BigDecimal, onOrderPlaced: (Order) -> Unit) {
    val viewModel: CheckoutViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CheckoutViewModel(ServiceLocator.checkout, ServiceLocator.cart) }
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.placedOrder) { state.placedOrder?.let(onOrderPlaced) }

    CheckoutContent(
        state = state,
        total = total,
        onShippingChange = viewModel::updateShipping,
        onCardChange = viewModel::updateCard,
        onPlaceOrder = viewModel::placeOrder,
    )
}

@Composable
fun CheckoutContent(
    state: CheckoutUiState,
    total: BigDecimal,
    onShippingChange: ((com.example.shopdemo.data.ShippingAddress) -> com.example.shopdemo.data.ShippingAddress) -> Unit,
    onCardChange: ((com.example.shopdemo.data.PaymentCard) -> com.example.shopdemo.data.PaymentCard) -> Unit,
    onPlaceOrder: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding().testTag(TestTags.Checkout.SCREEN)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            Text("Checkout", style = MaterialTheme.typography.headlineLarge)

            Text("Shipping", style = MaterialTheme.typography.titleMedium)
            Field("Full name", state.shipping.fullName, TestTags.Checkout.FULL_NAME) { v -> onShippingChange { it.copy(fullName = v) } }
            Field("Address", state.shipping.address, TestTags.Checkout.ADDRESS) { v -> onShippingChange { it.copy(address = v) } }
            Field("City", state.shipping.city, TestTags.Checkout.CITY) { v -> onShippingChange { it.copy(city = v) } }
            Field("ZIP", state.shipping.zip, TestTags.Checkout.ZIP, KeyboardType.Number) { v -> onShippingChange { it.copy(zip = v) } }

            Text("Payment", style = MaterialTheme.typography.titleMedium)
            Field("Card number", state.card.number, TestTags.Checkout.CARD_NUMBER, KeyboardType.Number) { v -> onCardChange { it.copy(number = v) } }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("MM/YY", state.card.expiry, TestTags.Checkout.EXPIRY, modifier = Modifier.weight(1f)) { v -> onCardChange { it.copy(expiry = v) } }
                Field("CVC", state.card.cvc, TestTags.Checkout.CVC, KeyboardType.Number, Modifier.weight(1f)) { v -> onCardChange { it.copy(cvc = v) } }
            }
            Text(
                "Test cards: 4242 4242 4242 4242 (success), 4000 0000 0000 0002 (declined)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Total", style = MaterialTheme.typography.titleMedium)
                Text(total.toCurrency(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag(TestTags.Checkout.TOTAL))
            }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag(TestTags.Checkout.ERROR))
            }
        }

        Button(
            onClick = onPlaceOrder,
            enabled = !state.isPlacing,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag(TestTags.Checkout.PLACE_ORDER),
        ) {
            if (state.isPlacing) CircularProgressIndicator(Modifier.size(20.dp)) else Text("Place Order")
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    tag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.testTag(tag),
    )
}
