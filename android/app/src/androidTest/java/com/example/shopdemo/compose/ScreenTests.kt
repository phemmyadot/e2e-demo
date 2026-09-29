package com.example.shopdemo.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shopdemo.data.CartItem
import com.example.shopdemo.data.MockCatalogService
import com.example.shopdemo.data.Order
import com.example.shopdemo.testing.TestTags
import com.example.shopdemo.ui.cart.CartScreen
import com.example.shopdemo.ui.checkout.CheckoutContent
import com.example.shopdemo.ui.checkout.CheckoutUiState
import com.example.shopdemo.ui.checkout.ConfirmationScreen
import com.example.shopdemo.ui.discover.DiscoverContent
import com.example.shopdemo.ui.discover.DiscoverUiState
import com.example.shopdemo.ui.theme.ShopDemoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Compose UI tests: each screen rendered in isolation with fake state — no ViewModel, no nav. */
@RunWith(AndroidJUnit4::class)
class DiscoverContentTest {
    @get:Rule val compose = createComposeRule()

    private val loaded = DiscoverUiState(
        categories = MockCatalogService.CATEGORIES,
        products = MockCatalogService.PRODUCTS,
        isLoading = false,
    )

    @Test
    fun rendersEveryProduct() {
        compose.setContent { ShopDemoTheme { DiscoverContent(loaded, {}, {}, {}) } }

        MockCatalogService.PRODUCTS.forEach {
            compose.onNodeWithTag(TestTags.Discover.product(it.id)).assertIsDisplayed()
        }
    }

    @Test
    fun showsLoadingAndEmptyStates() {
        var state by mutableStateOf(loaded.copy(isLoading = true))
        compose.setContent { ShopDemoTheme { DiscoverContent(state, {}, {}, {}) } }
        compose.onNodeWithTag(TestTags.Discover.LOADING).assertIsDisplayed()

        state = loaded.copy(products = emptyList())
        compose.onNodeWithTag(TestTags.Discover.EMPTY).assertTextEquals("No products found")
    }

    @Test
    fun forwardsUserInputToCallbacks() {
        var query = ""
        var category = ""
        var clicked = ""
        compose.setContent {
            ShopDemoTheme {
                DiscoverContent(loaded, { query = it }, { category = it }, { clicked = it.id })
            }
        }

        compose.onNodeWithTag(TestTags.Discover.SEARCH).performTextInput("watch")
        compose.onNodeWithTag(TestTags.Discover.category("audio")).performClick()
        compose.onNodeWithTag(TestTags.Discover.product("p2")).performClick()

        assertEquals("watch", query)
        assertEquals("audio", category)
        assertEquals("p2", clicked)
    }
}

@RunWith(AndroidJUnit4::class)
class CartScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun emptyCartHidesCheckout() {
        compose.setContent { ShopDemoTheme { CartScreen(emptyList(), {}, {}, {}) } }

        compose.onNodeWithTag(TestTags.Cart.EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(TestTags.Cart.CHECKOUT).assertDoesNotExist()
    }

    @Test
    fun showsQuantityAndSubtotal() {
        val items = listOf(CartItem(MockCatalogService.PRODUCTS[1], 2)) // 2 × $89.00
        compose.setContent { ShopDemoTheme { CartScreen(items, {}, {}, {}) } }

        // ListItem merges its children's semantics, so tags inside it live in the unmerged tree.
        compose.onNodeWithTag(TestTags.Cart.qty("p2"), useUnmergedTree = true).assertTextEquals("2")
        compose.onNodeWithTag(TestTags.Cart.SUBTOTAL).assertTextEquals("$178.00")
    }

    @Test
    fun quantityButtonsInvokeCallbacks() {
        val calls = mutableListOf<String>()
        val items = listOf(CartItem(MockCatalogService.PRODUCTS[1], 1))
        compose.setContent {
            ShopDemoTheme { CartScreen(items, { calls += "inc:$it" }, { calls += "dec:$it" }, { calls += "checkout" }) }
        }

        compose.onNodeWithTag(TestTags.Cart.increment("p2")).performClick()
        compose.onNodeWithTag(TestTags.Cart.decrement("p2")).performClick()
        compose.onNodeWithTag(TestTags.Cart.CHECKOUT).performClick()

        assertEquals(listOf("inc:p2", "dec:p2", "checkout"), calls)
    }
}

@RunWith(AndroidJUnit4::class)
class CheckoutContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun showsErrorFromState() {
        compose.setContent {
            ShopDemoTheme {
                CheckoutContent(CheckoutUiState(error = "Your card was declined"), "45.00".toBigDecimal(), {}, {}, {})
            }
        }

        compose.onNodeWithTag(TestTags.Checkout.ERROR).assertTextEquals("Your card was declined")
        compose.onNodeWithTag(TestTags.Checkout.TOTAL).assertTextEquals("$45.00")
    }

    @Test
    fun placeOrderDisabledWhilePlacing() {
        compose.setContent {
            ShopDemoTheme { CheckoutContent(CheckoutUiState(isPlacing = true), 0.toBigDecimal(), {}, {}, {}) }
        }

        compose.onNodeWithTag(TestTags.Checkout.PLACE_ORDER).assertIsNotEnabled()
    }

    @Test
    fun confirmationShowsOrderAndCallsDone() {
        var done = false
        compose.setContent {
            ShopDemoTheme { ConfirmationScreen(Order("ORD-123456", "45".toBigDecimal())) { done = true } }
        }

        compose.onNodeWithTag(TestTags.Confirmation.ORDER_NUMBER).assertTextEquals("ORD-123456")
        compose.onNodeWithText("Total paid: $45.00").assertIsDisplayed()
        compose.onNodeWithTag(TestTags.Confirmation.DONE).performClick()
        assertTrue(done)
    }
}
