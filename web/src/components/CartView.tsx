import { useStore } from '@nanostores/react';
import { formatPrice, productById, subtotalCents } from '../lib/services';
import { $cart, changeQuantity } from '../lib/stores';
import { TID } from '../lib/testIds';

export default function CartView() {
  const items = useStore($cart);

  if (items.length === 0) {
    return (
      <div className="card center stack" style={{ padding: 32 }} data-testid={TID.cart.empty}>
        <p><strong>Your cart is empty</strong></p>
        <p className="muted">Browse Discover to add products.</p>
      </div>
    );
  }

  return (
    <div className="stack">
      <ul className="card" style={{ listStyle: 'none', margin: 0, padding: 0 }} aria-label="Cart items">
        {items.map((item) => {
          const product = productById(item.productId);
          if (!product) return null;
          return (
            <li key={item.productId} className="row" data-testid={TID.cart.row(product.id)}>
              <span className="grow">
                <strong>{product.name}</strong>
                <br />
                <span className="muted">{formatPrice(product.priceCents * item.quantity)}</span>
              </span>
              <span className="qty">
                <button
                  type="button"
                  className="ghost"
                  aria-label={`Decrease ${product.name}`}
                  onClick={() => changeQuantity(product.id, -1)}
                  data-testid={TID.cart.decrement(product.id)}
                >−</button>
                <span data-testid={TID.cart.qty(product.id)}>{item.quantity}</span>
                <button
                  type="button"
                  className="ghost"
                  aria-label={`Increase ${product.name}`}
                  onClick={() => changeQuantity(product.id, 1)}
                  data-testid={TID.cart.increment(product.id)}
                >+</button>
              </span>
            </li>
          );
        })}
      </ul>
      <div className="card total">
        <span>Subtotal</span>
        <span data-testid={TID.cart.subtotal}>{formatPrice(subtotalCents(items))}</span>
      </div>
      <a className="button block" href="/checkout" data-testid={TID.cart.checkout}>Checkout</a>
    </div>
  );
}
