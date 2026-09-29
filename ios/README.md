# ShopDemo — iOS test stack POC

A small SwiftUI e-commerce app (login gate → discovery → cart → checkout) with fully mocked services, covered by the standard iOS test stack:

| Layer | Framework | Target | Where |
|---|---|---|---|
| Unit (preferred for new tests) | **Swift Testing** | `ShopDemoTests` | `ShopDemoTests/Unit/*Tests.swift` |
| Unit (legacy style) | **XCTest** | `ShopDemoTests` | `ShopDemoTests/Unit/MockAuthServiceXCTests.swift` |
| SwiftUI view | **ViewInspector** 0.10.3 | `ShopDemoTests` | `ShopDemoTests/Views/ViewInspectorTests.swift` |
| UI / E2E | **XCUITest** | `ShopDemoUITests` | `ShopDemoUITests/Flows/*UITests.swift` |

```
ShopDemo/
  ShopDemo.xcodeproj           shared scheme "ShopDemo" runs both test bundles
  ShopDemo/                    app (SwiftUI, iOS 17+)
    App/                       entry point, auth gate, stores, LaunchConfig
    Features/                  Auth · Discovery · Cart · Checkout · Profile
    Services/                  MockAuth / MockCatalog / MockCheckout
    Support/AccessibilityID.swift   IDs shared by app + UI tests
  ShopDemoTests/               Swift Testing + XCTest + ViewInspector (hosted in app)
  ShopDemoUITests/
    ShopDemoUITestCase.swift   base class: launch args, failure screenshots
    Screens/Screens.swift      page objects
    Flows/                     Auth · Discovery · Checkout
scripts/
  run-app.sh                   build + install + launch on a simulator
  test.sh                      run unit | ui | all
```

## Commands

```bash
scripts/run-app.sh                 # build, install, launch (default: iPhone 17)
scripts/run-app.sh "iPhone 16"     # any simulator name

scripts/test.sh unit               # ShopDemoTests   (~30s)
scripts/test.sh ui                 # ShopDemoUITests (~4 min)
scripts/test.sh all
open ShopDemo/build/TestResults-ui.xcresult   # screenshots, video, hierarchy of failures
```

Or open `ShopDemo/ShopDemo.xcodeproj` and press ⌘U.

## Mock data

| What | Value |
|---|---|
| Valid login | `demo@shop.com` / `password123` |
| Locked account | `locked@shop.com` / `password123` |
| Card that succeeds | `4242 4242 4242 4242` |
| Card that is declined | `4000 0000 0000 0002` |
| Products | 6 items across Audio / Wearables / Home (`p1`–`p6`) |

Mock calls wait 600 ms by default. All state is in memory, so every UI test launch starts clean.

## UI test launch arguments

Set in `ShopDemoUITestCase.launch(loggedIn:latencyMs:)` and read by `LaunchConfig` in the app:

| Argument | Effect |
|---|---|
| `-uitest.loggedIn` | start signed in as the demo user (skips the login UI for non-auth flows) |
| `-uitest.latencyMs <ms>` | mock latency; tests use `0`, the in-flight test uses `3000` |

## Conventions and gotchas this POC hit

- **Accessibility IDs**: every element a test touches has an ID from `AID` in `AccessibilityID.swift`. That file is compiled into the UI test target as well, so the tests and the app can't drift apart.
- **Container IDs**: use `.accessibilityContainer(id)`, not a bare `.accessibilityIdentifier` on a stack. A bare identifier gets copied onto every child and hides the children's own IDs.
- **ViewInspector and `@Observable`**: ViewInspector can't resolve `@Environment(SomeObservable.self)`, and the test crashes with an assertion. Views receive stores as plain properties (`CartView(cart:)`), and only the app root holds them.
- **ViewInspector 0.10.4**: its Package.swift doesn't resolve on Xcode 26 (`.visionOS(.v2)` with tools version 5.9). It's pinned to 0.10.3.
- **"Save Password?" sheet**: after a successful login, iOS shows this sheet over the app. It covers whatever sits under it, and turning off `textContentType` doesn't stop it. `LoginScreen.signIn()` dismisses it with `app.dismissSavePasswordPrompt()` and waits for it to animate away, because taps made during the animation are lost.
- **Loading states**: a `ProgressView` inside a button's label is merged into the button, so XCUITest can't see it. Tests check that the button is disabled instead, using a long mock latency so there's time to check.
- **Money**: prices are built with `Decimal(string:)`. `Decimal` built from a `Double` literal such as `199.99` isn't exact, and a unit test caught this.
