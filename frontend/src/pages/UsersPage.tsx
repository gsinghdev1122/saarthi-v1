import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

const ROLES = ['ADMIN', 'CANTEEN_MANAGER', 'STORE_SUPERVISOR', 'FINANCE_REVIEWER', 'AUDITOR'];

export default function UsersPage() {
  const qc = useQueryClient();
  const users = useQuery({ queryKey: ['users'], queryFn: api.listUsers });
  const [form, setForm] = useState({ username: '', password: '', displayName: '', role: 'AUDITOR' });
  const mutation = useMutation({
    mutationFn: () => api.createUser(form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['users'] });
      setForm({ username: '', password: '', displayName: '', role: 'AUDITOR' });
    },
  });

  return (
    <div>
      <h1>Users</h1>
      <p style={{ color: '#6b7566', fontSize: 13 }}>Admin-only. Create desk logins for the operations team.</p>

      <div className="card">
        <h3>Add user</h3>
        <form className="inline-form" onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }}>
          <div><label>Username</label><input required value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} /></div>
          <div><label>Display name</label><input required value={form.displayName} onChange={(e) => setForm({ ...form, displayName: e.target.value })} /></div>
          <div><label>Password</label><input required type="password" minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
          <div>
            <label>Role</label>
            <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              {ROLES.map((r) => <option key={r} value={r}>{r.replace('_', ' ')}</option>)}
            </select>
          </div>
          <button className="primary" type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Saving…' : 'Create user'}</button>
        </form>
        {mutation.isError && (
          <p className="error-banner">
            {(mutation.error as any)?.response?.data?.message || 'Could not create the user (duplicate username?).'}
          </p>
        )}
      </div>

      <div className="card">
        <table>
          <thead><tr><th>Username</th><th>Display name</th><th>Role</th><th>Status</th></tr></thead>
          <tbody>
            {users.data?.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td>
                <td>{u.displayName}</td>
                <td>{u.role.replace('_', ' ')}</td>
                <td><span className={`pill ${u.enabled ? 'active' : 'inactive'}`}>{u.enabled ? 'active' : 'disabled'}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
