package com.example.shopdemo.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.shopdemo.data.Product
import com.example.shopdemo.data.ServiceLocator
import com.example.shopdemo.data.toCurrency
import com.example.shopdemo.testing.TestTags

@Composable
fun DiscoverScreen(onProductClick: (Product) -> Unit) {
    val viewModel: DiscoverViewModel = viewModel(
        factory = viewModelFactory { initializer { DiscoverViewModel(ServiceLocator.catalog) } },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiscoverContent(
        state = state,
        onQueryChanged = viewModel::onQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onProductClick = onProductClick,
    )
}

/** Stateless — rendered directly by Compose UI tests with fake state. */
@Composable
fun DiscoverContent(
    state: DiscoverUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onProductClick: (Product) -> Unit,
) {
    Column(Modifier.fillMaxSize().testTag(TestTags.Discover.SCREEN)) {
        Text(
            "Discover",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search products") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(TestTags.Discover.SEARCH),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            items(state.categories, key = { it.id }) { category ->
                FilterChip(
                    selected = category.id == state.selectedCategory,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(category.name) },
                    modifier = Modifier.testTag(TestTags.Discover.category(category.id)),
                )
            }
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.testTag(TestTags.Discover.LOADING))
            }
            state.products.isEmpty() -> Text(
                "No products found",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp).testTag(TestTags.Discover.EMPTY),
            )
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(state.products, key = { it.id }) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = { Text(product.price.toCurrency()) },
                        modifier = Modifier
                            .clickable { onProductClick(product) }
                            .testTag(TestTags.Discover.product(product.id)),
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
