<template>
  <div class="page">
    <div class="checkin-layout">
      <div class="card">
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

      <div class="card">
        <div class="section-title">同类型货架负载</div>
        <div class="recommend-box">
          <div class="recommend-title">推荐货架</div>
          <div v-if="recommendLoading">加载中...</div>
          <div v-else-if="recommendedShelf">
            <div>货架：{{ recommendedShelf.shelfCode }}-{{ recommendedShelf.shelfLayer }}</div>
            <div>负载：{{ formatLoadUsage(recommendedShelf) }}</div>
            <div>负载率：{{ formatLoadRate(recommendedShelf) }}</div>
            <div v-if="form.useRecommendShelf" style="margin-top: 6px; color: #2a7a32">当前将使用该推荐货架</div>
          </div>
          <div v-else>{{ recommendError || '暂无可用推荐货架' }}</div>
        </div>
        <div v-if="loadError" style="margin: 8px 0; color: #c0392b">{{ loadError }}</div>
        <n-data-table
          :columns="loadColumns"
          :data="sortedLoadRows"
          :loading="loadLoading"
          :bordered="false"
          size="small"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, h, reactive, ref, watch } from 'vue';
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
  { label: '冷链', value: 2 },
  { label: '易碎', value: 3 }
];

const loadRows = ref([]);
const loadLoading = ref(false);
const loadError = ref('');
const recommendedShelf = ref(null);
const recommendLoading = ref(false);
const recommendError = ref('');

const getLoadRateNumber = (row) => {
  if (row?.loadRate !== null && row?.loadRate !== undefined && row?.loadRate !== '') {
    const parsed = Number(row.loadRate);
    if (!Number.isNaN(parsed)) {
      return parsed;
    }
  }
  const total = Number(row?.totalCapacity || 0);
  if (total <= 0) {
    return 0;
  }
  return Number(row?.currentUsage || 0) / total;
};

const formatLoadRate = (row) => `${Math.round(getLoadRateNumber(row) * 100)}%`;
const formatLoadUsage = (row) => `${row?.currentUsage ?? 0}/${row?.totalCapacity ?? 0}`;
const getLoadRateStyle = (row) => {
  const ratio = getLoadRateNumber(row);
  const baseStyle = {
    display: 'inline-block',
    minWidth: '52px',
    padding: '2px 8px',
    borderRadius: '999px',
    textAlign: 'center',
    fontWeight: 600,
    fontSize: '12px'
  };
  if (ratio >= 1) {
    return { ...baseStyle, background: '#fde8e8', color: '#b42318' };
  }
  if (ratio >= 0.8) {
    return { ...baseStyle, background: '#ffe8d9', color: '#b54708' };
  }
  if (ratio >= 0.5) {
    return { ...baseStyle, background: '#fff6dc', color: '#8a5b00' };
  }
  return { ...baseStyle, background: '#e8f7ef', color: '#1f7a45' };
};

const sortedLoadRows = computed(() =>
  [...loadRows.value].sort((a, b) => getLoadRateNumber(a) - getLoadRateNumber(b))
);

const loadColumns = [
  { title: '货架', key: 'shelfCode', render: (row) => `${row.shelfCode}-${row.shelfLayer}` },
  { title: '当前负载', key: 'currentUsage', render: (row) => formatLoadUsage(row) },
  {
    title: '负载率',
    key: 'loadRate',
    render: (row) => h('span', { style: getLoadRateStyle(row) }, formatLoadRate(row))
  }
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
    await refreshShelfLoadPanel();
  } catch (err) {
    message.error(err?.message || '入库失败');
  }
};

const refreshShelfLoadPanel = async () => {
  loadLoading.value = true;
  recommendLoading.value = true;
  loadError.value = '';
  recommendError.value = '';

  const [loadRes, recommendRes] = await Promise.allSettled([
    api.listShelfLoads({ shelfType: form.sizeType, status: 1 }),
    api.recommendShelf({ sizeType: form.sizeType })
  ]);

  if (loadRes.status === 'fulfilled') {
    loadRows.value = loadRes.value?.data || [];
  } else {
    loadRows.value = [];
    loadError.value = loadRes.reason?.message || '负载数据获取失败';
  }

  if (recommendRes.status === 'fulfilled') {
    recommendedShelf.value = recommendRes.value?.data || null;
  } else {
    recommendedShelf.value = null;
    recommendError.value = recommendRes.reason?.message || '推荐货架获取失败';
  }

  loadLoading.value = false;
  recommendLoading.value = false;
};

watch(
  () => form.sizeType,
  () => {
    refreshShelfLoadPanel();
  },
  { immediate: true }
);
</script>

<style scoped>
.checkin-layout {
  display: grid;
  grid-template-columns: minmax(420px, 640px) minmax(320px, 1fr);
  gap: 16px;
  align-items: start;
}

.recommend-box {
  margin-bottom: 12px;
  padding: 12px;
  border: 1px solid #e7e9ee;
  border-radius: 8px;
  background: #fafbff;
}

.recommend-title {
  font-weight: 600;
  margin-bottom: 6px;
}

@media (max-width: 1100px) {
  .checkin-layout {
    grid-template-columns: 1fr;
  }
}
</style>
