// Mocked backend. Same accounts, catalog and magic cards as the iOS and Android apps.
// Prices are integer cents to avoid floating-point money bugs.

export interface User { id: string; name: string; email: string }
export interface Category { id: string; name: string }
export interface Product {
  id: string; name: string; categoryId: string; priceCents: number; summary: string; emoji: string;
}
export interface CartItem { productId: string; quantity: number }
export interface Shipping { fullName: string; address: string; city: string; zip: string }
export interface Card { number: string; expiry: string; cvc: string }
export interface Order { id: string; totalCents: number }

export const formatPrice = (cents: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(cents / 100);

/**
 * Artificial latency so tests exercise real loading states. Playwright overrides it through
 * localStorage (`shopdemo:latencyMs`) before the page loads — see tests/fixtures.ts.
 */
export const LATENCY_KEY = 'shopdemo:latencyMs';
const latency = () => {
  const override = typeof localStorage !== 'undefined' ? localStorage.getItem(LATENCY_KEY) : null;
  return override === null ? 600 : Number(override);
};
const wait = () => new Promise((r) => setTimeout(r, latency()));

// region Auth

export const DEMO_USER: User = { id: 'u_1', name: 'Demo Shopper', email: 'demo@shop.com' };
const ACCOUNTS: Record<string, string> = {
  'demo@shop.com': 'password123',
  'locked@shop.com': 'password123',
};

export class AuthError extends Error {}

export async function signIn(email: string, password: string): Promise<User> {
  await wait();
  const normalized = email.trim().toLowerCase();
  if (ACCOUNTS[normalized] !== password) throw new AuthError('Invalid email or password');
  if (normalized === 'locked@shop.com') throw new AuthError('This account is locked');
  return { ...DEMO_USER, email: normalized };
}

// endregion

// region Catalog

export const CATEGORIES: Category[] = [
  { id: 'all', name: 'All' },
  { id: 'audio', name: 'Audio' },
  { id: 'wearables', name: 'Wearables' },
  { id: 'home', name: 'Home' },
];

export const PRODUCTS: Product[] = [
  { id: 'p1', name: 'Wireless Headphones', categoryId: 'audio', priceCents: 19999, emoji: '🎧',
    summary: 'Noise-cancelling over-ear headphones with 30h battery.' },
  { id: 'p2', name: 'Bluetooth Speaker', categoryId: 'audio', priceCents: 8900, emoji: '🔊',
    summary: 'Portable waterproof speaker with deep bass.' },
  { id: 'p3', name: 'Smart Watch', categoryId: 'wearables', priceCents: 24900, emoji: '⌚️',
    summary: 'Fitness tracking, notifications and GPS.' },
  { id: 'p4', name: 'Fitness Band', categoryId: 'wearables', priceCents: 5950, emoji: '🏃',
    summary: 'Lightweight tracker for steps, sleep and heart rate.' },
  { id: 'p5', name: 'Desk Lamp', categoryId: 'home', priceCents: 4500, emoji: '💡',
    summary: 'Dimmable LED lamp with warm and cool modes.' },
  { id: 'p6', name: 'Coffee Maker', categoryId: 'home', priceCents: 12900, emoji: '☕️',
    summary: 'Programmable drip coffee maker, 12 cups.' },
];

export const productById = (id: string) => PRODUCTS.find((p) => p.id === id);

export async function searchProducts(query: string, categoryId: string): Promise<Product[]> {
  await wait();
  const q = query.trim().toLowerCase();
  return PRODUCTS.filter(
    (p) => (categoryId === 'all' || p.categoryId === categoryId) && (!q || p.name.toLowerCase().includes(q)),
  );
}

// endregion

// region Checkout

export const SUCCESS_CARD = '4242424242424242';
export const DECLINED_CARD = '4000000000000002';

export class CheckoutError extends Error {}

export const subtotalCents = (items: CartItem[]) =>
  items.reduce((sum, i) => sum + (productById(i.productId)?.priceCents ?? 0) * i.quantity, 0);

export async function placeOrder(items: CartItem[], shipping: Shipping, card: Card): Promise<Order> {
  if (Object.values(shipping).some((v) => !v.trim())) {
    throw new CheckoutError('Please complete your shipping details');
  }
  const digits = card.number.replace(/\D/g, '');
  if (digits.length !== 16 || card.expiry.length < 4 || card.cvc.length < 3) {
    throw new CheckoutError('Please enter a valid card');
  }
  await wait();
  if (digits === DECLINED_CARD) throw new CheckoutError('Your card was declined');
  return { id: `ORD-${Math.floor(100000 + Math.random() * 900000)}`, totalCents: subtotalCents(items) };
}

// endregion
