import Foundation

/// Stable accessibility identifiers shared by the app, ShopDemoUITests (XCUITest) and
/// ShopDemoTests (ViewInspector). The test targets compile this file directly.
enum AID {
    enum Login {
        static let screen = "login.screen"
        static let email = "login.email"
        static let password = "login.password"
        static let submit = "login.submit"
        static let error = "login.error"
        static let spinner = "login.spinner"
    }

    enum Discover {
        static let screen = "discover.screen"
        static let search = "discover.search"
        static let empty = "discover.empty"
        static func category(_ id: String) -> String { "discover.category.\(id)" }
        static func product(_ id: String) -> String { "discover.product.\(id)" }
    }

    enum Product {
        static let screen = "product.screen"
        static let name = "product.name"
        static let price = "product.price"
        static let description = "product.description"
        static let addToCart = "product.addToCart"
        static let addedToast = "product.addedToast"
    }

    enum Cart {
        static let screen = "cart.screen"
        static let empty = "cart.empty"
        static let subtotal = "cart.subtotal"
        static let checkout = "cart.checkout"
        static func row(_ id: String) -> String { "cart.row.\(id)" }
        static func qty(_ id: String) -> String { "cart.qty.\(id)" }
        static func increment(_ id: String) -> String { "cart.increment.\(id)" }
        static func decrement(_ id: String) -> String { "cart.decrement.\(id)" }
    }

    enum Checkout {
        static let screen = "checkout.screen"
        static let fullName = "checkout.fullName"
        static let address = "checkout.address"
        static let city = "checkout.city"
        static let zip = "checkout.zip"
        static let cardNumber = "checkout.cardNumber"
        static let expiry = "checkout.expiry"
        static let cvc = "checkout.cvc"
        static let total = "checkout.total"
        static let placeOrder = "checkout.placeOrder"
        static let error = "checkout.error"
        static let spinner = "checkout.spinner"
    }

    enum Keyboard {
        static let done = "keyboard.done"
    }

    enum Confirmation {
        static let screen = "confirmation.screen"
        static let orderNumber = "confirmation.orderNumber"
        static let done = "confirmation.done"
    }

    enum Profile {
        static let screen = "profile.screen"
        static let email = "profile.email"
        static let signOut = "profile.signOut"
    }
}
