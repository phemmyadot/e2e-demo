import SwiftUI
import Testing
import ViewInspector
@testable import ShopDemo

@MainActor
@Suite("LoginView")
struct LoginViewTests {
    @Test func rendersTitleAndHint() throws {
        let sut = LoginView(session: SessionStore())

        #expect(throws: Never.self) { try sut.inspect().find(text: "Welcome to ShopDemo") }
        #expect(throws: Never.self) { try sut.inspect().find(text: "Hint: demo@shop.com / password123") }
    }

    @Test func signInIsDisabledWhenFieldsAreEmpty() throws {
        let sut = LoginView(session: SessionStore())
        let button = try sut.inspect().find(viewWithAccessibilityIdentifier: AID.Login.submit).button()

        #expect(button.isDisabled())
    }

    @Test func noErrorShownInitially() throws {
        let sut = LoginView(session: SessionStore())
        #expect(throws: (any Error).self) {
            try sut.inspect().find(viewWithAccessibilityIdentifier: AID.Login.error)
        }
    }
}

@MainActor
@Suite("ProductDetailView")
struct ProductDetailViewTests {
    let product = MockCatalogService.allProducts[0]

    @Test func showsNamePriceAndDescription() throws {
        let sut = ProductDetailView(product: product, cart: CartStore())
        let view = try sut.inspect()

        #expect(try view.find(viewWithAccessibilityIdentifier: AID.Product.name).text().string() == "Wireless Headphones")
        #expect(try view.find(viewWithAccessibilityIdentifier: AID.Product.price).text().string() == "$199.99")
        #expect(try view.find(viewWithAccessibilityIdentifier: AID.Product.description).text().string() == product.summary)
    }

    @Test func addToCartAddsProductToStore() throws {
        let cart = CartStore()
        let sut = ProductDetailView(product: product, cart: cart)

        try sut.inspect().find(viewWithAccessibilityIdentifier: AID.Product.addToCart).button().tap()

        #expect(cart.count == 1)
        #expect(cart.items.first?.product == product)
    }
}

@MainActor
@Suite("CartView")
struct CartViewTests {
    @Test func emptyCartShowsEmptyState() throws {
        let sut = CartView(cart: CartStore())

        #expect(throws: Never.self) {
            try sut.inspect().find(viewWithAccessibilityIdentifier: AID.Cart.empty)
        }
    }

    @Test func filledCartShowsRowsAndSubtotal() throws {
        let cart = CartStore()
        cart.add(MockCatalogService.allProducts[1]) // $89.00
        cart.add(MockCatalogService.allProducts[1])
        let view = try CartView(cart: cart).inspect()

        #expect(try view.find(viewWithAccessibilityIdentifier: AID.Cart.qty("p2")).text().string() == "2")
        #expect(try view.find(viewWithAccessibilityIdentifier: AID.Cart.subtotal).text().string() == "$178.00")
    }

    @Test func incrementButtonUpdatesStore() throws {
        let cart = CartStore()
        cart.add(MockCatalogService.allProducts[1])
        let view = try CartView(cart: cart).inspect()

        try view.find(viewWithAccessibilityIdentifier: AID.Cart.increment("p2")).button().tap()

        #expect(cart.count == 2)
    }
}

@MainActor
@Suite("OrderConfirmationView")
struct OrderConfirmationViewTests {
    @Test func showsOrderNumberAndCallsDone() throws {
        var doneTapped = false
        let sut = OrderConfirmationView(order: Order(id: "ORD-123456", total: 45)) { doneTapped = true }
        let view = try sut.inspect()

        #expect(try view.find(viewWithAccessibilityIdentifier: AID.Confirmation.orderNumber).text().string() == "ORD-123456")
        #expect(throws: Never.self) { try view.find(text: "Total paid: $45.00") }

        try view.find(viewWithAccessibilityIdentifier: AID.Confirmation.done).button().tap()
        #expect(doneTapped)
    }
}
