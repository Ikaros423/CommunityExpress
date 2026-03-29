<template>
  <div class="page">
    <div class="card" style="max-width: 520px; margin: 60px auto">
      <div class="section-title">短信重置密码</div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="form.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="验证码" path="code">
          <n-input v-model:value="form.code" placeholder="请输入验证码" />
        </n-form-item>
        <n-form-item label="新密码" path="newPassword">
          <n-input v-model:value="form.newPassword" type="password" placeholder="请输入新密码" />
        </n-form-item>
        <div class="flex">
          <n-button @click="handleRequest">获取验证码</n-button>
          <n-button type="primary" @click="handleConfirm">确认重置</n-button>
        </div>
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
import { api } from '../api';
import { PASSWORD_RULE, PHONE_REGEX, PHONE_RULE } from '../constants/validation';

const router = useRouter();
const message = useMessage();

const formRef = ref(null);
const form = reactive({
  phone: '',
  code: '',
  newPassword: ''
});

const rules = {
  phone: { ...PHONE_RULE },
  code: { required: true, message: '验证码不能为空', trigger: ['blur', 'input'] },
  newPassword: { ...PASSWORD_RULE }
};

const handleRequest = async () => {
  try {
    const phone = form.phone.trim();
    if (!PHONE_REGEX.test(phone)) {
      message.warning('请先输入正确的手机号');
      return;
    }
    await api.requestSmsCode({ phone, bizType: 'PASSWORD_RESET' });
    message.success('验证码已发送（控制台日志查看）');
  } catch (err) {
    if (err?.errors) {
      return;
    }
    message.error(err?.message || '验证码发送失败');
  }
};

const handleConfirm = async () => {
  try {
    await formRef.value?.validate();
    await api.confirmReset({
      phone: form.phone,
      code: form.code,
      newPassword: form.newPassword
    });
    message.success('密码重置成功');
    router.push('/login');
  } catch (err) {
    if (err?.errors) {
      return;
    }
  }
};

const goLogin = () => router.push('/login');
</script>
