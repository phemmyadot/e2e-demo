import XCTest

final class CheckoutUITests: ShopDemoUITestCase {
    override func setUpWithError() throws {
        try super.setUpWithError()
        launch(loggedIn: true)
        discover.root.waitToAppear()
    }

    func testEmptyCartShowsEmptyState() {
        tabBar.open("Cart")

        cart.empty.waitToAppear()
        XCTAssertFalse(cart.checkout.exists)
    }

    func testQuantityControlsUpdateSubtotal() {
        addToCart("p2") // $89.00
        tabBar.open("Cart")

        XCTAssertEqual(cart.qty("p2").waitToAppear().label, "1")
        XCTAssertEqual(cart.subtotal.label, "$89.00")

        cart.increment("p2").tap()
        XCTAssertEqual(cart.qty("p2").label, "2")
        XCTAssertEqual(cart.subtotal.label, "$178.00")

        cart.decrement("p2").tap()
        cart.decrement("p2").tap()
        cart.empty.waitToAppear()
    }

    func testHappyPathToOrderConfirmation() {
        addToCart("p1") // $199.99
        addToCart("p5") // $45.00

        tabBar.open("Cart")
        XCTAssertEqual(cart.subtotal.waitToAppear().label, "$244.99")
        cart.proceedToCheckout().root.waitToAppear()

        checkout.fillShipping()
        checkout.fillCard(TestData.successCard)
        XCTAssertEqual(checkout.total.label, "$244.99")
        checkout.placeOrder.tap()

        confirmation.root.waitToAppear()
        let orderNumber = confirmation.orderNumber.label
        XCTAssertNotNil(orderNumber.range(of: #"^ORD-\d{6}$"#, options: .regularExpression), orderNumber)

        confirmation.done.tap()
        cart.empty.waitToAppear()
    }

    func testDeclinedCardShowsError() {
        addToCart("p3")
        tabBar.open("Cart")
        cart.proceedToCheckout().root.waitToAppear()

        checkout.fillShipping()
        checkout.fillCard(TestData.declinedCard)
        checkout.placeOrder.tap()

        XCTAssertEqual(checkout.error.waitToAppear().label, "Your card was declined")
        XCTAssertFalse(confirmation.root.exists)
    }

    func testMissingShippingIsValidated() {
        addToCart("p4")
        tabBar.open("Cart")
        cart.proceedToCheckout().root.waitToAppear()

        checkout.placeOrder.tap()

        XCTAssertEqual(checkout.error.waitToAppear().label, "Please complete your shipping details")
    }
}
