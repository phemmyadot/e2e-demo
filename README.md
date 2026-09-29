# ShopDemo — test stack POC (iOS · Android · Web)

The same small e-commerce app (login gate → discovery → cart → checkout, all backend calls mocked), built natively for each platform and covered by that platform's standard test stack.

| | iOS — [`ios/`](ios/README.md) | Android — [`android/`](android/README.md) | Web — [`web/`](web/README.md) |
|---|---|---|---|
| Stack | Swift, SwiftUI | Kotlin; Views (login) + Compose | Astro + React islands |
| Unit | Swift Testing, XCTest | JUnit 4, Mockito / mockito-kotlin | — |
| Component | ViewInspector | Espresso, Compose UI Test | — |
| E2E | XCUITest | Espresso + Compose flows | Playwright (Chromium + iPhone WebKit) |
| Performance | — | UI Automator: baseline profile + macrobenchmarks | — |
| Run app | `ios/scripts/run-app.sh` | `android/scripts/run-app.sh` | `cd web && npm run dev` |
| Run tests | `ios/scripts/test.sh [unit\|ui\|all]` | `android/scripts/test.sh [unit\|ui\|baseline\|benchmark\|all]` | `cd web && npm test` |

All three share the same seed data (`demo@shop.com` / `password123`, card `4242 4242 4242 4242`) and the same element IDs (`cart.subtotal`, `checkout.placeOrder`, …), so a scenario reads the same on every platform.
