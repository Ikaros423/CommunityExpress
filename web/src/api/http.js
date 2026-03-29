import axios from 'axios';
import { useAuthStore } from '../stores/auth';
import { createDiscreteApi } from 'naive-ui';

const { message } = createDiscreteApi(['message']);

const http = axios.create({
  baseURL: '',
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && typeof payload.code !== 'undefined' && payload.code !== 200) {
      const error = { message: payload.message || '请求失败', payload };
      message.error(error.message);
      return Promise.reject(error);
    }
    return payload;
  },
  (error) => {
    const status = error?.response?.status;
    const backendMessage = error?.response?.data?.message;
    if (backendMessage) {
      message.error(backendMessage);
      return Promise.reject({ message: backendMessage, error });
    }
    if (status === 401) {
      message.error('未登录或登录已过期');
    } else if (status === 403) {
      message.error('无权限访问');
    } else {
      message.error('网络或服务器错误');
    }
    return Promise.reject(error);
  }
);

export default http;
