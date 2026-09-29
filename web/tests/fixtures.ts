import { test as base, expect, type Page } from '@playwright/test';
import { TID } from '../src/lib/testIds';
import { DEMO_USER, LATENCY_KEY } from '../src/lib/services';
import { SESSION_KEY } from '../src/lib/stores';

export { expect, TID };

export const DATA = {
  email: 'demo@shop.com',
  password: 'password123',
  lockedEmail: 'locked@shop.com',
  successCard: '4242 4242 4242 4242',
  declinedCard: '4000 0000 0000 0002',
  shipping: { fullName: 'Ada Lovelace', address: '1 Analytical Way', city: 'London', zip: '10001' },
};

// region Page objects

export class LoginPage {
  constructor(readonly page: Page) {}
  readonly email = this.page.getByTestId(TID.login.email);
  readonly password = this.page.getByTestId(TID.login.password);
  readonly submit = this.page.getByTestId(TID.login.submit);
  readonly error = this.page.getByTestId(TID.login.error);

  goto = () => this.page.goto('/login');

  async signIn(email = DATA.email, password = DATA.password) {
    await this.email.fill(email);
    await this.password.fill(password);
    await this.submit.click();
  }
}

export class ShopPages {
  constructor(readonly page: Page) {}

  tab = (name: 'discover' | 'cart' | 'profile') => this.page.getByTestId(TID.tab[name]);
  product = (id: string) => this.page.getByTestId(TID.discover.product(id));
  category = (id: string) => this.page.getByTestId(TID.discover.category(id));

  async addToCart(productId: string) {
    await this.page.goto(`/product/${productId}`);
    await this.page.getByTestId(TID.product.addToCart).click();
    await expect(this.page.getByTestId(TID.product.addedToast)).toBeVisible();
  }

  async openCheckout() {
    await this.tab('cart').click();
    await this.page.getByTestId(TID.cart.checkout).click();
    await expect(this.page.getByTestId(TID.checkout.screen)).toBeVisible();
  }

  async fillShipping(shipping = DATA.shipping) {
    await this.page.getByTestId(TID.checkout.fullName).fill(shipping.fullName);
    await this.page.getByTestId(TID.checkout.address).fill(shipping.address);
    await this.page.getByTestId(TID.checkout.city).fill(shipping.city);
    await this.page.getByTestId(TID.checkout.zip).fill(shipping.zip);
  }

  async fillCard(number: string, expiry = '12/30', cvc = '123') {
    await this.page.getByTestId(TID.checkout.cardNumber).fill(number);
    await this.page.getByTestId(TID.checkout.expiry).fill(expiry);
    await this.page.getByTestId(TID.checkout.cvc).fill(cvc);
  }
}

// endregion

// region Fixtures

type Options = {
  /** Mock backend latency. 0 keeps tests fast; raise it to test loading states. */
  latencyMs: number;
  /** Start with a session, skipping the login UI (like iOS `-uitest.loggedIn`). */
  signedIn: boolean;
};

export const test = base.extend<Options & { login: LoginPage; shop: ShopPages }>({
  latencyMs: [0, { option: true }],
  signedIn: [false, { option: true }],

  page: async ({ page, latencyMs, signedIn }, use) => {
    await page.addInitScript(
      ({ latencyKey, latencyMs, sessionKey, user }) => {
        localStorage.setItem(latencyKey, String(latencyMs));
        // Seed the session once per test, so a later sign-out sticks across navigations.
        if (user && !sessionStorage.getItem('shopdemo:seeded')) {
          localStorage.setItem(sessionKey, JSON.stringify(user));
          sessionStorage.setItem('shopdemo:seeded', '1');
        }
      },
      { latencyKey: LATENCY_KEY, latencyMs, sessionKey: SESSION_KEY, user: signedIn ? DEMO_USER : null },
    );
    await use(page);
  },

  login: async ({ page }, use) => use(new LoginPage(page)),
  shop: async ({ page }, use) => use(new ShopPages(page)),
});

// endregion
