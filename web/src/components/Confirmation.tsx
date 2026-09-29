import { formatPrice } from '../lib/services';
import { TID } from '../lib/testIds';

export default function Confirmation() {
  const params = new URLSearchParams(location.search);
  const orderId = params.get('order') ?? '—';
  const total = Number(params.get('total') ?? 0);

  return (
    <div className="center stack" style={{ paddingTop: 48 }} data-testid={TID.confirmation.screen}>
      <div style={{ fontSize: 64 }} aria-hidden>✅</div>
      <h1>Order confirmed</h1>
      <p><code data-testid={TID.confirmation.orderNumber}>{orderId}</code></p>
      <p className="muted">Total paid: {formatPrice(total)}</p>
      <a className="button block" href="/cart" data-testid={TID.confirmation.done}>Continue Shopping</a>
    </div>
  );
}
