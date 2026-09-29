import { useStore } from '@nanostores/react';
import { $session, signOut } from '../lib/stores';
import { TID } from '../lib/testIds';

export default function Profile() {
  const user = useStore($session);

  return (
    <div className="stack" data-testid={TID.profile.screen}>
      <div className="card">
        <div className="row"><span className="grow">Name</span><span className="muted">{user?.name ?? '—'}</span></div>
        <div className="row">
          <span className="grow">Email</span>
          <span className="muted" data-testid={TID.profile.email}>{user?.email ?? '—'}</span>
        </div>
      </div>
      <button
        type="button"
        className="block danger"
        onClick={() => { signOut(); location.assign('/login'); }}
        data-testid={TID.profile.signOut}
      >
        Sign Out
      </button>
    </div>
  );
}
