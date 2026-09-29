package com.example.shopdemo.data

/**
 * Minimal manual DI. Instrumented tests run in the app process, so they call [reset] in
 * `@Before` to start from a clean state (the app keeps everything in memory).
 */
object ServiceLocator {
    var auth: AuthService = MockAuthService()
    var catalog: CatalogService = MockCatalogService()
    var checkout: CheckoutService = MockCheckoutService()
    var session = SessionRepository(auth)
        private set
    var cart = CartRepository()
        private set

    fun reset() {
        auth = MockAuthService()
        catalog = MockCatalogService()
        checkout = MockCheckoutService()
        session = SessionRepository(auth)
        cart = CartRepository()
        MockLatency.millis = 600
    }
}
