const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('deskflow_token');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_URL}${path}`, { ...options, headers });

  if (res.status === 401) {
    localStorage.removeItem('deskflow_token');
    localStorage.removeItem('deskflow_user');
    window.location.href = '/login';
    throw new Error('Session expired');
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(err.error || 'Server error');
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get: <T>(path: string) => request<T>(path, { method: 'GET' }),
  post: <T>(path: string, body: unknown) => request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
};

// Auth
export const authApi = {
  login: (email: string, password: string) =>
    api.post<{ token: string; email: string; role: string; firstName: string; lastName: string; employeeId: string }>(
      '/api/auth/login', { email, password }
    ),
  me: () => api.get<{ email: string; role: string; firstName: string; lastName: string; employeeId: string }>('/api/auth/me'),
};

// Resources (UC-01)
export const resourcesApi = {
  list: () => api.get<Resource[]>('/api/resources'),
  add: (data: ResourceCreate) => api.post<{ resourceId: string }>('/api/resources', data),
  update: (id: string, data: ResourceCreate) => api.put(`/api/resources/${id}`, data),
  markUnavailable: (id: string) => api.put(`/api/resources/${id}/unavailable`),
  markAvailable: (id: string) => api.put(`/api/resources/${id}/available`),
  deactivate: (id: string) => api.delete(`/api/resources/${id}`),
};

// Allocation requests (UC-02, 03, 14)
export const requestsApi = {
  submit: (data: SubmitRequest) => api.post<{ requestId: string }>('/api/requests', data),
  cancel: (id: string) => api.put(`/api/requests/${id}/cancel`),
  my: () => api.get<AllocationRequest[]>('/api/requests/my'),
};

// Administration (UC-04-09, 15)
export const administrationApi = {
  requests: (status?: string) => api.get<AllocationRequest[]>(`/api/administration/requests${status ? `?status=${status}` : ''}`),
  approve: (id: string) => api.put(`/api/administration/requests/${id}/approve`),
  reject: (id: string, reason: string) => api.put(`/api/administration/requests/${id}/reject`, { reason }),
  issue: (id: string) => api.put(`/api/administration/requests/${id}/issue`),
  return: (id: string) => api.put(`/api/administration/requests/${id}/return`),
  allocations: () => api.get<AllocationRequest[]>('/api/administration/allocations'),
};

// Employee admin (IT_ADMIN — UC-10, UC-11, limits)
export const employeeAdminApi = {
  list: () => api.get<EmployeeWithLimits[]>('/api/administration/employees'),
  upsertLimit: (employeeId: string, resourceType: string, amount: number) =>
    api.put(`/api/administration/employees/${employeeId}/limits`, { resourceType, amount }),
};

// Types
export interface Resource {
  id: string;
  type: string;
  name: string;
  building: string;
  floor: number;
  number: string;
  available: boolean;
}

export interface ResourceCreate {
  type: string;
  name: string;
  building: string;
  floor: number;
  number: string;
}

export interface AllocationRequest {
  id: string;
  employeeId: string;
  employeeFirstName: string | null;
  employeeLastName: string | null;
  resourceId: string;
  status: string;
  dateFrom: string;
  dateTo: string;
  justification: string;
  submittedAt: string;
  issuedAt: string | null;
  returnedAt: string | null;
  rejectionReason: string | null;
}

export interface SubmitRequest {
  resourceId: string;
  dateFrom: string;
  dateTo: string;
  justification: string;
}

export interface EmployeeWithLimits {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  limits: Record<string, number>;
}

export interface CurrentUser {
  email: string;
  role: string;
  firstName: string;
  lastName: string;
  employeeId: string;
}
