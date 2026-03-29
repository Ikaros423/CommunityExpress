<template>
  <div class="page">
    <div class="card flex-column">
      <div class="section-title">寄件管理</div>

      <n-form
        v-if="isUser"
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-placement="top"
        style="margin-bottom: 16px"
      >
        <div class="section-subtitle">我要寄件</div>
        <div class="grid-form">
          <n-form-item label="寄件人手机号（当前账号）">
            <n-input v-model:value="createForm.senderPhone" readonly />
          </n-form-item>
          <n-form-item label="寄件地址" path="senderAddress">
            <n-input v-model:value="createForm.senderAddress" placeholder="请输入寄件地址" />
          </n-form-item>
          <n-form-item label="收件人姓名" path="receiverName">
            <n-input v-model:value="createForm.receiverName" placeholder="请输入收件人姓名" />
          </n-form-item>
          <n-form-item label="收件人手机号" path="receiverPhone">
            <n-input v-model:value="createForm.receiverPhone" placeholder="请输入收件人手机号" />
          </n-form-item>
          <n-form-item label="收件地址" path="receiverAddress">
            <n-input v-model:value="createForm.receiverAddress" placeholder="请输入收件地址" />
          </n-form-item>
          <n-form-item label="包裹类型" path="packageType">
            <n-select v-model:value="createForm.packageType" :options="packageTypeOptions" />
          </n-form-item>
        </div>
        <n-form-item label="备注">
          <n-input v-model:value="createForm.remark" placeholder="可选" />
        </n-form-item>
        <div class="flex">
          <n-button type="primary" :loading="creating" @click="handleCreate">提交寄件申请</n-button>
        </div>
      </n-form>

      <div class="flex">
        <n-select v-model:value="filters.status" :options="statusOptionsWithAll" placeholder="状态筛选" />
        <n-input
          v-if="isStaffOrAdmin"
          v-model:value="filters.senderPhone"
          placeholder="寄件人手机号(员工/管理员)"
        />
        <n-button type="primary" :loading="loading" @click="fetchList">查询</n-button>
      </div>

      <n-data-table :columns="columns" :data="rows" :loading="loading" :bordered="false" :pagination="pagination" />
    </div>
  </div>
</template>

<script setup>
import { computed, h, onMounted, reactive, ref } from 'vue';
import { NButton, useMessage } from 'naive-ui';
import { api } from '../../api';
import { buildRequiredSelectRule, PHONE_RULE } from '../../constants/validation';
import { useAuthStore } from '../../stores/auth';

const message = useMessage();
const auth = useAuthStore();

const rows = ref([]);
const loading = ref(false);
const creating = ref(false);
const updatingId = ref(null);
const createFormRef = ref(null);

const isUser = computed(() => auth.role === 'USER');
const isStaffOrAdmin = computed(() => ['STAFF', 'ADMIN'].includes(auth.role));

const pagination = reactive({
  page: 1,
  pageSize: 15,
  showSizePicker: false,
  onChange: (page) => {
    pagination.page = page;
  }
});

const filters = reactive({
  status: null,
  senderPhone: ''
});

const createForm = reactive({
  senderPhone: '',
  senderAddress: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  packageType: 0,
  remark: ''
});

const packageTypeOptions = [
  { label: '标准', value: 0 },
  { label: '大件', value: 1 },
  { label: '冷链', value: 2 },
  { label: '易碎', value: 3 }
];

const packageTypeLabelMap = {
  0: '标准',
  1: '大件',
  2: '冷链',
  3: '易碎'
};

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '已受理', value: 1 },
  { label: '已寄出', value: 2 },
  { label: '已取消', value: 3 }
];

const statusOptionsWithAll = [{ label: '全部', value: null }, ...statusOptions];

const statusLabelMap = {
  0: '待处理',
  1: '已受理',
  2: '已寄出',
  3: '已取消'
};

const createRules = {
  senderAddress: { required: true, message: '寄件地址不能为空', trigger: ['blur', 'input'] },
  receiverName: { required: true, message: '收件人姓名不能为空', trigger: ['blur', 'input'] },
  receiverPhone: { ...PHONE_RULE },
  receiverAddress: { required: true, message: '收件地址不能为空', trigger: ['blur', 'input'] },
  packageType: buildRequiredSelectRule('包裹类型不能为空')
};

const getStatusLabel = (value) => statusLabelMap[value] ?? String(value ?? '-');
const getPackageTypeLabel = (value) => packageTypeLabelMap[value] ?? String(value ?? '-');
const formatDateTime = (value) => (value ? String(value).replace('T', ' ') : '-');

const resetCreateForm = () => {
  createForm.senderPhone = auth.user?.username || '';
  createForm.senderAddress = '';
  createForm.receiverName = '';
  createForm.receiverPhone = '';
  createForm.receiverAddress = '';
  createForm.packageType = 0;
  createForm.remark = '';
};

const fetchList = async () => {
  try {
    loading.value = true;
    const res = await api.listSendOrders({
      status: filters.status ?? undefined,
      senderPhone: isStaffOrAdmin.value ? (filters.senderPhone.trim() || undefined) : undefined
    });
    rows.value = res.data || [];
    pagination.page = 1;
  } catch (err) {
    return;
  } finally {
    loading.value = false;
  }
};

const handleCreate = async () => {
  try {
    creating.value = true;
    await createFormRef.value?.validate();
    await api.createSendOrder({
      senderAddress: createForm.senderAddress.trim(),
      receiverName: createForm.receiverName.trim(),
      receiverPhone: createForm.receiverPhone.trim(),
      receiverAddress: createForm.receiverAddress.trim(),
      packageType: createForm.packageType,
      remark: createForm.remark?.trim() || ''
    });
    message.success('提交成功');
    resetCreateForm();
    await fetchList();
  } catch (err) {
    if (err?.errors) {
      return;
    }
    return;
  } finally {
    creating.value = false;
  }
};

const handleUpdateStatus = async (row, status) => {
  try {
    updatingId.value = row.id;
    await api.updateSendOrderStatus(row.id, { status });
    message.success('状态更新成功');
    await fetchList();
  } catch (err) {
    return;
  } finally {
    updatingId.value = null;
  }
};

const columns = computed(() => {
  const base = [
    { title: 'ID', key: 'id' },
    { title: '寄件人手机号', key: 'senderPhone' },
    { title: '寄件地址', key: 'senderAddress' },
    { title: '收件人', key: 'receiverName' },
    { title: '收件人手机号', key: 'receiverPhone' },
    { title: '收件地址', key: 'receiverAddress' },
    {
      title: '包裹类型',
      key: 'packageType',
      render(row) {
        return getPackageTypeLabel(row.packageType);
      }
    },
    {
      title: '状态',
      key: 'status',
      render(row) {
        return getStatusLabel(row.status);
      }
    },
    {
      title: '创建时间',
      key: 'createTime',
      render(row) {
        return formatDateTime(row.createTime);
      }
    },
    {
      title: '更新时间',
      key: 'updateTime',
      render(row) {
        return formatDateTime(row.updateTime);
      }
    }
  ];

  if (!isStaffOrAdmin.value) {
    return base;
  }

  return [
    ...base,
    {
      title: '操作',
      key: 'actions',
      render(row) {
        if (row.status === 2 || row.status === 3) {
          return '-';
        }
        if (row.status === 0) {
          return [
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                loading: updatingId.value === row.id,
                style: 'margin-right: 8px',
                onClick: () => handleUpdateStatus(row, 1)
              },
              { default: () => '受理' }
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'error',
                loading: updatingId.value === row.id,
                onClick: () => handleUpdateStatus(row, 3)
              },
              { default: () => '取消' }
            )
          ];
        }
        if (row.status === 1) {
          return [
            h(
              NButton,
              {
                size: 'small',
                type: 'success',
                loading: updatingId.value === row.id,
                style: 'margin-right: 8px',
                onClick: () => handleUpdateStatus(row, 2)
              },
              { default: () => '寄出' }
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'error',
                loading: updatingId.value === row.id,
                onClick: () => handleUpdateStatus(row, 3)
              },
              { default: () => '取消' }
            )
          ];
        }
        return '-';
      }
    }
  ];
});

onMounted(() => {
  resetCreateForm();
  fetchList();
});
</script>

<style scoped>
.grid-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(220px, 1fr));
  gap: 12px;
}

.section-subtitle {
  font-weight: 600;
  margin-bottom: 8px;
}

@media (max-width: 960px) {
  .grid-form {
    grid-template-columns: 1fr;
  }
}
</style>
