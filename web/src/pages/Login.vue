<template>
  <div class="page">
    <div class="card" style="max-width: 420px; margin: 60px auto">
      <div class="section-title">登录</div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="手机号" path="account">
          <n-input v-model:value="form.account" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-button type="primary" block @click="handleLogin">登录</n-button>
      </n-form>
      <div style="margin-top: 16px; display: flex; justify-content: space-between">
        <n-button text @click="goRegister">去注册</n-button>
        <n-button text @click="goForgot">忘记密码</n-button>
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
  account: '',
  password: ''
});

const rules = {
  account: { ...PHONE_RULE },
  password: { ...PASSWORD_RULE }
};

const handleLogin = async () => {
  try {
    await formRef.value?.validate();
    await auth.login(form.account, form.password);
    message.success('登录成功');
    router.push('/');
  } catch (err) {
    if (err?.errors) {
      return;
    }
  }
};

const goRegister = () => router.push('/register');
const goForgot = () => router.push('/forgot');
</script>
