import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

const inr = (v: number) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);

export default function ReportsPage() {
  const query = useQuery({ queryKey: ['reports-overview'], queryFn: api.getReportsOverview });
  const r = query.data;
  return (
    <div>
      <h1>Reports</h1>
      {query.isLoading && <p>Loading…</p>}
      {r && (
        <>
          <div className="grid-4">
            <div className="card"><div className="stat-label">Gross sales</div><div className="stat-value">{inr(r.sales)}</div></div>
            <div className="card"><div className="stat-label">Expenses</div><div className="stat-value">{inr(r.expenses)}</div></div>
            <div className="card"><div className="stat-label">Operating surplus</div><div className="stat-value">{inr(r.profit)}</div></div>
          </div>
          <div className="card">
            <h3>Monthly movement</h3>
            <table>
              <thead><tr><th>Month</th><th>Sales</th><th>Expenses</th></tr></thead>
              <tbody>
                {r.salesByMonth.map((m) => (
                  <tr key={m.month}><td>{m.month}</td><td>{inr(m.sales)}</td><td>{inr(m.expenses)}</td></tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
