package com.example.shopdemo.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shopdemo.data.MockCatalogService
import com.example.shopdemo.data.Order
import com.example.shopdemo.data.ServiceLocator
import com.example.shopdemo.data.subtotal
import com.example.shopdemo.testing.LaunchConfig
import com.example.shopdemo.testing.TestTags
import com.example.shopdemo.ui.cart.CartScreen
import com.example.shopdemo.ui.checkout.CheckoutScreen
import com.example.shopdemo.ui.checkout.ConfirmationScreen
import com.example.shopdemo.ui.discover.DiscoverScreen
import com.example.shopdemo.ui.login.LoginActivity
import com.example.shopdemo.ui.product.ProductDetailScreen
import com.example.shopdemo.ui.profile.ProfileScreen
import com.example.shopdemo.ui.theme.ShopDemoTheme

/** The Compose half of the app. Auth gate: bounces to LoginActivity without a session. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LaunchConfig.apply(intent)
        if (!ServiceLocator.session.isLoggedIn) {
            showLogin()
            return
        }
        enableEdgeToEdge()
        setContent { ShopDemoTheme { ShopApp(onSignedOut = ::showLogin) } }
    }

    private fun showLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
        finish()
    }
}

private enum class Tab(val route: String, val label: String, val icon: ImageVector, val tag: String) {
    Discover("discover", "Discover", Icons.Default.Search, TestTags.Tab.DISCOVER),
    Cart("cart", "Cart", Icons.Default.ShoppingCart, TestTags.Tab.CART),
    Profile("profile", "Profile", Icons.Default.Person, TestTags.Tab.PROFILE),
}

private object Routes {
    const val PRODUCT = "product/{id}"
    const val CHECKOUT = "checkout"
    const val CONFIRMATION = "confirmation"
    fun product(id: String) = "product/$id"
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShopApp(onSignedOut: () -> Unit) {
    val nav = rememberNavController()
    val cart = ServiceLocator.cart
    val session = ServiceLocator.session
    val items by cart.items.collectAsStateWithLifecycle()
    val user by session.user.collectAsStateWithLifecycle()
    var lastOrder by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        // Exposes test tags as resource IDs so UI Automator (benchmarks) can find them.
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        bottomBar = { BottomBar(nav, cartCount = items.sumOf { it.quantity }) },
    ) { padding ->
        NavHost(nav, startDestination = Tab.Discover.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Discover.route) {
                DiscoverScreen(onProductClick = { nav.navigate(Routes.product(it.id)) })
            }
            composable(Routes.PRODUCT) { entry ->
                val product = MockCatalogService.PRODUCTS.first { it.id == entry.arguments?.getString("id") }
                ProductDetailScreen(
                    product = product,
                    onAddToCart = { cart.add(product) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Tab.Cart.route) {
                CartScreen(
                    items = items,
                    onIncrement = cart::increment,
                    onDecrement = cart::decrement,
                    onCheckout = { nav.navigate(Routes.CHECKOUT) },
                )
            }
            composable(Routes.CHECKOUT) {
                CheckoutScreen(total = items.subtotal()) { order ->
                    lastOrder = order
                    nav.navigate(Routes.CONFIRMATION) { popUpTo(Tab.Cart.route) }
                }
            }
            composable(Routes.CONFIRMATION) {
                val order = lastOrder ?: return@composable
                ConfirmationScreen(order) { nav.popBackStack(Tab.Cart.route, inclusive = false) }
            }
            composable(Tab.Profile.route) {
                ProfileScreen(user) {
                    cart.clear()
                    session.signOut()
                    onSignedOut()
                }
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController, cartCount: Int) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    NavigationBar {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = current == tab.route,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (tab == Tab.Cart && cartCount > 0) {
                        BadgedBox(badge = { Badge { Text("$cartCount") } }) { Icon(tab.icon, null) }
                    } else {
                        Icon(tab.icon, null)
                    }
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag(tab.tag),
            )
        }
    }
}
