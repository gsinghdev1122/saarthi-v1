import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';
import { api } from '../api/client';

vi.mock('../api/client', async () => {
  const actual = await vi.importActual<typeof import('../api/client')>('../api/client');
  return {
    ...actual,
    api: {
      ...actual.api,
      getDashboardSummary: vi.fn().mockResolvedValue({
        canteen: 'Delhi Cantt', salesToday: 100000, inventoryValue: 500000,
        lowStockItems: 2, pendingApprovals: 1, attendanceRate: 95.5, lastImport: null,
      }),
      getRecentActivity: vi.fn().mockResolvedValue([]),
    },
  };
});

// Simulate an already-authenticated user so App renders the protected shell
// instead of redirecting to /login.
vi.mock('../context/AuthContext', async () => {
  const actual = await vi.importActual<typeof import('../context/AuthContext')>('../context/AuthContext');
  return {
    ...actual,
    useAuth: () => ({
      token: 'fake-token',
      user: { username: 'admin', displayName: 'Administrator', role: 'ADMIN' },
      login: vi.fn(),
      logout: vi.fn(),
      isAuthenticated: true,
    }),
  };
});

describe('App', () => {
  it('renders the dashboard with data from the API when authenticated', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/']}>
          <App />
        </MemoryRouter>
      </QueryClientProvider>
    );

    await waitFor(() => expect(screen.getByText(/Delhi Cantt/)).toBeInTheDocument());
    expect(api.getDashboardSummary).toHaveBeenCalled();
  });
});
