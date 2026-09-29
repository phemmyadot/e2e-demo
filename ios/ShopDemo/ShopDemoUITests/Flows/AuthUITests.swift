import XCTest

final class AuthUITests: ShopDemoUITestCase {
    func testColdLaunchShowsLoginNotStore() {
        launch()

        login.root.waitToAppear()
        XCTAssertTrue(app.staticTexts["Welcome to ShopDemo"].exists)
        XCTAssertFalse(app.tabBars.firstMatch.exists)
    }

    func testSignInDisabledUntilBothFieldsFilled() {
        launch()

        XCTAssertFalse(login.submit.waitToAppear().isEnabled)
        login.email.tap()
        login.email.typeText(TestData.email)
        XCTAssertFalse(login.submit.isEnabled)
        login.password.tap()
        login.password.typeText(TestData.password)
        XCTAssertTrue(login.submit.isEnabled)
    }

    func testWrongPasswordShowsError() {
        launch()

        login.attemptSignIn(password: "nope")

        XCTAssertEqual(login.error.waitToAppear().label, "Invalid email or password")
        XCTAssertTrue(login.root.exists)
    }

    func testLockedAccountShowsError() {
        launch()

        login.attemptSignIn(email: TestData.lockedEmail)

        XCTAssertEqual(login.error.waitToAppear().label, "This account is locked")
    }

    func testSignInIsDisabledWhileRequestInFlight() {
        launch(latencyMs: 3_000)

        login.enter(email: TestData.email, password: TestData.password)
        login.submit.tap()

        XCTAssertFalse(login.submit.isEnabled)
        XCTAssertTrue(login.root.exists)
        discover.root.waitToAppear()
    }

    func testSignInThenSignOut() {
        launch()

        login.signIn()
        tabBar.open("Profile")
        XCTAssertTrue(profile.email.waitToAppear().label.contains(TestData.email))

        profile.signOut.tap()
        login.root.waitToAppear()
    }
}
