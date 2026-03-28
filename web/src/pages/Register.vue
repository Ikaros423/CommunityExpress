<template>
  <div class="page">
    <div class="card" style="max-width: 480px; margin: 60px auto">
      <div class="section-title">用户注册</div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="手机号" path="username">
          <n-input v-model:value="form.username" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="昵称">
          <n-input v-model:value="form.nickname" placeholder="可选" />
        </n-form-item>
        <n-button type="primary" block @click="handleRegister">注册</n-button>
      </n-form>
      <div style="margin-top: 16px; text-align: right">
        <n-button text @click="goLogin">返回登录</n-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { useAuthStore } from '../stores/auth';
import { PASSWORD_RULE, PHONE_RULE } from '../constants/validation';

const router = useRouter();
const message = useMessage();
const auth = useAuthStore();

const formRef = ref(null);
const form = reactive({
  username: '',
  password: '',
  nickname: ''
});

const rules = {
  username: { ...PHONE_RULE },
  password: { ...PASSWORD_RULE }
};

const handleRegister = async () => {
  try {
    await formRef.value?.validate();
    await auth.register(form);
    message.success('注册成功');
    router.push('/login');
  } catch (err) {
    if (err?.errors) {
      return;
    }
  }
};

const goLogin = () => router.push('/login');
</script>
