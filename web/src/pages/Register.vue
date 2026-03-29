<template>
  <div class="page">
    <div class="card" style="max-width: 480px; margin: 60px auto">
      <div class="section-title">用户注册</div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="手机号" path="username">
          <n-input v-model:value="form.username" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="验证码" path="code">
          <div class="flex" style="width: 100%">
            <n-input v-model:value="form.code" placeholder="请输入短信验证码" />
            <n-button :disabled="sendingCode || cooldown > 0" :loading="sendingCode" @click="handleSendCode">
              {{ cooldown > 0 ? `${cooldown}s后重试` : '获取验证码' }}
            </n-button>
          </div>
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
import { onUnmounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { api } from '../api';
import { useAuthStore } from '../stores/auth';
import { PASSWORD_RULE, PHONE_REGEX, PHONE_RULE } from '../constants/validation';

const router = useRouter();
const message = useMessage();
const auth = useAuthStore();

const formRef = ref(null);
const sendingCode = ref(false);
const cooldown = ref(0);
let cooldownTimer = null;

const form = reactive({
  username: '',
  code: '',
  password: '',
  nickname: ''
});

const rules = {
  username: { ...PHONE_RULE },
  code: { required: true, message: '验证码不能为空', trigger: ['blur', 'input'] },
  password: { ...PASSWORD_RULE }
};

const startCooldown = () => {
  cooldown.value = 60;
  if (cooldownTimer) {
    clearInterval(cooldownTimer);
  }
  cooldownTimer = setInterval(() => {
    if (cooldown.value <= 1) {
      clearInterval(cooldownTimer);
      cooldownTimer = null;
      cooldown.value = 0;
      return;
    }
    cooldown.value -= 1;
  }, 1000);
};

const handleSendCode = async () => {
  if (sendingCode.value || cooldown.value > 0) {
    return;
  }
  try {
    const phone = form.username.trim();
    if (!PHONE_REGEX.test(phone)) {
      message.warning('请先输入正确的手机号');
      return;
    }
    sendingCode.value = true;
    await api.requestSmsCode({
      phone,
      bizType: 'REGISTER'
    });
    message.success('验证码已发送（控制台日志查看）');
    startCooldown();
  } catch (err) {
    if (err?.errors) {
      return;
    }
    message.error(err?.message || '验证码发送失败');
  } finally {
    sendingCode.value = false;
  }
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

onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer);
    cooldownTimer = null;
  }
});
</script>
