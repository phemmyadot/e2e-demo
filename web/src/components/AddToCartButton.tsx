import { useState } from 'react';
import { addToCart } from '../lib/stores';
import { TID } from '../lib/testIds';

export default function AddToCartButton({ productId }: { productId: string }) {
  const [added, setAdded] = useState(false);

  return (
    <div className="stack">
      {added && (
        <p role="status" className="success" data-testid={TID.product.addedToast}>
          Added to cart
        </p>
      )}
      <button
        type="button"
        className="block"
        onClick={() => { addToCart(productId); setAdded(true); }}
        data-testid={TID.product.addToCart}
      >
        Add to Cart
      </button>
    </div>
  );
}
