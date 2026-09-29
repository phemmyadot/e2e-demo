# ShopDemo — mobile test stack POC

The same small e-commerce app (login gate → discovery → cart → checkout, all backend calls mocked), built natively on both platforms, each covered by its platform's standard test stack.

| | iOS — [`ios/`](ios/README.md) | Android — [`android/`](android/README.md) |
|---|---|---|
| Language / UI | Swift, SwiftUI | Kotlin; Views (login) + Compose (store) |
| Unit | Swift Testing (preferred), XCTest | JUnit 4, Mockito / mockito-kotlin |
| View / component | ViewInspector | Espresso (Views), Compose UI Test |
| UI / E2E | XCUITest | Espresso + Compose UI Test flows |
| Performance | — | UI Automator: baseline profile + macrobenchmarks |
| Run app | `ios/scripts/run-app.sh` | `android/scripts/run-app.sh` |
| Run tests | `ios/scripts/test.sh [unit\|ui\|all]` | `android/scripts/test.sh [unit\|ui\|baseline\|benchmark\|all]` |

Both apps share the same seed data (`demo@shop.com` / `password123`, card `4242 4242 4242 4242`) and the same element IDs (`cart.subtotal`, `checkout.placeOrder`, …), so a scenario reads the same way on either platform.
