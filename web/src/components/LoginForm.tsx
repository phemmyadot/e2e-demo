import { useState, type FormEvent } from 'react';
import { signIn } from '../lib/services';
import { $session } from '../lib/stores';
import { TID } from '../lib/testIds';

export default function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const canSubmit = email.trim() !== '' && password !== '' && !loading;

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!canSubmit) return;
    setError(null);
    setLoading(true);
    try {
      $session.set(await signIn(email, password));
      const next = new URLSearchParams(location.search).get('next');
      location.assign(next?.startsWith('/') ? next : '/');
    } catch (err) {
      setError((err as Error).message);
      setLoading(false);
    }
  }

  return (
    <form onSubmit={submit} className="stack" data-testid={TID.login.screen} noValidate>
      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          autoComplete="username"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          data-testid={TID.login.email}
        />
      </div>
      <div>
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          data-testid={TID.login.password}
        />
      </div>
      {error && (
        <p role="alert" className="error" data-testid={TID.login.error}>
          {error}
        </p>
      )}
      <button type="submit" className="block" disabled={!canSubmit} data-testid={TID.login.submit}>
        {loading ? <span className="spinner" data-testid={TID.login.spinner} aria-label="Signing in" /> : 'Sign In'}
      </button>
      <p className="muted center">Hint: demo@shop.com / password123</p>
    </form>
  );
}
