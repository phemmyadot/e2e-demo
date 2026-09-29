import { useEffect, useState } from 'react';
import { CATEGORIES, formatPrice, searchProducts, type Product } from '../lib/services';
import { TID } from '../lib/testIds';

export default function Discover() {
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('all');
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    searchProducts(query, category).then((result) => {
      if (cancelled) return;
      setProducts(result);
      setLoading(false);
    });
    return () => { cancelled = true; };
  }, [query, category]);

  return (
    <section data-testid={TID.discover.screen}>
      <input
        type="search"
        placeholder="Search products"
        aria-label="Search products"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        data-testid={TID.discover.search}
      />
      <div className="chips" role="group" aria-label="Categories" style={{ marginTop: 12 }}>
        {CATEGORIES.map((c) => (
          <button
            key={c.id}
            type="button"
            className="chip"
            aria-pressed={c.id === category}
            onClick={() => setCategory(c.id)}
            data-testid={TID.discover.category(c.id)}
          >
            {c.name}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="center"><span className="spinner" data-testid={TID.discover.loading} aria-label="Loading" /></p>
      ) : products.length === 0 ? (
        <p className="muted" data-testid={TID.discover.empty}>No products found</p>
      ) : (
        <ul className="card" style={{ listStyle: 'none', margin: 0, padding: 0 }} aria-label="Products">
          {products.map((p) => (
            <li key={p.id}>
              <a className="row" href={`/product/${p.id}`} data-testid={TID.discover.product(p.id)}>
                <span className="thumb" aria-hidden>{p.emoji}</span>
                <span className="grow">
                  <strong>{p.name}</strong>
                  <br />
                  <span className="muted">{formatPrice(p.priceCents)}</span>
                </span>
                <span className="muted" aria-hidden>›</span>
              </a>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
