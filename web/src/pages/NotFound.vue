<template>
  <div class="page">
    <div class="card">
      <div class="section-title">页面不存在</div>
      <n-button type="primary" @click="goHome">返回首页</n-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();
const goHome = () => router.push('/');
</script>
