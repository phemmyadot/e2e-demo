# ShopDemo Web — Astro + React + Playwright POC

The same mocked shop as `../ios` and `../android` (login gate → discovery → cart → checkout), built with **Astro** (pages and static generation) and **React islands** (interactive parts), and tested end to end with **Playwright**.

```
src/
  pages/               login · index (discover) · product/[id] (pre-rendered) · cart · checkout · confirmation · profile
  layouts/Layout.astro auth gate (inline <head> script) + tab bar
  components/*.tsx     React islands (client:only)
  lib/services.ts      mock backend: same accounts, products and magic cards as mobile
  lib/stores.ts        nanostores + localStorage: session & cart shared across islands/pages
  lib/testIds.ts       data-testid values: same names as iOS AID / Android TestTags
tests/
  fixtures.ts          page objects + fixtures (latencyMs, signedIn)
  auth · discovery · checkout .spec.ts
```

## Commands

```bash
npm install
npx playwright install chromium webkit   # one time

npm run dev              # http://localhost:4321  (login: demo@shop.com / password123)
npm test                 # builds, serves the production build, runs Chromium + iPhone WebKit
npm run test:ui          # Playwright UI mode
npm run test:headed      # watch it in a real Chromium window
npm run report           # HTML report with traces, screenshots and video of failures
```

## Test design

- **Real production build.** `webServer` runs `astro build` and then `astro preview`, so tests hit the same static output that ships.
- **Two browser engines.** The `chromium` project runs Desktop Chrome and the `mobile-safari` project runs iPhone 15 on WebKit. The WebKit run is the closest a web test gets to your iOS users.
- **State seeding.** The `signedIn` and `latencyMs` fixtures write to `localStorage` through `page.addInitScript` before any page script runs. This is the web equivalent of the iOS launch arguments and Android intent extras. The session is seeded once per test, guarded by a `sessionStorage` flag, so signing out stays signed out across navigations.
- **Loading states.** Tests run with latency set to 0. The spinner test raises it with `test.use({ latencyMs: 1_500 })` so the loading state stays visible long enough to check.
- **Locators.** Tests find elements with `getByTestId` using the shared IDs, and use role-based locators (`getByRole('searchbox')`, `getByRole('alert')`) where that's more meaningful.

## Gotchas this POC hit

- **`astro preview` backgrounds itself under AI agents.** Astro 7 detects when an AI coding agent runs it and starts the preview server as a background daemon. The command then exits right away, and Playwright reports "webServer exited early". The `webServer` command passes `--ignore-lock`, which turns that auto-backgrounding off.
- **`client:only` islands.** Islands that read `localStorage` (cart, session) use `client:only="react"`. Server-rendering them would produce the empty-cart state and then cause a hydration mismatch. Product pages stay static HTML; only the Add to Cart button is an island.
- **Auth gate before first paint.** An inline `<head>` script redirects to `/login?next=…` when there's no session, so protected pages never flash.
- **Money in integer cents.** Prices are stored as integer cents to avoid floating-point rounding. This is the web version of the `Decimal` bug the iOS unit tests caught.
