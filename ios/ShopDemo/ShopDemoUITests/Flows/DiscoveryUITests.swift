import XCTest

final class DiscoveryUITests: ShopDemoUITestCase {
    override func setUpWithError() throws {
        try super.setUpWithError()
        launch(loggedIn: true)
        discover.root.waitToAppear()
    }

    func testShowsFullCatalog() {
        for id in ["p1", "p2", "p3", "p4", "p5", "p6"] {
            XCTAssertTrue(discover.product(id).waitForExistence(timeout: defaultTimeout), "missing \(id)")
        }
    }

    func testCategoryFilterNarrowsResults() {
        discover.category("audio").tap()

        discover.product("p1").waitToAppear()
        XCTAssertTrue(discover.product("p2").exists)
        discover.product("p3").waitToDisappear()

        discover.category("all").tap()
        discover.product("p3").waitToAppear()
    }

    func testSearchFiltersAndShowsEmptyState() {
        discover.search.waitToAppear().tap()
        discover.search.typeText("watch")

        discover.product("p3").waitToAppear()
        discover.product("p1").waitToDisappear()

        discover.search.replaceText("zzz")
        XCTAssertEqual(discover.empty.waitToAppear().label, "No products found")
    }

    func testProductDetailShowsInfo() {
        discover.openProduct("p1")

        XCTAssertEqual(product.name.waitToAppear().label, "Wireless Headphones")
        XCTAssertEqual(product.price.label, "$199.99")
        XCTAssertTrue(product.addToCart.isEnabled)
    }
}
