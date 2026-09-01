import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

export default function WorkforcePage() {
  const qc = useQueryClient();
  const employees = useQuery({ queryKey: ['employees'], queryFn: api.listEmployees });
  const [form, setForm] = useState({ name: '', employeeCode: '', category: 'Permanent', designation: '', contractEnd: '' });
  const mutation = useMutation({
    mutationFn: () => api.createEmployee({ ...form, contractEnd: form.contractEnd || null }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['employees'] });
      setForm({ name: '', employeeCode: '', category: 'Permanent', designation: '', contractEnd: '' });
    },
  });

  return (
    <div>
      <h1>Workforce</h1>
      <div className="card">
        <h3>Add workforce member</h3>
        <form className="inline-form" onSubmit={(e) => { e.preventDefault(); mutation.mutate(); }}>
          <div><label>Name</label><input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div>
          <div><label>Employee code</label><input required value={form.employeeCode} onChange={(e) => setForm({ ...form, employeeCode: e.target.value })} /></div>
          <div><label>Designation</label><input required value={form.designation} onChange={(e) => setForm({ ...form, designation: e.target.value })} /></div>
          <div>
            <label>Category</label>
            <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
              <option>Permanent</option><option>Contractual</option><option>Daily Wage</option>
            </select>
          </div>
          <button className="primary" type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Saving…' : 'Save member'}</button>
        </form>
        {mutation.isError && <p className="error-banner">Could not save (duplicate employee code?).</p>}
      </div>
      <div className="card">
        <table>
          <thead><tr><th>Name</th><th>Code</th><th>Category</th><th>Designation</th><th>Attendance</th><th>Status</th></tr></thead>
          <tbody>
            {employees.data?.map((e) => (
              <tr key={e.id}>
                <td>{e.name}</td><td>{e.employeeCode}</td><td>{e.category}</td><td>{e.designation}</td>
                <td>{e.attendance}%</td><td><span className={`pill ${e.status}`}>{e.status}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
