package com.example.shopdemo.ui.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shopdemo.data.Product
import com.example.shopdemo.data.toCurrency
import com.example.shopdemo.testing.TestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(product: Product, onAddToCart: () -> Unit, onBack: () -> Unit) {
    var added by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.testTag(TestTags.Product.SCREEN),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag(TestTags.Product.BACK)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp)) {
                if (added) {
                    Text(
                        "Added to cart",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag(TestTags.Product.ADDED_TOAST),
                    )
                }
                Button(
                    onClick = { onAddToCart(); added = true },
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.Product.ADD_TO_CART),
                ) { Text("Add to Cart") }
            }
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            Text(
                product.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.testTag(TestTags.Product.NAME),
            )
            Text(
                product.price.toCurrency(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag(TestTags.Product.PRICE),
            )
            Text(
                product.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(TestTags.Product.DESCRIPTION),
            )
        }
    }
}
