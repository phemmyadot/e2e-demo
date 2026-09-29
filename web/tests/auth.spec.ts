import { test, expect, TID, DATA } from './fixtures';

test.describe('Auth gate', () => {
  test('protected pages redirect to login and return after sign-in', async ({ page, login }) => {
    await page.goto('/cart');

    await expect(page).toHaveURL(/\/login\?next=%2Fcart/);
    await login.signIn();

    await expect(page).toHaveURL(/\/cart$/);
    await expect(page.getByTestId(TID.cart.empty)).toBeVisible();
  });

  test('sign in is disabled until both fields are filled', async ({ login }) => {
    await login.goto();

    await expect(login.submit).toBeDisabled();
    await login.email.fill(DATA.email);
    await expect(login.submit).toBeDisabled();
    await login.password.fill(DATA.password);
    await expect(login.submit).toBeEnabled();
  });

  test('wrong password shows an error', async ({ page, login }) => {
    await login.goto();
    await login.signIn(DATA.email, 'nope');

    await expect(login.error).toHaveText('Invalid email or password');
    await expect(page).toHaveURL(/\/login/);
  });

  test('locked account is rejected', async ({ login }) => {
    await login.goto();
    await login.signIn(DATA.lockedEmail);

    await expect(login.error).toHaveText('This account is locked');
  });

  test.describe('with real latency', () => {
    test.use({ latencyMs: 1_500 });

    test('shows a spinner and disables submit while signing in', async ({ page, login }) => {
      await login.goto();
      await login.signIn();

      await expect(page.getByTestId(TID.login.spinner)).toBeVisible();
      await expect(login.submit).toBeDisabled();
      await expect(page.getByTestId(TID.discover.screen)).toBeVisible();
    });
  });

  test.describe('signed in', () => {
    test.use({ signedIn: true });

    test('profile shows the user and sign out returns to login', async ({ page, shop }) => {
      await page.goto('/');
      await shop.tab('profile').click();
      await expect(page.getByTestId(TID.profile.email)).toHaveText(DATA.email);

      await page.getByTestId(TID.profile.signOut).click();
      await expect(page).toHaveURL(/\/login/);

      // The gate still holds after signing out.
      await page.goto('/profile');
      await expect(page).toHaveURL(/\/login/);
    });
  });
});
