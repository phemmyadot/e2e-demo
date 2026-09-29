import { test, expect, TID } from './fixtures';

test.use({ signedIn: true });

test.describe('Discovery', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('shows the full catalog', async ({ shop }) => {
    for (const id of ['p1', 'p2', 'p3', 'p4', 'p5', 'p6']) {
      await expect(shop.product(id)).toBeVisible();
    }
  });

  test('category filter narrows results', async ({ shop }) => {
    await shop.category('audio').click();

    await expect(shop.category('audio')).toHaveAttribute('aria-pressed', 'true');
    await expect(shop.product('p1')).toBeVisible();
    await expect(shop.product('p2')).toBeVisible();
    await expect(shop.product('p3')).toBeHidden();

    await shop.category('all').click();
    await expect(shop.product('p3')).toBeVisible();
  });

  test('search filters by name and shows an empty state', async ({ page, shop }) => {
    const search = page.getByRole('searchbox', { name: 'Search products' });

    await search.fill('watch');
    await expect(shop.product('p3')).toBeVisible();
    await expect(shop.product('p1')).toBeHidden();

    await search.fill('zzz');
    await expect(page.getByTestId(TID.discover.empty)).toHaveText('No products found');
  });

  test('product detail page is pre-rendered with name, price and description', async ({ page, shop }) => {
    await shop.product('p1').click();

    await expect(page).toHaveURL(/\/product\/p1$/);
    await expect(page.getByTestId(TID.product.name)).toHaveText('Wireless Headphones');
    await expect(page.getByTestId(TID.product.price)).toHaveText('$199.99');
    await expect(page.getByTestId(TID.product.addToCart)).toBeEnabled();
  });
});
