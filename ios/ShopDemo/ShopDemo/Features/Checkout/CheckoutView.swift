import SwiftUI

struct CheckoutView: View {
    let cart: CartStore
    let onComplete: (Order) -> Void

    private let service: CheckoutService = MockCheckoutService()

    @State private var shipping = ShippingAddress()
    @State private var card = PaymentCard()
    @State private var error: String?
    @State private var isPlacing = false

    var body: some View {
        Form {
            Section("Shipping") {
                field("Full name", text: $shipping.fullName, id: AID.Checkout.fullName)
                    .textContentType(.name)
                field("Address", text: $shipping.address, id: AID.Checkout.address)
                    .textContentType(.streetAddressLine1)
                field("City", text: $shipping.city, id: AID.Checkout.city)
                    .textContentType(.addressCity)
                field("ZIP", text: $shipping.zip, id: AID.Checkout.zip)
                    .keyboardType(.numberPad)
            }

            Section {
                field("Card number", text: $card.number, id: AID.Checkout.cardNumber)
                    .keyboardType(.numberPad)
                HStack {
                    field("MM/YY", text: $card.expiry, id: AID.Checkout.expiry)
                    field("CVC", text: $card.cvc, id: AID.Checkout.cvc)
                        .keyboardType(.numberPad)
                }
            } header: {
                Text("Payment")
            } footer: {
                Text("Test cards: 4242 4242 4242 4242 (success), 4000 0000 0000 0002 (declined)")
            }

            Section {
                HStack {
                    Text("Total").font(.headline)
                    Spacer()
                    Text(cart.subtotal.currency)
                        .font(.headline)
                        .accessibilityIdentifier(AID.Checkout.total)
                }
                if let error {
                    Text(error)
                        .foregroundStyle(.red)
                        .accessibilityIdentifier(AID.Checkout.error)
                }
            }
        }
        .safeAreaInset(edge: .bottom) {
            Button {
                Task { await placeOrder() }
            } label: {
                ZStack {
                    Text("Place Order").opacity(isPlacing ? 0 : 1)
                    if isPlacing {
                        ProgressView().accessibilityIdentifier(AID.Checkout.spinner)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(isPlacing)
            .padding()
            .background(.bar)
            .accessibilityLabel("Place Order")
            .accessibilityIdentifier(AID.Checkout.placeOrder)
        }
        .navigationTitle("Checkout")
        .keyboardDoneButton()
        .accessibilityContainer(AID.Checkout.screen)
    }

    private func field(_ title: String, text: Binding<String>, id: String) -> some View {
        TextField(title, text: text)
            .autocorrectionDisabled()
            .accessibilityLabel(title)
            .accessibilityIdentifier(id)
    }

    private func placeOrder() async {
        error = nil
        isPlacing = true
        defer { isPlacing = false }
        do {
            let order = try await service.placeOrder(items: cart.items, shipping: shipping, card: card)
            onComplete(order)
        } catch {
            self.error = error.localizedDescription
        }
    }
}
