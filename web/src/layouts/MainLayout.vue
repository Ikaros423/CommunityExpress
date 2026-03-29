<template>
  <n-layout has-sider style="min-height: 100vh">
    <n-layout-sider width="220" content-style="padding: 16px" bordered>
      <div class="section-title">CommunityExpress</div>
      <n-menu :options="menuOptions" @update:value="handleSelect" />
      <div style="margin-top: 20px">
        <n-button block type="default" @click="logout">退出登录</n-button>
      </div>
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered style="padding: 12px 20px">
        <div>当前角色：{{ auth.role || 'UNKNOWN' }}</div>
      </n-layout-header>
      <n-layout-content>
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();

const baseMenus = [
  { label: '概览', key: '/' },
  { label: '快递管理', key: '/expresses', roles: ['USER', 'STAFF', 'ADMIN'] },
  { label: '寄件管理', key: '/send-orders', roles: ['USER', 'STAFF', 'ADMIN'] }
];

const staffMenus = [
  { label: '快递入库', key: '/express/checkin', roles: ['STAFF', 'ADMIN'] },
  { label: '货架管理', key: '/shelves', roles: ['STAFF', 'ADMIN'] }
];

const adminMenus = [
  { label: '用户管理', key: '/users', roles: ['ADMIN'] }
];

const menuOptions = computed(() => {
  const role = auth.role;
  const all = [...baseMenus, ...staffMenus, ...adminMenus];
  return all.filter((item) => !item.roles || item.roles.includes(role));
});

const handleSelect = (key) => {
  router.push(key);
};

const logout = () => {
  auth.logout();
  router.push('/login');
};
</script>
