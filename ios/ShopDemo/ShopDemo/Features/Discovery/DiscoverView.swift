import SwiftUI

struct DiscoverView: View {
    let cart: CartStore
    private let catalog: CatalogService = MockCatalogService()

    @State private var query = ""
    @State private var selectedCategory = "all"
    @State private var categories: [Category] = []
    @State private var products: [Product] = []
    @State private var isLoading = true

    var body: some View {
        NavigationStack {
            List {
                Section {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(categories) { category in
                                CategoryChip(
                                    category: category,
                                    isSelected: category.id == selectedCategory
                                ) { selectedCategory = category.id }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
                .listRowInsets(EdgeInsets(top: 0, leading: 16, bottom: 0, trailing: 16))

                Section {
                    if isLoading {
                        HStack { Spacer(); ProgressView(); Spacer() }
                    } else if products.isEmpty {
                        Text("No products found")
                            .foregroundStyle(.secondary)
                            .accessibilityIdentifier(AID.Discover.empty)
                    } else {
                        ForEach(products) { product in
                            NavigationLink(value: product) {
                                ProductRow(product: product)
                            }
                            .accessibilityIdentifier(AID.Discover.product(product.id))
                        }
                    }
                }
            }
            .navigationTitle("Discover")
            .searchable(text: $query, prompt: "Search products")
            .navigationDestination(for: Product.self) { ProductDetailView(product: $0, cart: cart) }
            .accessibilityContainer(AID.Discover.screen)
            .task { categories = await catalog.categories() }
            .task(id: "\(query)|\(selectedCategory)") { await load() }
        }
    }

    private func load() async {
        isLoading = true
        let result = await catalog.products(query: query, categoryId: selectedCategory)
        guard !Task.isCancelled else { return }
        products = result
        isLoading = false
    }
}

private struct CategoryChip: View {
    let category: Category
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(category.name, action: action)
            .buttonStyle(.bordered)
            .tint(isSelected ? .accentColor : .secondary)
            .accessibilityIdentifier(AID.Discover.category(category.id))
            .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private struct ProductRow: View {
    let product: Product

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: product.symbol)
                .font(.title2)
                .frame(width: 44, height: 44)
                .background(.tint.opacity(0.12), in: .rect(cornerRadius: 8))
                .accessibilityHidden(true)
            VStack(alignment: .leading) {
                Text(product.name).font(.headline)
                Text(product.price.currency).foregroundStyle(.secondary)
            }
        }
    }
}

#Preview {
    DiscoverView(cart: CartStore())
}
