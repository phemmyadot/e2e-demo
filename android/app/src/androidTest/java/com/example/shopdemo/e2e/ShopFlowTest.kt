package com.example.shopdemo.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.shopdemo.R
import com.example.shopdemo.TestData
import com.example.shopdemo.launchLogin
import com.example.shopdemo.launchStoreSignedIn
import com.example.shopdemo.resetApp
import com.example.shopdemo.testing.TestTags
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end flows across the real app: Espresso drives the View-based login, Compose UI Test
 * drives the Compose store. `createEmptyComposeRule` attaches to whichever activity is running.
 */
@RunWith(AndroidJUnit4::class)
class ShopFlowTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Before
    fun setUp() = resetApp(latencyMs = 0)

    @Test
    fun loginBrowseAndCheckout() {
        launchLogin().use {
            // Espresso: View-based login
            onView(withId(R.id.login_email)).perform(typeText(TestData.EMAIL))
            onView(withId(R.id.login_password)).perform(typeText(TestData.PASSWORD))
            closeSoftKeyboard()
            onView(withId(R.id.login_submit)).perform(click())

            // Compose: store
            compose.waitForTag(TestTags.Discover.SCREEN)
            compose.addToCart("p1") // $199.99
            compose.addToCart("p5") // $45.00

            compose.onNodeWithTag(TestTags.Tab.CART).performClick()
            compose.onNodeWithTag(TestTags.Cart.SUBTOTAL).assertTextEquals("$244.99")
            compose.onNodeWithTag(TestTags.Cart.CHECKOUT).performClick()

            compose.fillCheckout(TestData.SUCCESS_CARD)
            compose.onNodeWithTag(TestTags.Checkout.TOTAL).assertTextEquals("$244.99")
            compose.onNodeWithTag(TestTags.Checkout.PLACE_ORDER).performClick()

            compose.waitForTag(TestTags.Confirmation.SCREEN)
            val orderNumber = compose.onNodeWithTag(TestTags.Confirmation.ORDER_NUMBER).textValue()
            assertTrue(orderNumber, Regex("""ORD-\d{6}""").matches(orderNumber))

            compose.onNodeWithTag(TestTags.Confirmation.DONE).performClick()
            compose.onNodeWithTag(TestTags.Cart.EMPTY).assertIsDisplayed()
        }
    }

    @Test
    fun declinedCardShowsErrorAndKeepsCart() {
        launchStoreSignedIn().use {
            compose.waitForTag(TestTags.Discover.SCREEN)
            compose.addToCart("p3")
            compose.onNodeWithTag(TestTags.Tab.CART).performClick()
            compose.onNodeWithTag(TestTags.Cart.CHECKOUT).performClick()

            compose.fillCheckout(TestData.DECLINED_CARD)
            compose.onNodeWithTag(TestTags.Checkout.PLACE_ORDER).performClick()

            compose.waitForTag(TestTags.Checkout.ERROR)
            compose.onNodeWithTag(TestTags.Checkout.ERROR).assertTextEquals("Your card was declined")
        }
    }

    @Test
    fun missingShippingIsValidated() {
        launchStoreSignedIn().use {
            compose.waitForTag(TestTags.Discover.SCREEN)
            compose.addToCart("p4")
            compose.onNodeWithTag(TestTags.Tab.CART).performClick()
            compose.onNodeWithTag(TestTags.Cart.CHECKOUT).performClick()
            compose.onNodeWithTag(TestTags.Checkout.PLACE_ORDER).performClick()

            compose.waitForTag(TestTags.Checkout.ERROR)
            compose.onNodeWithTag(TestTags.Checkout.ERROR).assertTextEquals("Please complete your shipping details")
        }
    }

    @Test
    fun categoryFilterAndSearch() {
        launchStoreSignedIn().use {
            compose.waitForTag(TestTags.Discover.product("p3"))

            compose.onNodeWithTag(TestTags.Discover.category("audio")).performClick()
            compose.waitForTag(TestTags.Discover.product("p1"))
            compose.onNodeWithTag(TestTags.Discover.product("p3")).assertDoesNotExist()

            compose.onNodeWithTag(TestTags.Discover.category("all")).performClick()
            compose.onNodeWithTag(TestTags.Discover.SEARCH).performTextInput("zzz")
            compose.waitForTag(TestTags.Discover.EMPTY)
        }
    }

    @Test
    fun signOutReturnsToLogin() {
        launchStoreSignedIn().use {
            compose.waitForTag(TestTags.Discover.SCREEN)
            compose.onNodeWithTag(TestTags.Tab.PROFILE).performClick()
            compose.onNodeWithTag(TestTags.Profile.EMAIL, useUnmergedTree = true).assertTextEquals(TestData.EMAIL)

            compose.onNodeWithTag(TestTags.Profile.SIGN_OUT).performClick()

            onView(withId(R.id.login_title)).check(matches(isDisplayed()))
        }
    }
}

// region Flow helpers

private fun ComposeTestRule.waitForTag(tag: String, timeoutMillis: Long = 5_000) =
    waitUntil(timeoutMillis) { onAllNodes(androidx.compose.ui.test.hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }

private fun ComposeTestRule.addToCart(productId: String) {
    onNodeWithTag(TestTags.Tab.DISCOVER).performClick()
    waitForTag(TestTags.Discover.product(productId))
    onNodeWithTag(TestTags.Discover.product(productId)).performClick()
    onNodeWithTag(TestTags.Product.ADD_TO_CART).performClick()
    onNodeWithTag(TestTags.Product.ADDED_TOAST).assertIsDisplayed()
    onNodeWithTag(TestTags.Product.BACK).performClick()
}

private fun ComposeTestRule.fillCheckout(card: String) {
    listOf(
        TestTags.Checkout.FULL_NAME to "Ada Lovelace",
        TestTags.Checkout.ADDRESS to "1 Analytical Way",
        TestTags.Checkout.CITY to "London",
        TestTags.Checkout.ZIP to "10001",
        TestTags.Checkout.CARD_NUMBER to card,
        TestTags.Checkout.EXPIRY to "12/30",
        TestTags.Checkout.CVC to "123",
    ).forEach { (tag, text) ->
        onNodeWithTag(tag).performScrollTo().performTextInput(text)
    }
}

private fun androidx.compose.ui.test.SemanticsNodeInteraction.textValue(): String =
    fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].joinToString("")

// endregion
