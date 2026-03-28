<template>
  <div class="page">
    <div class="card" style="max-width: 640px">
      <div class="section-title">快递入库</div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="快递单号" path="trackingNumber">
          <n-input v-model:value="form.trackingNumber" />
        </n-form-item>
        <n-form-item label="物流公司">
          <n-input v-model:value="form.logisticsCompany" />
        </n-form-item>
        <n-form-item label="尺寸类型" path="sizeType">
          <n-select v-model:value="form.sizeType" :options="sizeOptions" />
        </n-form-item>
        <n-form-item label="收件人姓名">
          <n-input v-model:value="form.receiverName" />
        </n-form-item>
        <n-form-item label="收件人手机号" path="receiverPhone">
          <n-input v-model:value="form.receiverPhone" />
        </n-form-item>
        <n-form-item label="使用推荐货架">
          <n-switch v-model:value="form.useRecommendShelf" />
        </n-form-item>
        <n-form-item label="货架编号" path="shelfCode">
          <n-input v-model:value="form.shelfCode" :disabled="form.useRecommendShelf" />
        </n-form-item>
        <n-form-item label="货架层数" path="shelfLayer">
          <n-input v-model:value="form.shelfLayer" :disabled="form.useRecommendShelf" />
        </n-form-item>
        <n-button type="primary" @click="handleSubmit">提交入库</n-button>
      </n-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useMessage } from 'naive-ui';
import { api } from '../../api';
import { buildRequiredSelectRule, PHONE_RULE } from '../../constants/validation';

const message = useMessage();

const formRef = ref(null);
const form = reactive({
  trackingNumber: '',
  logisticsCompany: '',
  sizeType: 0,
  receiverName: '',
  receiverPhone: '',
  useRecommendShelf: true,
  shelfCode: '',
  shelfLayer: ''
});

const sizeOptions = [
  { label: '标准', value: 0 },
  { label: '大件', value: 1 },
  { label: '易碎', value: 2 },
  { label: '冷链', value: 3 }
];

const rules = {
  trackingNumber: { required: true, message: '快递单号不能为空', trigger: ['blur', 'input'] },
  sizeType: buildRequiredSelectRule('尺寸类型不能为空'),
  receiverPhone: { ...PHONE_RULE },
  shelfCode: {
    validator: () => (form.useRecommendShelf || form.shelfCode !== ''),
    message: '货架编号不能为空',
    trigger: ['blur', 'input']
  },
  shelfLayer: {
    validator: () => (form.useRecommendShelf || form.shelfLayer !== ''),
    message: '货架层数不能为空',
    trigger: ['blur', 'input']
  }
};

const handleSubmit = async () => {
  try {
    await formRef.value?.validate();
    const payload = {
      ...form,
      shelfCode: form.useRecommendShelf ? null : Number(form.shelfCode),
      shelfLayer: form.useRecommendShelf ? null : Number(form.shelfLayer)
    };
    await api.checkIn(payload);
    message.success('入库成功');
  } catch (err) {
    message.error(err?.message || '入库失败');
  }
};
</script>
