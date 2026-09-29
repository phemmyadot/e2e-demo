import Foundation
import Testing
@testable import ShopDemo

@Suite("CartStore")
struct CartStoreTests {
    let headphones = MockCatalogService.allProducts[0] // $199.99
    let speaker = MockCatalogService.allProducts[1]    // $89.00

    @Test func startsEmpty() {
        let cart = CartStore()
        #expect(cart.items.isEmpty)
        #expect(cart.count == 0)
        #expect(cart.subtotal == 0)
    }

    @Test func addingSameProductIncrementsQuantity() {
        let cart = CartStore()
        cart.add(headphones)
        cart.add(headphones)

        #expect(cart.items.count == 1)
        #expect(cart.items.first?.quantity == 2)
        #expect(cart.count == 2)
    }

    @Test func subtotalSumsLineTotals() {
        let cart = CartStore()
        cart.add(headphones)
        cart.add(speaker)
        cart.increment(speaker.id)

        #expect(cart.subtotal == Decimal(string: "377.99"))
        #expect(cart.subtotal.currency == "$377.99")
    }

    @Test func decrementToZeroRemovesItem() {
        let cart = CartStore()
        cart.add(speaker)
        cart.decrement(speaker.id)

        #expect(cart.items.isEmpty)
    }

    @Test func unknownIdsAreIgnored() {
        let cart = CartStore()
        cart.add(speaker)
        cart.increment("nope")
        cart.decrement("nope")

        #expect(cart.count == 1)
    }

    @Test func clearEmptiesCart() {
        let cart = CartStore()
        cart.add(headphones)
        cart.add(speaker)
        cart.clear()

        #expect(cart.items.isEmpty)
    }
}
