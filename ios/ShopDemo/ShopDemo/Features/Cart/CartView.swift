import SwiftUI

enum CartRoute: Hashable {
    case checkout
    case confirmation(Order)
}

struct CartView: View {
    let cart: CartStore
    @State private var path: [CartRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            Group {
                if cart.items.isEmpty {
                    ContentUnavailableView(
                        "Your cart is empty",
                        systemImage: "cart",
                        description: Text("Browse Discover to add products.")
                    )
                    .accessibilityIdentifier(AID.Cart.empty)
                } else {
                    List {
                        ForEach(cart.items) { item in
                            CartRow(item: item, cart: cart)
                        }
                        HStack {
                            Text("Subtotal").font(.headline)
                            Spacer()
                            Text(cart.subtotal.currency)
                                .font(.headline)
                                .accessibilityIdentifier(AID.Cart.subtotal)
                        }
                    }
                    .safeAreaInset(edge: .bottom) {
                        Button {
                            path.append(.checkout)
                        } label: {
                            Text("Checkout").frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        .controlSize(.large)
                        .padding()
                        .background(.bar)
                        .accessibilityIdentifier(AID.Cart.checkout)
                    }
                }
            }
            .navigationTitle("Cart")
            .accessibilityContainer(AID.Cart.screen)
            .navigationDestination(for: CartRoute.self) { route in
                switch route {
                case .checkout:
                    CheckoutView(cart: cart) { order in
                        cart.clear()
                        path = [.confirmation(order)]
                    }
                case .confirmation(let order):
                    OrderConfirmationView(order: order) { path.removeAll() }
                }
            }
        }
    }
}

private struct CartRow: View {
    let item: CartItem
    let cart: CartStore

    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(item.product.name).font(.headline)
                Text(item.lineTotal.currency).foregroundStyle(.secondary)
            }
            Spacer()
            HStack(spacing: 12) {
                Button { cart.decrement(item.id) } label: { Image(systemName: "minus.circle") }
                    .accessibilityLabel("Decrease quantity")
                    .accessibilityIdentifier(AID.Cart.decrement(item.id))
                Text("\(item.quantity)")
                    .monospacedDigit()
                    .accessibilityIdentifier(AID.Cart.qty(item.id))
                Button { cart.increment(item.id) } label: { Image(systemName: "plus.circle") }
                    .accessibilityLabel("Increase quantity")
                    .accessibilityIdentifier(AID.Cart.increment(item.id))
            }
            .buttonStyle(.borderless)
            .font(.title3)
        }
        .accessibilityContainer(AID.Cart.row(item.id))
    }
}
