import http from './http';
import { normalizePageData } from '../utils/page';

const buildPagedResponse = (res, fallback) => ({
  ...res,
  data: normalizePageData(res?.data, fallback)
});

const getPagingFallback = (params = {}) => ({
  page: params?.page,
  pageSize: params?.pageSize
});

export const api = {
  login: (payload) => http.post('/system/users/login', payload),
  register: (payload) => http.post('/system/users/register', payload),
  refresh: () => http.post('/system/users/refresh'),
  requestSmsCode: (payload) => http.post('/system/users/sms-code/request', payload),
  requestResetCode: (payload) => http.post('/system/users/password-reset/request', payload),
  confirmReset: (payload) => http.post('/system/users/password-reset/confirm', payload),
  getDashboardSummary: () => http.get('/system/dashboard/summary'),
  getDashboardTrend: (params) => http.get('/system/dashboard/trend', { params }),
  getDashboardRanks: () => http.get('/system/dashboard/ranks'),

  listExpresses: async (params = {}) => {
    const res = await http.get('/system/expresses', { params });
    return buildPagedResponse(res, getPagingFallback(params));
  },
  getExpress: (id) => http.get(`/system/expresses/${id}`),
  checkIn: (payload) => http.post('/system/expresses', payload),
  checkOut: (trackingNumber) => http.post(`/system/expresses/${trackingNumber}/checkout`),
  claimExpress: (payload) => http.post('/system/expresses/claim', payload),
  updateExpress: (id, payload) => http.put(`/system/expresses/${id}`, payload),
  relocateExpress: (id, payload) => http.post(`/system/expresses/${id}/relocate`, payload),
  deleteExpress: (id) => http.delete(`/system/expresses/${id}`),

  listShelves: async (params = {}) => {
    const res = await http.get('/system/shelves', { params });
    return buildPagedResponse(res, getPagingFallback(params));
  },
  listShelfLoads: (params) => http.get('/system/shelves/load', { params }),
  getShelf: (id) => http.get(`/system/shelves/${id}`),
  recommendShelf: (params) => http.get('/system/shelves/recommend', { params }),
  lookupShelf: (params) => http.get('/system/shelves/lookup', { params }),
  createShelf: (payload) => http.post('/system/shelves', payload),
  updateShelf: (id, payload) => http.put(`/system/shelves/${id}`, payload),
  deleteShelf: (id) => http.delete(`/system/shelves/${id}`),

  listUsers: async (params = {}) => {
    const res = await http.get('/system/users', { params });
    return buildPagedResponse(res, getPagingFallback(params));
  },
  getUser: (id) => http.get(`/system/users/${id}`),
  createUser: (payload) => http.post('/system/users', payload),
  updateUser: (id, payload) => http.put(`/system/users/${id}`, payload),
  deleteUser: (id) => http.delete(`/system/users/${id}`),

  createSendOrder: (payload) => http.post('/system/send-orders', payload),
  listSendOrders: async (params = {}) => {
    const res = await http.get('/system/send-orders', { params });
    return buildPagedResponse(res, getPagingFallback(params));
  },
  updateSendOrderStatus: (id, payload) => http.put(`/system/send-orders/${id}/status`, payload)
};
