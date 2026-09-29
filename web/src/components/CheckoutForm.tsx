import { useStore } from '@nanostores/react';
import { useState, type FormEvent } from 'react';
import { formatPrice, placeOrder, subtotalCents, type Card, type Shipping } from '../lib/services';
import { $cart, clearCart } from '../lib/stores';
import { TID } from '../lib/testIds';

type FieldDef<T> = { key: keyof T; label: string; testId: string; inputMode?: 'numeric'; autoComplete?: string };

const SHIPPING_FIELDS: FieldDef<Shipping>[] = [
  { key: 'fullName', label: 'Full name', testId: TID.checkout.fullName, autoComplete: 'name' },
  { key: 'address', label: 'Address', testId: TID.checkout.address, autoComplete: 'street-address' },
  { key: 'city', label: 'City', testId: TID.checkout.city, autoComplete: 'address-level2' },
  { key: 'zip', label: 'ZIP', testId: TID.checkout.zip, inputMode: 'numeric', autoComplete: 'postal-code' },
];
const CARD_FIELDS: FieldDef<Card>[] = [
  { key: 'number', label: 'Card number', testId: TID.checkout.cardNumber, inputMode: 'numeric', autoComplete: 'cc-number' },
  { key: 'expiry', label: 'MM/YY', testId: TID.checkout.expiry, autoComplete: 'cc-exp' },
  { key: 'cvc', label: 'CVC', testId: TID.checkout.cvc, inputMode: 'numeric', autoComplete: 'cc-csc' },
];

export default function CheckoutForm() {
  const items = useStore($cart);
  const [shipping, setShipping] = useState<Shipping>({ fullName: '', address: '', city: '', zip: '' });
  const [card, setCard] = useState<Card>({ number: '', expiry: '', cvc: '' });
  const [error, setError] = useState<string | null>(null);
  const [placing, setPlacing] = useState(false);

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setPlacing(true);
    try {
      const order = await placeOrder(items, shipping, card);
      clearCart();
      location.assign(`/confirmation?order=${order.id}&total=${order.totalCents}`);
    } catch (err) {
      setError((err as Error).message);
      setPlacing(false);
    }
  }

  const field = <T,>(def: FieldDef<T>, value: T, set: (v: T) => void) => (
    <div key={String(def.key)}>
      <label htmlFor={def.testId}>{def.label}</label>
      <input
        id={def.testId}
        inputMode={def.inputMode}
        autoComplete={def.autoComplete}
        value={String(value[def.key])}
        onChange={(e) => set({ ...value, [def.key]: e.target.value })}
        data-testid={def.testId}
      />
    </div>
  );

  return (
    <form onSubmit={submit} className="stack" data-testid={TID.checkout.screen} noValidate>
      <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
        <legend><strong>Shipping</strong></legend>
        {SHIPPING_FIELDS.map((f) => field(f, shipping, setShipping))}
      </fieldset>
      <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
        <legend><strong>Payment</strong></legend>
        {CARD_FIELDS.map((f) => field(f, card, setCard))}
        <p className="muted" style={{ fontSize: 13 }}>
          Test cards: 4242 4242 4242 4242 (success), 4000 0000 0000 0002 (declined)
        </p>
      </fieldset>
      <div className="card total">
        <span>Total</span>
        <span data-testid={TID.checkout.total}>{formatPrice(subtotalCents(items))}</span>
      </div>
      {error && <p role="alert" className="error" data-testid={TID.checkout.error}>{error}</p>}
      <button type="submit" className="block" disabled={placing} data-testid={TID.checkout.placeOrder}>
        {placing ? <span className="spinner" aria-label="Placing order" /> : 'Place Order'}
      </button>
    </form>
  );
}
