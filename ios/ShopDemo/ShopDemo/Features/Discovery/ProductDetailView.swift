import SwiftUI

struct ProductDetailView: View {
    let product: Product
    let cart: CartStore
    @State private var showAdded = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Image(systemName: product.symbol)
                    .font(.system(size: 96))
                    .frame(maxWidth: .infinity, minHeight: 220)
                    .background(.tint.opacity(0.12), in: .rect(cornerRadius: 16))
                    .accessibilityHidden(true)
                Text(product.name)
                    .font(.title.bold())
                    .accessibilityIdentifier(AID.Product.name)
                Text(product.price.currency)
                    .font(.title2)
                    .accessibilityIdentifier(AID.Product.price)
                Text(product.summary)
                    .foregroundStyle(.secondary)
                    .accessibilityIdentifier(AID.Product.description)
            }
            .padding()
        }
        .safeAreaInset(edge: .bottom) {
            VStack(spacing: 8) {
                if showAdded {
                    Text("Added to cart")
                        .font(.footnote.bold())
                        .foregroundStyle(.green)
                        .accessibilityIdentifier(AID.Product.addedToast)
                }
                Button {
                    cart.add(product)
                    showAdded = true
                } label: {
                    Text("Add to Cart").frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .accessibilityIdentifier(AID.Product.addToCart)
            }
            .padding()
            .background(.bar)
        }
        .navigationBarTitleDisplayMode(.inline)
        .accessibilityContainer(AID.Product.screen)
    }
}
