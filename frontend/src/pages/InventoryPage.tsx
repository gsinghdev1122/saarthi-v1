import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

const inr = (v: number) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);

export default function InventoryPage() {
  const [search, setSearch] = useState('');
  const [division, setDivision] = useState('');
  const query = useQuery({
    queryKey: ['inventory', search, division],
    queryFn: () => api.listInventory({ search: search || undefined, division: division || undefined }),
  });

  return (
    <div>
      <h1>Inventory</h1>
      <div className="card">
        <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
          <input placeholder="Search item or index number" value={search} onChange={(e) => setSearch(e.target.value)} />
          <select value={division} onChange={(e) => setDivision(e.target.value)}>
            <option value="">All divisions</option>
            <option value="Grocery">Grocery</option>
            <option value="Liquor">Liquor</option>
          </select>
        </div>
        <table>
          <thead><tr><th>Index / item</th><th>Division</th><th>Closing stock</th><th>Reorder at</th><th>Value</th><th>Status</th></tr></thead>
          <tbody>
            {query.data?.map((item) => (
              <tr key={item.id}>
                <td>{item.indexNo} — {item.name}</td>
                <td>{item.division}</td>
                <td>{item.closingStock}</td>
                <td>{item.reorderLevel}</td>
                <td>{inr(item.value)}</td>
                <td><span className={`pill ${item.status}`}>{item.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
