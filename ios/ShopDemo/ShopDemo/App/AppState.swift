import Foundation
import Observation

@Observable
final class SessionStore {
    private(set) var user: User?
    var isLoggedIn: Bool { user != nil }

    private let auth: AuthService

    init(auth: AuthService = MockAuthService(), user: User? = nil) {
        self.auth = auth
        self.user = user
    }

    func signIn(email: String, password: String) async throws {
        user = try await auth.signIn(email: email, password: password)
    }

    func signOut() { user = nil }
}

@Observable
final class CartStore {
    private(set) var items: [CartItem] = []

    var count: Int { items.reduce(0) { $0 + $1.quantity } }
    var subtotal: Decimal { items.reduce(0) { $0 + $1.lineTotal } }

    func add(_ product: Product) {
        if let i = items.firstIndex(where: { $0.id == product.id }) {
            items[i].quantity += 1
        } else {
            items.append(CartItem(product: product, quantity: 1))
        }
    }

    func increment(_ id: String) {
        guard let i = items.firstIndex(where: { $0.id == id }) else { return }
        items[i].quantity += 1
    }

    func decrement(_ id: String) {
        guard let i = items.firstIndex(where: { $0.id == id }) else { return }
        items[i].quantity -= 1
        if items[i].quantity == 0 { items.remove(at: i) }
    }

    func clear() { items.removeAll() }
}

/// Launch arguments set by XCUITest (`app.launchArguments`) to start in a known state:
///   -uitest.loggedIn            start signed in as the demo user
///   -uitest.latencyMs <ms>      override the mock service latency
enum LaunchConfig {
    private static let args = ProcessInfo.processInfo.arguments

    static var startLoggedIn: Bool { args.contains("-uitest.loggedIn") }

    /// `-key value` launch arguments are exposed through UserDefaults.
    static var latencyMs: UInt64? {
        UserDefaults.standard.string(forKey: "uitest.latencyMs").flatMap(UInt64.init)
    }
}
