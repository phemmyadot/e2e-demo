package com.example.shopdemo.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.shopdemo.data.Order
import com.example.shopdemo.data.toCurrency
import com.example.shopdemo.testing.TestTags

@Composable
fun ConfirmationScreen(order: Order, onDone: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().padding(24.dp).testTag(TestTags.Confirmation.SCREEN),
    ) {
        Text("Order confirmed", style = MaterialTheme.typography.headlineMedium)
        Text(
            order.id,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag(TestTags.Confirmation.ORDER_NUMBER),
        )
        Text("Total paid: ${order.total.toCurrency()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().testTag(TestTags.Confirmation.DONE)) {
            Text("Continue Shopping")
        }
    }
}
