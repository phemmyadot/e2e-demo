package com.example.shopdemo.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.shopdemo.data.CartItem
import com.example.shopdemo.data.subtotal
import com.example.shopdemo.data.toCurrency
import com.example.shopdemo.testing.TestTags

/** Stateless — the cart lives in CartRepository; MainActivity wires the callbacks. */
@Composable
fun CartScreen(
    items: List<CartItem>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onCheckout: () -> Unit,
) {
    Column(Modifier.fillMaxSize().testTag(TestTags.Cart.SCREEN)) {
        Text("Cart", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(16.dp))

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.testTag(TestTags.Cart.EMPTY)) {
                    Text("Your cart is empty", style = MaterialTheme.typography.titleMedium)
                    Text("Browse Discover to add products.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            return@Column
        }

        LazyColumn(Modifier.weight(1f)) {
            items(items, key = { it.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.product.name) },
                    supportingContent = { Text(item.lineTotal.toCurrency()) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onDecrement(item.id) },
                                modifier = Modifier.testTag(TestTags.Cart.decrement(item.id)),
                            ) { Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease quantity") }
                            Text("${item.quantity}", modifier = Modifier.testTag(TestTags.Cart.qty(item.id)))
                            IconButton(
                                onClick = { onIncrement(item.id) },
                                modifier = Modifier.testTag(TestTags.Cart.increment(item.id)),
                            ) { Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase quantity") }
                        }
                    },
                    modifier = Modifier.testTag(TestTags.Cart.row(item.id)),
                )
                HorizontalDivider()
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text("Subtotal", style = MaterialTheme.typography.titleMedium)
            Text(
                items.subtotal().toCurrency(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag(TestTags.Cart.SUBTOTAL),
            )
        }
        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).testTag(TestTags.Cart.CHECKOUT),
        ) { Text("Checkout") }
    }
}
