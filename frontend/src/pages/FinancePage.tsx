import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

const inr = (v: number) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);

export default function FinancePage() {
  const qc = useQueryClient();
  const expenses = useQuery({ queryKey: ['expenses'], queryFn: api.listExpenses });
  const approvals = useQuery({ queryKey: ['approvals'], queryFn: api.listApprovals });
  const [form, setForm] = useState({ category: '', vendor: '', amount: '', date: new Date().toISOString().slice(0, 10) });
  const mutation = useMutation({
    mutationFn: () => api.createExpense({ ...form, amount: Number(form.amount) }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['expenses'] });
      qc.invalidateQueries({ queryKey: ['approvals'] });
      setForm({ category: '', vendor: '', amount: '', date: new Date().toISOString().slice(0, 10) });
    },
  });

  return (
    <div>
      <h1>Finance</h1>
      <div className="card">
        <h3>Record expense</h3>
        <form className="inline-form" onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }}>
          <div><label>Category</label><input required value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} /></div>
          <div><label>Vendor</label><input required value={form.vendor} onChange={(e) => setForm({ ...form, vendor: e.target.value })} /></div>
          <div><label>Amount (INR)</label><input required type="number" min="0" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} /></div>
          <div><label>Date</label><input required type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} /></div>
          <button className="primary" type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Saving…' : 'Save expense'}</button>
        </form>
      </div>
      <div className="card">
        <h3>Expense register</h3>
        <table>
          <thead><tr><th>Category / vendor</th><th>Date</th><th>Amount</th><th>Status</th></tr></thead>
          <tbody>
            {expenses.data?.map((e) => (
              <tr key={e.id}><td>{e.category} — {e.vendor}</td><td>{e.date}</td><td>{inr(e.amount)}</td><td><span className={`pill ${e.status}`}>{e.status}</span></td></tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="card">
        <h3>Approval queue</h3>
        <table>
          <thead><tr><th>Reference</th><th>Type</th><th>Amount</th><th>Status</th></tr></thead>
          <tbody>
            {approvals.data?.map((a) => (
              <tr key={a.id}><td>{a.reference}</td><td>{a.type}</td><td>{inr(a.amount)}</td><td><span className={`pill ${a.status}`}>{a.status}</span></td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
