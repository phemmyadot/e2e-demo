package com.example.shopdemo

import com.example.shopdemo.data.AuthException
import com.example.shopdemo.data.AuthService
import com.example.shopdemo.data.CartRepository
import com.example.shopdemo.data.CatalogService
import com.example.shopdemo.data.CheckoutException
import com.example.shopdemo.data.CheckoutService
import com.example.shopdemo.data.MockCatalogService
import com.example.shopdemo.data.Order
import com.example.shopdemo.data.SessionRepository
import com.example.shopdemo.data.User
import com.example.shopdemo.ui.checkout.CheckoutViewModel
import com.example.shopdemo.ui.discover.DiscoverViewModel
import com.example.shopdemo.ui.login.LoginViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

class LoginViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val auth: AuthService = mock()
    private val session = SessionRepository(auth)

    @Test
    fun submitIsDisabledUntilBothFieldsFilled() {
        val vm = LoginViewModel(session)
        assertFalse(vm.state.value.canSubmit)
        vm.onEmailChanged("demo@shop.com")
        assertFalse(vm.state.value.canSubmit)
        vm.onPasswordChanged("password123")
        assertTrue(vm.state.value.canSubmit)
    }

    @Test
    fun successfulSubmitSignsIn() = runTest {
        whenever(auth.signIn("demo@shop.com", "password123")) doReturn User.DEMO
        val vm = LoginViewModel(session).apply {
            onEmailChanged("demo@shop.com")
            onPasswordChanged("password123")
        }

        vm.submit()

        assertTrue(vm.state.value.signedIn)
        assertTrue(session.isLoggedIn)
        verifyBlocking(auth) { signIn("demo@shop.com", "password123") }
    }

    @Test
    fun failedSubmitShowsErrorAndStaysSignedOut() = runTest {
        auth.stub { onBlocking { signIn(any(), any()) } doSuspendableAnswer { throw AuthException.Locked() } }
        val vm = LoginViewModel(session).apply {
            onEmailChanged("locked@shop.com")
            onPasswordChanged("password123")
        }

        vm.submit()

        assertEquals("This account is locked", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
        assertFalse(session.isLoggedIn)
    }

    @Test
    fun submitWithEmptyFieldsDoesNotCallService() = runTest {
        LoginViewModel(session).submit()
        verify(auth, never()).signIn(any(), any())
    }
}

class DiscoverViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val catalog: CatalogService = mock {
        onBlocking { categories() } doReturn MockCatalogService.CATEGORIES
        onBlocking { products(any(), any()) } doReturn MockCatalogService.PRODUCTS
    }

    @Test
    fun loadsCategoriesAndProductsOnInit() {
        val vm = DiscoverViewModel(catalog)

        assertEquals(4, vm.state.value.categories.size)
        assertEquals(6, vm.state.value.products.size)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun selectingCategoryReloadsWithFilter() = runTest {
        val vm = DiscoverViewModel(catalog)
        vm.onCategorySelected("audio")

        assertEquals("audio", vm.state.value.selectedCategory)
        verifyBlocking(catalog) { products(eq(""), eq("audio")) }
    }
}

class CheckoutViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val checkout: CheckoutService = mock()
    private val cart = CartRepository().apply { add(MockCatalogService.PRODUCTS[0]) }

    @Test
    fun successfulOrderClearsCartAndExposesOrder() = runTest {
        val order = Order("ORD-000001", "199.99".toBigDecimal())
        whenever(checkout.placeOrder(any(), any(), any())) doReturn order
        val vm = CheckoutViewModel(checkout, cart)

        vm.placeOrder()

        assertEquals(order, vm.state.value.placedOrder)
        assertTrue(cart.items.value.isEmpty())
    }

    @Test
    fun declinedCardShowsErrorAndKeepsCart() = runTest {
        checkout.stub {
            onBlocking { placeOrder(any(), any(), any()) } doSuspendableAnswer { throw CheckoutException.Declined() }
        }
        val vm = CheckoutViewModel(checkout, cart)

        vm.placeOrder()

        assertEquals("Your card was declined", vm.state.value.error)
        assertNull(vm.state.value.placedOrder)
        assertEquals(1, cart.count)
    }

    @Test
    fun formInputIsPassedToService() = runTest {
        whenever(checkout.placeOrder(any(), any(), any())) doReturn Order("ORD-1", 0.toBigDecimal())
        val vm = CheckoutViewModel(checkout, cart)
        vm.updateShipping { it.copy(fullName = "Ada", city = "London") }
        vm.updateCard { it.copy(number = "4242") }

        vm.placeOrder()

        verifyBlocking(checkout) {
            placeOrder(
                eq(listOf(com.example.shopdemo.data.CartItem(MockCatalogService.PRODUCTS[0], 1))),
                eq(com.example.shopdemo.data.ShippingAddress(fullName = "Ada", city = "London")),
                eq(com.example.shopdemo.data.PaymentCard(number = "4242")),
            )
        }
    }
}
