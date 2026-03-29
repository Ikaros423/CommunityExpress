<template>
  <div class="page">
    <div class="card flex-column">
      <div class="section-title">快递查询与管理</div>
      <div class="flex">
        <n-input v-model:value="filters.trackingNumber" placeholder="快递单号" />
        <n-input
          v-if="isStaffOrAdmin"
          v-model:value="filters.receiverPhone"
          placeholder="收件人手机号(员工/管理员)"
        />
        <n-select v-model:value="filters.status" :options="statusOptions" placeholder="状态" style="min-width: 140px" />
        <n-select
          v-if="isStaffOrAdmin"
          v-model:value="filters.overdueOnly"
          :options="overdueOptions"
          placeholder="滞留筛选"
          style="min-width: 140px"
        />
        <n-button type="primary" :loading="loading" @click="fetchList">查询</n-button>
      </div>
      <n-data-table :columns="columns" :data="rows" :loading="loading" :bordered="false" :pagination="pagination" />
    </div>

    <n-modal v-model:show="showEdit">
      <div class="card" style="max-width: 520px; margin: 80px auto">
        <div class="section-title">编辑快递</div>
        <n-form ref="editFormRef" :model="editForm" :rules="editRules" label-placement="top">
          <n-form-item label="快递单号" path="trackingNumber">
            <n-input v-model:value="editForm.trackingNumber" />
          </n-form-item>
          <n-form-item label="物流公司">
            <n-input v-model:value="editForm.logisticsCompany" />
          </n-form-item>
          <n-form-item label="收件人姓名">
            <n-input v-model:value="editForm.receiverName" />
          </n-form-item>
          <n-form-item label="收件人手机号" path="receiverPhone">
            <n-input v-model:value="editForm.receiverPhone" />
          </n-form-item>
          <n-form-item label="取件人手机号" path="pickupPhone">
            <n-input v-model:value="editForm.pickupPhone" />
          </n-form-item>
          <n-form-item label="状态">
            <n-select v-model:value="editForm.status" :options="statusOptions" />
          </n-form-item>
          <n-form-item label="备注">
            <n-input v-model:value="editForm.remark" />
          </n-form-item>
          <div class="flex">
            <n-button @click="showEdit = false">取消</n-button>
            <n-button type="primary" :loading="updating" @click="handleUpdate">保存</n-button>
          </div>
        </n-form>
      </div>
    </n-modal>

    <n-modal v-model:show="showRelocate">
      <div class="card" style="max-width: 520px; margin: 80px auto">
        <div class="section-title">快递换柜</div>
        <n-form ref="relocateFormRef" :model="relocateForm" :rules="relocateRules" label-placement="top">
          <n-form-item label="尺寸类型" path="sizeType">
            <n-select v-model:value="relocateForm.sizeType" :options="sizeOptions" />
          </n-form-item>
          <n-form-item label="货架编号">
            <n-input-number v-model:value="relocateForm.shelfCode" :min="1" clearable placeholder="为空则自动分配" />
          </n-form-item>
          <n-form-item label="货架层数">
            <n-input-number v-model:value="relocateForm.shelfLayer" :min="1" clearable placeholder="为空则自动分配" />
          </n-form-item>
          <div class="flex">
            <n-button @click="showRelocate = false">取消</n-button>
            <n-button type="primary" :loading="relocating" @click="handleRelocate">保存</n-button>
          </div>
        </n-form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, h, onMounted, reactive, ref, watch } from 'vue';
import { NButton, useMessage } from 'naive-ui';
import { api } from '../../api';
import { buildRequiredSelectRule, PHONE_REGEX } from '../../constants/validation';
import { useAuthStore } from '../../stores/auth';

const message = useMessage();
const rows = ref([]);
const loading = ref(false);
const updating = ref(false);
const relocating = ref(false);
const deletingId = ref(null);
const checkoutingId = ref(null);
const auth = useAuthStore();
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
  trackingNumber: '',
  receiverPhone: '',
  status: null,
  overdueOnly: false
});

const applyUserDefaultFilters = () => {
  if (auth.role === 'USER' && filters.status == null) {
    filters.status = 1;
  }
};

const statusOptions = [
  { label: '待入库', value: 0 },
  { label: '待取件', value: 1 },
  { label: '已取件', value: 2 },
  { label: '已退回', value: 3 }
];

const overdueOptions = [
  { label: '全部', value: false },
  { label: '仅滞留(48h+)', value: true }
];

const sizeOptions = [
  { label: '标准', value: 0 },
  { label: '大件', value: 1 },
  { label: '易碎', value: 2 },
  { label: '冷链', value: 3 }
];

const statusLabelMap = {
  0: '待入库',
  1: '待取件',
  2: '已取件',
  3: '已退回'
};

const getStatusLabel = (value) => statusLabelMap[value] ?? String(value ?? '-');
const formatDateTime = (value) => (value ? String(value).replace('T', ' ') : '-');
const parseDateTime = (value) => {
  if (!value) {
    return null;
  }
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return null;
  }
  return parsed;
};
const getOverdueDays = (row) => {
  if (row?.status !== 1) {
    return null;
  }
  const createdAt = parseDateTime(row.createTime);
  if (!createdAt) {
    return null;
  }
  const elapsedMs = Date.now() - createdAt.getTime();
  if (elapsedMs < 48 * 60 * 60 * 1000) {
    return null;
  }
  return Math.max(2, Math.floor(elapsedMs / (24 * 60 * 60 * 1000)));
};
const getOverdueLabel = (row) => {
  const overdueDays = getOverdueDays(row);
  return overdueDays == null ? '-' : `已滞留 ${overdueDays} 天`;
};

const showEdit = ref(false);
const editFormRef = ref(null);
const editForm = reactive({
  id: null,
  trackingNumber: '',
  logisticsCompany: '',
  receiverName: '',
  receiverPhone: '',
  pickupPhone: '',
  status: 1,
  remark: ''
});

const showRelocate = ref(false);
const relocateFormRef = ref(null);
const relocateForm = reactive({
  id: null,
  sizeType: 0,
  shelfCode: null,
  shelfLayer: null
});

const editRules = {
  trackingNumber: { required: true, message: '快递单号不能为空', trigger: ['blur', 'input'] },
  receiverPhone: {
    validator: () => !editForm.receiverPhone || PHONE_REGEX.test(editForm.receiverPhone),
    message: '手机号格式不正确',
    trigger: ['blur', 'input']
  },
  pickupPhone: {
    validator: () => !editForm.pickupPhone || PHONE_REGEX.test(editForm.pickupPhone),
    message: '手机号格式不正确',
    trigger: ['blur', 'input']
  }
};

const relocateRules = {
  sizeType: buildRequiredSelectRule('尺寸类型不能为空')
};

const fetchList = async () => {
  try {
    loading.value = true;
    const trackingNumber = filters.trackingNumber.trim() || undefined;
    const receiverPhone = isStaffOrAdmin.value
      ? (filters.receiverPhone.trim() || undefined)
      : (auth.user?.username || undefined);
    const res = await api.listExpresses({
      trackingNumber,
      receiverPhone,
      status: filters.status ?? undefined,
      overdueOnly: isStaffOrAdmin.value && filters.overdueOnly ? true : undefined
    });
    rows.value = res.data || [];
    pagination.page = 1;
  } catch (err) {
    return;
  } finally {
    loading.value = false;
  }
};

const handleDelete = async (row) => {
  try {
    deletingId.value = row.id;
    await api.deleteExpress(row.id);
    message.success('删除成功');
    await fetchList();
  } catch (err) {
    return;
  } finally {
    deletingId.value = null;
  }
};

const openEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    trackingNumber: row.trackingNumber,
    logisticsCompany: row.logisticsCompany,
    receiverName: row.receiverName,
    receiverPhone: row.receiverPhone,
    pickupPhone: row.pickupPhone,
    status: row.status,
    remark: row.remark
  });
  showEdit.value = true;
};

const openRelocate = (row) => {
  Object.assign(relocateForm, {
    id: row.id,
    sizeType: row.sizeType ?? 0,
    shelfCode: null,
    shelfLayer: null
  });
  showRelocate.value = true;
};

const handleUpdate = async () => {
  try {
    updating.value = true;
    await editFormRef.value?.validate();
    const payload = {
      trackingNumber: editForm.trackingNumber,
      logisticsCompany: editForm.logisticsCompany,
      receiverName: editForm.receiverName,
      receiverPhone: editForm.receiverPhone,
      pickupPhone: editForm.pickupPhone,
      status: editForm.status,
      remark: editForm.remark
    };
    await api.updateExpress(editForm.id, payload);
    message.success('更新成功');
    showEdit.value = false;
    await fetchList();
  } catch (err) {
    if (err?.errors) {
      return;
    }
    return;
  } finally {
    updating.value = false;
  }
};

const handleRelocate = async () => {
  try {
    relocating.value = true;
    await relocateFormRef.value?.validate();
    await api.relocateExpress(relocateForm.id, {
      sizeType: relocateForm.sizeType,
      shelfCode: relocateForm.shelfCode ?? null,
      shelfLayer: relocateForm.shelfLayer ?? null
    });
    message.success('换柜成功');
    showRelocate.value = false;
    await fetchList();
  } catch (err) {
    if (err?.errors) {
      return;
    }
    return;
  } finally {
    relocating.value = false;
  }
};

const handleCheckout = async (row) => {
  try {
    checkoutingId.value = row.id;
    await api.checkOut(row.trackingNumber);
    message.success('出库成功');
    await fetchList();
  } catch (err) {
    return;
  } finally {
    checkoutingId.value = null;
  }
};

const columns = computed(() => {
  const baseColumns = [
    { title: '单号', key: 'trackingNumber' },
    { title: '取件码', key: 'pickupCode' },
    { title: '物流公司', key: 'logisticsCompany' },
    { title: '收件人', key: 'receiverName' },
    { title: '手机号', key: 'receiverPhone' },
    { title: '货架', key: 'shelfCode' },
    { title: '层', key: 'shelfLayer' },
    {
      title: '状态',
      key: 'status',
      render(row) {
        return getStatusLabel(row.status);
      }
    },
    {
      title: '滞留状态',
      key: 'overdue',
      render(row) {
        return getOverdueLabel(row);
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

  if (auth.role === 'USER') {
    return [
      ...baseColumns,
      {
        title: '操作',
        key: 'actions',
        render(row) {
          return h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              disabled: row.status !== 1,
              loading: checkoutingId.value === row.id,
              onClick: () => handleCheckout(row)
            },
            { default: () => '出库' }
          );
        }
      }
    ];
  }

  if (!['STAFF', 'ADMIN'].includes(auth.role)) {
    return baseColumns;
  }

  return [
    ...baseColumns,
    {
      title: '操作',
      key: 'actions',
      render(row) {
        return [
          h(
            NButton,
            { size: 'small', type: 'primary', onClick: () => openEdit(row), style: 'margin-right: 8px' },
            { default: () => '编辑' }
          ),
          h(
            NButton,
            { size: 'small', onClick: () => openRelocate(row), style: 'margin-right: 8px' },
            { default: () => '换柜' }
          ),
          h(
            NButton,
            { size: 'small', type: 'error', loading: deletingId.value === row.id, onClick: () => handleDelete(row) },
            { default: () => '删除' }
          )
        ];
      }
    }
  ];
});

watch(
  () => auth.role,
  () => {
    applyUserDefaultFilters();
  },
  { immediate: true }
);

onMounted(() => {
  applyUserDefaultFilters();
  fetchList();
});
</script>
