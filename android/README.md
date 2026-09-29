# ShopDemo Android — test stack POC

The same mocked shop as `../ios` (login gate → discovery → cart → checkout), written in Kotlin and covered by the standard Android test stack:

| Layer | Framework | Where | Needs device |
|---|---|---|---|
| Unit | **JUnit 4** + **Mockito / mockito-kotlin** | `app/src/test/` | no |
| View-based UI | **Espresso** | `app/src/androidTest/.../espresso/` | yes |
| Compose UI | **Compose UI Test** | `app/src/androidTest/.../compose/` | yes |
| End-to-end flows | Espresso + Compose UI Test (hybrid) | `app/src/androidTest/.../e2e/` | yes |
| Baseline profile + macrobenchmarks | **AndroidX Test + UI Automator** | `benchmark/` | yes |

The app is deliberately **hybrid**. Login is a classic View-based Activity (XML, ViewBinding and a ViewModel) and everything after it is Compose. That way Espresso and Compose UI Test each have real screens to cover, and the end-to-end tests show both working in one flow.

```
app/src/main/java/com/example/shopdemo/
  data/        models, mock services (auth/catalog/checkout), repositories, ServiceLocator
  testing/     TestTags (Compose tags = iOS AIDs), LaunchConfig (intent extras)
  ui/login/    LoginActivity (Views) + LoginViewModel
  ui/main/     MainActivity (Compose), nav + bottom bar
  ui/…         discover · product · cart · checkout · profile (stateless *Content composables)
benchmark/     UI Automator journeys, BaselineProfileGenerator, StartupBenchmark
```

## Commands

```bash
scripts/run-app.sh            # boots ShopDemo_Pixel_API_34 if needed, installs, launches

scripts/test.sh unit          # ~20s, no device
scripts/test.sh ui            # ~1.5 min on the emulator
scripts/test.sh baseline      # regenerate the baseline profile
scripts/test.sh benchmark     # startup + frame timing
```

Stack: AGP 9.4 (built-in Kotlin), Kotlin 2.4, Gradle 9.8, compileSdk 37, minSdk 26, Compose BOM 2026.09.

## Mock data

Same as iOS: `demo@shop.com` / `password123`, locked account `locked@shop.com`, card `4242…4242` succeeds, `4000…0002` is declined, and the six products are `p1`–`p6`.

## Conventions and gotchas this POC hit

- **Test tags match the iOS IDs.** Compose `testTag`s in `TestTags.kt` use the same names as the iOS `AID` values, such as `cart.subtotal`. The View-based login uses `R.id.login_*`.
- **UI Automator sees Compose tags.** The root sets `Modifier.semantics { testTagsAsResourceId = true }`, which exposes the tags as resource IDs, so `By.res("cart.checkout")` works.
- **`ListItem` merges semantics.** A tag on a `Text` inside a Material3 `ListItem` is only in the unmerged tree, so tests use `onNodeWithTag(tag, useUnmergedTree = true)`. This is the Compose version of the SwiftUI container-ID problem.
- **Stateless screens.** Each screen has a stateless `…Content` composable that Compose UI tests render with fake state. The stateful wrapper owns the ViewModel.
- **Test state.** Instrumented tests run in the app's process, so they call `resetApp()` (`ServiceLocator.reset()`) before launching. Macrobenchmarks run in a separate process and use intent extras instead: `uitest.loggedIn` and `uitest.latencyMs`, the equivalent of the iOS launch arguments.
- **PowerMock is not included.** PowerMock 2.x supports only Mockito 2 and 3, and it can't share a classpath with Mockito 5. `MockServicesTest` shows the replacement, `Mockito.mockStatic(...)`, which works out of the box with Mockito 5's inline mock maker. If legacy PowerMock tests have to stay, they need their own module pinned to Mockito 3.
- **UI Automator journeys must fail loudly.** An early version used `findObject(...)?.click()`, which silently did nothing when the element had scrolled off screen, and the failure only surfaced three steps later. `waitAndClick(res)` fails immediately and names the missing ID.
- **Emulator benchmarks.** Benchmarks on an emulator are indicative only (`suppressErrors=EMULATOR`). The POC measured a cold-start median of 386 ms without the profile and 370 ms with it. Use a physical device for real numbers.
