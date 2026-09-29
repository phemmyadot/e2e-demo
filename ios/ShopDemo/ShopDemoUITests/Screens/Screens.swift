import XCTest

/// Page objects. Each screen wraps the XCUIElement queries for one feature so flows read as steps.
/// Identifiers come from `AID` (ShopDemo/Support/AccessibilityID.swift, compiled into this target).

let defaultTimeout: TimeInterval = 10

extension XCUIApplication {
    /// Any element type, matched by accessibility identifier.
    func element(_ id: String) -> XCUIElement {
        descendants(matching: .any).matching(identifier: id).firstMatch
    }

    func dismissKeyboard() {
        let done = buttons[AID.Keyboard.done]
        if done.exists { done.tap() }
    }

    /// iOS shows a system "Save Password?" sheet shortly after a successful login (it detects
    /// the email + SecureField pair even without content types). It blocks taps underneath,
    /// so dismiss it and wait for it to finish animating away.
    func dismissSavePasswordPrompt(timeout: TimeInterval = 3) {
        let notNow = buttons["Not Now"]
        guard notNow.waitForExistence(timeout: timeout) else { return }
        notNow.tap()
        notNow.waitToDisappear()
    }
}

extension XCUIElement {
    @discardableResult
    func waitToAppear(_ timeout: TimeInterval = defaultTimeout,
                      file: StaticString = #filePath, line: UInt = #line) -> XCUIElement {
        XCTAssertTrue(waitForExistence(timeout: timeout), "\(self) did not appear", file: file, line: line)
        return self
    }

    func waitToDisappear(_ timeout: TimeInterval = defaultTimeout,
                         file: StaticString = #filePath, line: UInt = #line) {
        let gone = XCTNSPredicateExpectation(predicate: NSPredicate(format: "exists == false"), object: self)
        XCTAssertEqual(XCTWaiter.wait(for: [gone], timeout: timeout), .completed,
                       "\(self) is still visible", file: file, line: line)
    }

    /// Replaces the field's contents.
    func replaceText(_ text: String) {
        tap()
        if let current = value as? String, !current.isEmpty, current != placeholderValue {
            typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: current.count))
        }
        typeText(text)
    }
}

struct TabBar {
    let app: XCUIApplication
    func open(_ tab: String) { app.tabBars.buttons[tab].tap() }
}

struct LoginScreen {
    let app: XCUIApplication

    var root: XCUIElement { app.element(AID.Login.screen) }
    var email: XCUIElement { app.textFields[AID.Login.email] }
    var password: XCUIElement { app.secureTextFields[AID.Login.password] }
    var submit: XCUIElement { app.buttons[AID.Login.submit] }
    var error: XCUIElement { app.staticTexts[AID.Login.error] }

    func enter(email: String, password: String) {
        self.email.tap()
        self.email.typeText(email)
        self.password.tap()
        self.password.typeText(password)
        app.dismissKeyboard()
    }

    /// Signs in and expects failure (the Save Password sheet only follows a successful login).
    func attemptSignIn(email: String = TestData.email, password: String = TestData.password) {
        enter(email: email, password: password)
        submit.tap()
    }

    @discardableResult
    func signIn(email: String = TestData.email, password: String = TestData.password) -> DiscoverScreen {
        attemptSignIn(email: email, password: password)
        let discover = DiscoverScreen(app: app)
        discover.root.waitToAppear()
        app.dismissSavePasswordPrompt()
        return discover
    }
}

struct DiscoverScreen {
    let app: XCUIApplication

    var root: XCUIElement { app.element(AID.Discover.screen) }
    var search: XCUIElement { app.searchFields.firstMatch }
    var empty: XCUIElement { app.staticTexts[AID.Discover.empty] }
    func category(_ id: String) -> XCUIElement { app.buttons[AID.Discover.category(id)] }
    func product(_ id: String) -> XCUIElement { app.buttons[AID.Discover.product(id)] }

    @discardableResult
    func openProduct(_ id: String) -> ProductScreen {
        product(id).waitToAppear().tap()
        return ProductScreen(app: app)
    }
}

struct ProductScreen {
    let app: XCUIApplication

    var name: XCUIElement { app.staticTexts[AID.Product.name] }
    var price: XCUIElement { app.staticTexts[AID.Product.price] }
    var addToCart: XCUIElement { app.buttons[AID.Product.addToCart] }
    var addedToast: XCUIElement { app.staticTexts[AID.Product.addedToast] }

    func back() { app.navigationBars.buttons.element(boundBy: 0).tap() }
}

struct CartScreen {
    let app: XCUIApplication

    var empty: XCUIElement { app.element(AID.Cart.empty) }
    var subtotal: XCUIElement { app.staticTexts[AID.Cart.subtotal] }
    var checkout: XCUIElement { app.buttons[AID.Cart.checkout] }
    func qty(_ id: String) -> XCUIElement { app.staticTexts[AID.Cart.qty(id)] }
    func increment(_ id: String) -> XCUIElement { app.buttons[AID.Cart.increment(id)] }
    func decrement(_ id: String) -> XCUIElement { app.buttons[AID.Cart.decrement(id)] }

    @discardableResult
    func proceedToCheckout() -> CheckoutScreen {
        checkout.tap()
        return CheckoutScreen(app: app)
    }
}

struct CheckoutScreen {
    let app: XCUIApplication

    var root: XCUIElement { app.element(AID.Checkout.screen) }
    var total: XCUIElement { app.staticTexts[AID.Checkout.total] }
    var placeOrder: XCUIElement { app.buttons[AID.Checkout.placeOrder] }
    var error: XCUIElement { app.staticTexts[AID.Checkout.error] }

    func fill(_ id: String, _ text: String) {
        let field = app.textFields[id]
        field.tap()
        field.typeText(text)
        app.dismissKeyboard()
    }

    func fillShipping() {
        fill(AID.Checkout.fullName, TestData.fullName)
        fill(AID.Checkout.address, TestData.address)
        fill(AID.Checkout.city, TestData.city)
        fill(AID.Checkout.zip, TestData.zip)
    }

    func fillCard(_ number: String) {
        fill(AID.Checkout.cardNumber, number)
        fill(AID.Checkout.expiry, "12/30")
        fill(AID.Checkout.cvc, "123")
    }
}

struct ConfirmationScreen {
    let app: XCUIApplication

    var root: XCUIElement { app.element(AID.Confirmation.screen) }
    var orderNumber: XCUIElement { app.staticTexts[AID.Confirmation.orderNumber] }
    var done: XCUIElement { app.buttons[AID.Confirmation.done] }
}

struct ProfileScreen {
    let app: XCUIApplication

    var email: XCUIElement { app.element(AID.Profile.email) }
    var signOut: XCUIElement { app.buttons[AID.Profile.signOut] }
}

/// Seed data from ShopDemo/Services/MockServices.swift.
enum TestData {
    static let email = "demo@shop.com"
    static let password = "password123"
    static let lockedEmail = "locked@shop.com"
    static let successCard = "4242424242424242"
    static let declinedCard = "4000000000000002"
    static let fullName = "Ada Lovelace"
    static let address = "1 Analytical Way"
    static let city = "London"
    static let zip = "10001"
}
