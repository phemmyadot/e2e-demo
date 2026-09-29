import XCTest
@testable import ShopDemo

/// XCTest-style unit tests. New unit tests should prefer Swift Testing (see CartStoreTests);
/// this class shows both frameworks living in the same target.
final class MockAuthServiceXCTests: XCTestCase {
    private var sut: MockAuthService!

    override func setUp() {
        super.setUp()
        MockLatency.nanoseconds = 0
        sut = MockAuthService()
    }

    func testValidCredentialsReturnUser() async throws {
        let user = try await sut.signIn(email: "demo@shop.com", password: "password123")
        XCTAssertEqual(user.email, "demo@shop.com")
        XCTAssertEqual(user.name, "Demo Shopper")
    }

    func testWrongPasswordThrowsInvalidCredentials() async {
        do {
            _ = try await sut.signIn(email: "demo@shop.com", password: "nope")
            XCTFail("Expected invalidCredentials")
        } catch {
            XCTAssertEqual(error as? AuthError, .invalidCredentials)
        }
    }

    func testLockedAccountThrowsLocked() async {
        do {
            _ = try await sut.signIn(email: "locked@shop.com", password: "password123")
            XCTFail("Expected locked")
        } catch {
            XCTAssertEqual(error as? AuthError, .locked)
            XCTAssertEqual(error.localizedDescription, "This account is locked")
        }
    }
}
