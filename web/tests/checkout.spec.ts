import { test, expect, TID, DATA } from './fixtures';

test.use({ signedIn: true });

test.describe('Cart & checkout', () => {
  test('empty cart shows empty state', async ({ page, shop }) => {
    await page.goto('/');
    await shop.tab('cart').click();

    await expect(page.getByTestId(TID.cart.empty)).toBeVisible();
    await expect(page.getByTestId(TID.cart.checkout)).toHaveCount(0);
  });

  test('quantity controls update subtotal and badge', async ({ page, shop }) => {
    await shop.addToCart('p2'); // $89.00
    await expect(page.getByTestId(TID.tab.cartBadge)).toHaveText('1');
    await shop.tab('cart').click();

    await expect(page.getByTestId(TID.cart.qty('p2'))).toHaveText('1');
    await expect(page.getByTestId(TID.cart.subtotal)).toHaveText('$89.00');

    await page.getByTestId(TID.cart.increment('p2')).click();
    await expect(page.getByTestId(TID.cart.qty('p2'))).toHaveText('2');
    await expect(page.getByTestId(TID.cart.subtotal)).toHaveText('$178.00');
    await expect(page.getByTestId(TID.tab.cartBadge)).toHaveText('2');

    await page.getByTestId(TID.cart.decrement('p2')).click();
    await page.getByTestId(TID.cart.decrement('p2')).click();
    await expect(page.getByTestId(TID.cart.empty)).toBeVisible();
  });

  test('cart survives a reload', async ({ page, shop }) => {
    await shop.addToCart('p5');
    await page.goto('/cart');
    await page.reload();

    await expect(page.getByTestId(TID.cart.qty('p5'))).toHaveText('1');
  });

  test('happy path: browse → add → checkout → confirmation', async ({ page, shop }) => {
    await shop.addToCart('p1'); // $199.99
    await shop.addToCart('p5'); // $45.00

    await shop.openCheckout();
    await expect(page.getByTestId(TID.checkout.total)).toHaveText('$244.99');
    await shop.fillShipping();
    await shop.fillCard(DATA.successCard);
    await page.getByTestId(TID.checkout.placeOrder).click();

    await expect(page.getByTestId(TID.confirmation.screen)).toBeVisible();
    await expect(page.getByTestId(TID.confirmation.orderNumber)).toHaveText(/^ORD-\d{6}$/);
    await expect(page.getByText('Total paid: $244.99')).toBeVisible();

    await page.getByTestId(TID.confirmation.done).click();
    await expect(page.getByTestId(TID.cart.empty)).toBeVisible();
    await expect(page.getByTestId(TID.tab.cartBadge)).toHaveCount(0);
  });

  test('declined card shows an error and keeps the cart', async ({ page, shop }) => {
    await shop.addToCart('p3');
    await shop.openCheckout();
    await shop.fillShipping();
    await shop.fillCard(DATA.declinedCard);
    await page.getByTestId(TID.checkout.placeOrder).click();

    await expect(page.getByTestId(TID.checkout.error)).toHaveText('Your card was declined');
    await expect(page.getByTestId(TID.tab.cartBadge)).toHaveText('1');
  });

  test('missing shipping details are validated', async ({ page, shop }) => {
    await shop.addToCart('p4');
    await shop.openCheckout();
    await page.getByTestId(TID.checkout.placeOrder).click();

    await expect(page.getByRole('alert')).toHaveText('Please complete your shipping details');
  });
});
