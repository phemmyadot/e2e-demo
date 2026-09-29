package com.example.shopdemo.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shopdemo.data.CatalogService
import com.example.shopdemo.data.Category
import com.example.shopdemo.data.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val query: String = "",
    val selectedCategory: String = "all",
    val categories: List<Category> = emptyList(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
)

class DiscoverViewModel(private val catalog: CatalogService) : ViewModel() {
    private val _state = MutableStateFlow(DiscoverUiState())
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch { _state.update { it.copy(categories = catalog.categories()) } }
        load()
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        load()
    }

    fun onCategorySelected(id: String) {
        _state.update { it.copy(selectedCategory = id) }
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true) }
        val (query, category) = _state.value.let { it.query to it.selectedCategory }
        loadJob = viewModelScope.launch {
            val products = catalog.products(query, category)
            _state.update { it.copy(products = products, isLoading = false) }
        }
    }
}
