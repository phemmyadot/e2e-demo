import SwiftUI

@main
struct ShopDemoApp: App {
    @State private var session: SessionStore
    @State private var cart = CartStore()

    init() {
        if let ms = LaunchConfig.latencyMs { MockLatency.nanoseconds = ms * 1_000_000 }
        let user = LaunchConfig.startLoggedIn ? User.demo : nil
        _session = State(initialValue: SessionStore(user: user))
    }

    var body: some Scene {
        WindowGroup {
            RootView(session: session, cart: cart)
        }
    }
}

/// Auth gate: nothing behind the tab bar is reachable without a session.
struct RootView: View {
    let session: SessionStore
    let cart: CartStore

    var body: some View {
        Group {
            if session.isLoggedIn {
                MainTabView(session: session, cart: cart)
            } else {
                // NavigationStack hosts the keyboard toolbar.
                NavigationStack { LoginView(session: session) }
            }
        }
        .animation(.default, value: session.isLoggedIn)
    }
}

struct MainTabView: View {
    let session: SessionStore
    let cart: CartStore

    var body: some View {
        TabView {
            DiscoverView(cart: cart)
                .tabItem { Label("Discover", systemImage: "magnifyingglass") }
            CartView(cart: cart)
                .tabItem { Label("Cart", systemImage: "cart") }
                .badge(cart.count)
            ProfileView(session: session, cart: cart)
                .tabItem { Label("Profile", systemImage: "person") }
        }
    }
}
