// Shared state across Astro islands (and page navigations) via nanostores + localStorage.
import { persistentAtom } from '@nanostores/persistent';
import type { CartItem, User } from './services';

const json = { encode: JSON.stringify, decode: JSON.parse };

export const SESSION_KEY = 'shopdemo:session';
export const CART_KEY = 'shopdemo:cart';

export const $session = persistentAtom<User | null>(SESSION_KEY, null, json);
export const $cart = persistentAtom<CartItem[]>(CART_KEY, [], json);

export function addToCart(productId: string) {
  const items = $cart.get();
  const existing = items.find((i) => i.productId === productId);
  $cart.set(
    existing
      ? items.map((i) => (i.productId === productId ? { ...i, quantity: i.quantity + 1 } : i))
      : [...items, { productId, quantity: 1 }],
  );
}

export function changeQuantity(productId: string, delta: 1 | -1) {
  $cart.set(
    $cart
      .get()
      .map((i) => (i.productId === productId ? { ...i, quantity: i.quantity + delta } : i))
      .filter((i) => i.quantity > 0),
  );
}

export const clearCart = () => $cart.set([]);

export function signOut() {
  clearCart();
  $session.set(null);
}
