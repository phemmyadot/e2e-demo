import Foundation

struct User: Equatable {
    let id: String
    let name: String
    let email: String

    static let demo = User(id: "u_1", name: "Demo Shopper", email: "demo@shop.com")
}

struct Category: Identifiable, Hashable {
    let id: String
    let name: String
}

struct Product: Identifiable, Hashable {
    let id: String
    let name: String
    let categoryId: String
    let price: Decimal
    let symbol: String
    let summary: String
}

struct CartItem: Identifiable, Equatable {
    let product: Product
    var quantity: Int
    var id: String { product.id }
    var lineTotal: Decimal { product.price * Decimal(quantity) }
}

struct ShippingAddress: Equatable {
    var fullName = ""
    var address = ""
    var city = ""
    var zip = ""
}

struct PaymentCard: Equatable {
    var number = ""
    var expiry = ""
    var cvc = ""
}

struct Order: Identifiable, Hashable {
    let id: String
    let total: Decimal
}

extension Decimal {
    var currency: String {
        formatted(.currency(code: "USD").locale(Locale(identifier: "en_US")))
    }
}
