import Foundation
import Testing
@testable import ShopDemo

@Suite("MockCatalogService")
struct MockCatalogServiceTests {
    let service = MockCatalogService()

    init() { MockLatency.nanoseconds = 0 }

    @Test func allCategoryReturnsEverything() async {
        let products = await service.products(query: "", categoryId: "all")
        #expect(products.count == MockCatalogService.allProducts.count)
    }

    @Test(arguments: [
        ("audio", ["p1", "p2"]),
        ("wearables", ["p3", "p4"]),
        ("home", ["p5", "p6"]),
    ])
    func filtersByCategory(categoryId: String, expected: [String]) async {
        let products = await service.products(query: "", categoryId: categoryId)
        #expect(products.map(\.id) == expected)
    }

    @Test func searchIsCaseInsensitiveAndTrimmed() async {
        let products = await service.products(query: "  WATCH ", categoryId: nil)
        #expect(products.map(\.id) == ["p3"])
    }

    @Test func searchAndCategoryCombine() async {
        let products = await service.products(query: "speaker", categoryId: "home")
        #expect(products.isEmpty)
    }
}

@Suite("MockCheckoutService")
struct MockCheckoutServiceTests {
    let service = MockCheckoutService()
    let items = [CartItem(product: MockCatalogService.allProducts[0], quantity: 2)]
    let shipping = ShippingAddress(fullName: "Ada", address: "1 Way", city: "London", zip: "10001")

    init() { MockLatency.nanoseconds = 0 }

    @Test func successCardPlacesOrder() async throws {
        let card = PaymentCard(number: "4242 4242 4242 4242", expiry: "12/30", cvc: "123")
        let order = try await service.placeOrder(items: items, shipping: shipping, card: card)

        #expect(order.total == Decimal(string: "399.98"))
        #expect(order.id.hasPrefix("ORD-"))
    }

    @Test func declinedCardThrows() async {
        let card = PaymentCard(number: "4000 0000 0000 0002", expiry: "12/30", cvc: "123")
        await #expect(throws: CheckoutError.declined) {
            try await service.placeOrder(items: items, shipping: shipping, card: card)
        }
    }

    @Test(arguments: ["4242", "", "4242 4242 4242 4242 1"])
    func malformedCardNumberThrows(number: String) async {
        let card = PaymentCard(number: number, expiry: "12/30", cvc: "123")
        await #expect(throws: CheckoutError.invalidCard) {
            try await service.placeOrder(items: items, shipping: shipping, card: card)
        }
    }

    @Test func blankShippingFieldThrows() async {
        var shipping = shipping
        shipping.city = "   "
        let card = PaymentCard(number: "4242424242424242", expiry: "12/30", cvc: "123")
        await #expect(throws: CheckoutError.missingShipping) {
            try await service.placeOrder(items: items, shipping: shipping, card: card)
        }
    }
}

@Suite("SessionStore")
struct SessionStoreTests {
    init() { MockLatency.nanoseconds = 0 }

    @Test func signInSetsUser() async throws {
        let session = SessionStore()
        try await session.signIn(email: "Demo@Shop.com ", password: "password123")

        #expect(session.isLoggedIn)
        #expect(session.user?.email == "demo@shop.com")
    }

    @Test func failedSignInLeavesSessionLoggedOut() async {
        let session = SessionStore()
        await #expect(throws: AuthError.invalidCredentials) {
            try await session.signIn(email: "demo@shop.com", password: "wrong")
        }
        #expect(!session.isLoggedIn)
    }

    @Test func signOutClearsUser() {
        let session = SessionStore(user: .demo)
        session.signOut()
        #expect(!session.isLoggedIn)
    }
}
