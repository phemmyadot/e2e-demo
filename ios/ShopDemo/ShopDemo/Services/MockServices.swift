import Foundation

/// All backend calls are mocked in-process with a small artificial delay so that
/// E2E tests exercise real loading states without any network dependency.
enum MockLatency {
    static var nanoseconds: UInt64 = 600_000_000
    static func wait() async { try? await Task.sleep(nanoseconds: nanoseconds) }
}

// MARK: - Auth

enum AuthError: LocalizedError, Equatable {
    case invalidCredentials
    case locked

    var errorDescription: String? {
        switch self {
        case .invalidCredentials: "Invalid email or password"
        case .locked: "This account is locked"
        }
    }
}

protocol AuthService {
    func signIn(email: String, password: String) async throws -> User
}

struct MockAuthService: AuthService {
    /// Seeded accounts: email -> password.
    static let accounts = [
        "demo@shop.com": "password123",
        "locked@shop.com": "password123",
    ]

    func signIn(email: String, password: String) async throws -> User {
        await MockLatency.wait()
        let email = email.trimmingCharacters(in: .whitespaces).lowercased()
        guard Self.accounts[email] == password else { throw AuthError.invalidCredentials }
        if email == "locked@shop.com" { throw AuthError.locked }
        return User(id: User.demo.id, name: User.demo.name, email: email)
    }
}

// MARK: - Catalog

protocol CatalogService {
    func categories() async -> [Category]
    func products(query: String, categoryId: String?) async -> [Product]
}

struct MockCatalogService: CatalogService {
    static let allCategories = [
        Category(id: "all", name: "All"),
        Category(id: "audio", name: "Audio"),
        Category(id: "wearables", name: "Wearables"),
        Category(id: "home", name: "Home"),
    ]

    static let allProducts = [
        Product(id: "p1", name: "Wireless Headphones", categoryId: "audio", price: Decimal(string: "199.99")!,
                symbol: "headphones", summary: "Noise-cancelling over-ear headphones with 30h battery."),
        Product(id: "p2", name: "Bluetooth Speaker", categoryId: "audio", price: Decimal(string: "89.00")!,
                symbol: "hifispeaker", summary: "Portable waterproof speaker with deep bass."),
        Product(id: "p3", name: "Smart Watch", categoryId: "wearables", price: Decimal(string: "249.00")!,
                symbol: "applewatch", summary: "Fitness tracking, notifications and GPS."),
        Product(id: "p4", name: "Fitness Band", categoryId: "wearables", price: Decimal(string: "59.50")!,
                symbol: "figure.run", summary: "Lightweight tracker for steps, sleep and heart rate."),
        Product(id: "p5", name: "Desk Lamp", categoryId: "home", price: Decimal(string: "45.00")!,
                symbol: "lamp.desk", summary: "Dimmable LED lamp with warm and cool modes."),
        Product(id: "p6", name: "Coffee Maker", categoryId: "home", price: Decimal(string: "129.00")!,
                symbol: "cup.and.saucer", summary: "Programmable drip coffee maker, 12 cups."),
    ]

    func categories() async -> [Category] { Self.allCategories }

    func products(query: String, categoryId: String?) async -> [Product] {
        await MockLatency.wait()
        let q = query.trimmingCharacters(in: .whitespaces).lowercased()
        return Self.allProducts.filter { p in
            (categoryId == nil || categoryId == "all" || p.categoryId == categoryId)
                && (q.isEmpty || p.name.lowercased().contains(q))
        }
    }
}

// MARK: - Checkout

enum CheckoutError: LocalizedError, Equatable {
    case missingShipping
    case invalidCard
    case declined

    var errorDescription: String? {
        switch self {
        case .missingShipping: "Please complete your shipping details"
        case .invalidCard: "Please enter a valid card"
        case .declined: "Your card was declined"
        }
    }
}

protocol CheckoutService {
    func placeOrder(items: [CartItem], shipping: ShippingAddress, card: PaymentCard) async throws -> Order
}

struct MockCheckoutService: CheckoutService {
    /// Stripe-style magic numbers.
    static let successCard = "4242424242424242"
    static let declinedCard = "4000000000000002"

    func placeOrder(items: [CartItem], shipping: ShippingAddress, card: PaymentCard) async throws -> Order {
        guard [shipping.fullName, shipping.address, shipping.city, shipping.zip]
            .allSatisfy({ !$0.trimmingCharacters(in: .whitespaces).isEmpty })
        else { throw CheckoutError.missingShipping }

        let digits = card.number.filter(\.isNumber)
        guard digits.count == 16, card.expiry.count >= 4, card.cvc.count >= 3 else {
            throw CheckoutError.invalidCard
        }

        await MockLatency.wait()
        if digits == Self.declinedCard { throw CheckoutError.declined }

        let total = items.reduce(Decimal(0)) { $0 + $1.lineTotal }
        return Order(id: "ORD-\(Int.random(in: 100_000...999_999))", total: total)
    }
}
