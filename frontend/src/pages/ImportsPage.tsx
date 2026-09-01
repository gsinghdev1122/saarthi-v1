import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

export default function ImportsPage() {
  const qc = useQueryClient();
  const imports = useQuery({ queryKey: ['imports'], queryFn: api.listImports });

  const [uploadType, setUploadType] = useState('inventory');
  const [uploadCanteen, setUploadCanteen] = useState('Delhi Cantt');
  const [file, setFile] = useState<File | null>(null);
  const uploadMutation = useMutation({
    mutationFn: () => {
      if (!file) throw new Error('No file selected');
      return api.uploadImportFile(file, uploadType, uploadCanteen);
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['imports'] });
      qc.invalidateQueries({ queryKey: ['dashboard-summary'] });
      qc.invalidateQueries({ queryKey: ['inventory'] });
      qc.invalidateQueries({ queryKey: ['employees'] });
      setFile(null);
    },
  });

  const [form, setForm] = useState({ filename: '', fileType: 'inventory', canteen: 'Delhi Cantt', rowCount: 0 });
  const mutation = useMutation({
    mutationFn: api.createImport,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['imports'] });
      qc.invalidateQueries({ queryKey: ['dashboard-summary'] });
      setForm({ filename: '', fileType: 'inventory', canteen: 'Delhi Cantt', rowCount: 0 });
    },
  });

  return (
    <div>
      <h1>Import centre</h1>

      <div className="card">
        <h3>Upload a CIMS export</h3>
        <p style={{ fontSize: 13, color: '#6b7566' }}>
          Upload a fixed-width <code>.prn</code> inventory/sales file or an <code>.xls</code>/<code>.xlsx</code> attendance
          export. The rows are parsed and applied to Inventory/Workforce directly — this is a real parse, not just metadata.
        </p>
        <form
          className="inline-form"
          onSubmit={(e) => {
            e.preventDefault();
            uploadMutation.mutate();
          }}
        >
          <div>
            <label>File</label>
            <input
              required
              type="file"
              accept=".prn,.xls,.xlsx,.txt"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            />
          </div>
          <div>
            <label>Type</label>
            <select value={uploadType} onChange={(e) => setUploadType(e.target.value)}>
              <option value="inventory">Inventory (.prn)</option>
              <option value="grocery_sales">Grocery sales (.prn)</option>
              <option value="liquor_sales">Liquor sales (.prn)</option>
              <option value="attendance">Attendance (.xls/.xlsx)</option>
            </select>
          </div>
          <div>
            <label>Canteen</label>
            <input value={uploadCanteen} onChange={(e) => setUploadCanteen(e.target.value)} />
          </div>
          <button className="primary" type="submit" disabled={uploadMutation.isPending || !file}>
            {uploadMutation.isPending ? 'Uploading…' : 'Upload & parse'}
          </button>
        </form>
        {uploadMutation.isError && (
          <p className="error-banner">
            {(uploadMutation.error as any)?.response?.data?.message || 'Could not parse the uploaded file.'}
          </p>
        )}
        {uploadMutation.isSuccess && <p style={{ color: '#2d6542', fontSize: 13 }}>{uploadMutation.data.message}</p>}
      </div>

      <div className="card">
        <h3>Register a batch manually (no file)</h3>
        <p style={{ fontSize: 13, color: '#6b7566' }}>
          For batches already applied outside this system — records the batch in the audit trail only.
        </p>
        <form className="inline-form" onSubmit={(e) => { e.preventDefault(); mutation.mutate(form); }}>
          <div>
            <label>Filename</label>
            <input required value={form.filename} onChange={(e) => setForm({ ...form, filename: e.target.value })} />
          </div>
          <div>
            <label>Type</label>
            <select value={form.fileType} onChange={(e) => setForm({ ...form, fileType: e.target.value })}>
              <option value="inventory">Inventory</option>
              <option value="grocery_sales">Grocery sales</option>
              <option value="liquor_sales">Liquor sales</option>
              <option value="attendance">Attendance</option>
              <option value="payroll">Payroll</option>
            </select>
          </div>
          <div>
            <label>Row count</label>
            <input type="number" min={0} value={form.rowCount} onChange={(e) => setForm({ ...form, rowCount: Number(e.target.value) })} />
          </div>
          <button className="primary" type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Registering…' : 'Register file'}</button>
        </form>
        {mutation.isError && <p className="error-banner">Could not register the file.</p>}
      </div>
      <div className="card">
        <h3>Recent source batches</h3>
        <table>
          <thead><tr><th>File</th><th>Type</th><th>Rows</th><th>Status</th><th>Uploaded</th></tr></thead>
          <tbody>
            {imports.data?.map((i) => (
              <tr key={i.id}>
                <td>{i.filename}</td>
                <td>{i.fileType}</td>
                <td>{i.rowCount.toLocaleString('en-IN')}</td>
                <td><span className={`pill ${i.status}`}>{i.status}</span></td>
                <td>{new Date(i.uploadedAt).toLocaleString('en-IN')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
