import XCTest

/// Base class for UI tests. Launches a fresh app per test; all app state is in-memory,
/// so every test starts clean.
class ShopDemoUITestCase: XCTestCase {
    var app: XCUIApplication!

    var tabBar: TabBar { TabBar(app: app) }
    var login: LoginScreen { LoginScreen(app: app) }
    var discover: DiscoverScreen { DiscoverScreen(app: app) }
    var product: ProductScreen { ProductScreen(app: app) }
    var cart: CartScreen { CartScreen(app: app) }
    var checkout: CheckoutScreen { CheckoutScreen(app: app) }
    var confirmation: ConfirmationScreen { ConfirmationScreen(app: app) }
    var profile: ProfileScreen { ProfileScreen(app: app) }

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
    }

    override func tearDownWithError() throws {
        if let run = testRun, run.failureCount > 0 {
            let shot = XCTAttachment(screenshot: app.screenshot())
            shot.name = "failure-\(name)"
            shot.lifetime = .keepAlways
            add(shot)
        }
        app = nil
    }

    /// - Parameters:
    ///   - loggedIn: skip the login screen (`-uitest.loggedIn`).
    ///   - latencyMs: mock service latency; 0 keeps tests fast.
    func launch(loggedIn: Bool = false, latencyMs: Int = 0) {
        app.launchArguments = ["-uitest.latencyMs", String(latencyMs)]
        if loggedIn { app.launchArguments.append("-uitest.loggedIn") }
        app.launch()
    }

    /// Adds a product via Discover → detail → Add to Cart, then returns to the list.
    func addToCart(_ productId: String) {
        tabBar.open("Discover")
        discover.openProduct(productId)
        product.addToCart.tap()
        product.addedToast.waitToAppear()
        product.back()
        discover.root.waitToAppear()
    }
}
