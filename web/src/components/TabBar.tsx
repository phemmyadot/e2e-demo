import { useStore } from '@nanostores/react';
import { $cart } from '../lib/stores';
import { TID } from '../lib/testIds';

const TABS = [
  { href: '/', label: 'Discover', icon: '🔍', testId: TID.tab.discover },
  { href: '/cart', label: 'Cart', icon: '🛒', testId: TID.tab.cart },
  { href: '/profile', label: 'Profile', icon: '👤', testId: TID.tab.profile },
];

export default function TabBar({ active }: { active: string }) {
  const count = useStore($cart).reduce((n, i) => n + i.quantity, 0);

  return (
    <nav className="tabbar" aria-label="Main">
      {TABS.map((tab) => (
        <a
          key={tab.href}
          href={tab.href}
          data-testid={tab.testId}
          aria-current={active === tab.href ? 'page' : undefined}
        >
          <span className="icon" aria-hidden>{tab.icon}</span>
          {tab.label}
          {tab.href === '/cart' && count > 0 && (
            <span className="badge" data-testid={TID.tab.cartBadge} aria-label={`${count} items`}>
              {count}
            </span>
          )}
        </a>
      ))}
    </nav>
  );
}
