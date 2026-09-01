import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

const inr = (v: number | undefined) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v ?? 0);

export default function DashboardPage() {
  const summary = useQuery({ queryKey: ['dashboard-summary'], queryFn: api.getDashboardSummary });
  const activity = useQuery({ queryKey: ['activity'], queryFn: api.getRecentActivity });

  if (summary.isLoading) return <p>Loading dashboard…</p>;
  if (summary.isError) return <p className="error-banner">Could not load the dashboard. Is the backend running?</p>;

  const s = summary.data!;
  return (
    <div>
      <h1>Command overview</h1>
      <p>Readiness picture for {s.canteen}.</p>
      <div className="grid-4">
        <div className="card"><div className="stat-label">Sales today</div><div className="stat-value">{inr(s.salesToday)}</div></div>
        <div className="card"><div className="stat-label">Inventory value</div><div className="stat-value">{inr(s.inventoryValue)}</div></div>
        <div className="card"><div className="stat-label">Pending approvals</div><div className="stat-value">{s.pendingApprovals}</div></div>
        <div className="card"><div className="stat-label">Attendance rate</div><div className="stat-value">{s.attendanceRate}%</div></div>
      </div>
      <div className="card">
        <h3>Recent activity</h3>
        {activity.isLoading && <p>Loading…</p>}
        {activity.data?.length === 0 && <p>No activity yet.</p>}
        <table>
          <tbody>
            {activity.data?.map((a) => (
              <tr key={a.id}>
                <td>{a.title}</td>
                <td>{a.detail}</td>
                <td>{new Date(a.timestamp).toLocaleString('en-IN')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
