import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

// A 401 means the token is missing/expired/invalid — broadcast an event so
// AuthContext can clear local state and redirect to /login, without this
// module needing to depend on React/router directly.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }
    return Promise.reject(error);
  }
);

export interface DashboardSummary {
  canteen: string;
  salesToday: number;
  inventoryValue: number;
  lowStockItems: number;
  pendingApprovals: number;
  attendanceRate: number;
  lastImport: string | null;
}

export interface ActivityItem {
  id: number;
  title: string;
  detail: string;
  kind: string;
  timestamp: string;
}

export interface ImportBatch {
  id: number;
  filename: string;
  fileType: string;
  canteen: string;
  status: string;
  rowCount: number;
  message: string | null;
  uploadedAt: string;
}

export interface InventoryItem {
  id: number;
  indexNo: string;
  name: string;
  division: string;
  closingStock: number;
  reorderLevel: number;
  value: number;
  trend: string;
  status: string;
  canteen: string;
}

export interface Employee {
  id: number;
  name: string;
  employeeCode: string;
  category: string;
  designation: string;
  attendance: number;
  contractEnd: string | null;
  status: string;
  canteen: string;
}

export interface Expense {
  id: number;
  category: string;
  vendor: string;
  amount: number;
  date: string;
  status: string;
  submittedBy: string;
  canteen: string;
}

export interface Approval {
  id: number;
  type: string;
  reference: string;
  amount: number;
  submittedBy: string;
  submittedAt: string;
  status: string;
}

export interface ReportsOverview {
  sales: number;
  expenses: number;
  profit: number;
  salesByMonth: { month: string; sales: number; expenses: number }[];
}

export interface AppUser {
  id: number;
  username: string;
  displayName: string;
  role: string;
  enabled: boolean;
}

export const api = {
  getDashboardSummary: () => apiClient.get<DashboardSummary>('/dashboard/summary').then((r) => r.data),
  getRecentActivity: () => apiClient.get<ActivityItem[]>('/activity').then((r) => r.data),
  listImports: () => apiClient.get<ImportBatch[]>('/imports').then((r) => r.data),
  createImport: (body: { filename: string; fileType: string; canteen: string; rowCount: number }) =>
    apiClient.post<ImportBatch>('/imports', body).then((r) => r.data),
  uploadImportFile: (file: File, fileType: string, canteen: string) => {
    const form = new FormData();
    form.append('file', file);
    form.append('fileType', fileType);
    form.append('canteen', canteen);
    return apiClient
      .post<ImportBatch>('/imports/upload', form, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then((r) => r.data);
  },
  listInventory: (params: { division?: string; search?: string }) =>
    apiClient.get<InventoryItem[]>('/inventory', { params }).then((r) => r.data),
  listEmployees: () => apiClient.get<Employee[]>('/employees').then((r) => r.data),
  createEmployee: (body: { name: string; employeeCode: string; category: string; designation: string; contractEnd: string | null }) =>
    apiClient.post<Employee>('/employees', body).then((r) => r.data),
  listExpenses: () => apiClient.get<Expense[]>('/expenses').then((r) => r.data),
  createExpense: (body: { category: string; vendor: string; amount: number; date: string }) =>
    apiClient.post<Expense>('/expenses', body).then((r) => r.data),
  listApprovals: () => apiClient.get<Approval[]>('/approvals').then((r) => r.data),
  getReportsOverview: () => apiClient.get<ReportsOverview>('/reports/overview').then((r) => r.data),
  healthz: () => apiClient.get('/healthz').then((r) => r.data),
};

