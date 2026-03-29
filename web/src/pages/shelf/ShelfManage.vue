<template>
  <div class="page">
    <div class="card flex-column">
      <div class="section-title">货架管理</div>
      <div class="flex">
        <n-input-number v-model:value="filters.shelfCode" placeholder="编号" :min="1" clearable />
        <n-input-number v-model:value="filters.shelfLayer" placeholder="层" :min="1" clearable />
        <n-select v-model:value="filters.status" :options="statusOptions" placeholder="状态" />
        <n-button type="primary" :loading="loading" @click="fetchList">查询</n-button>
        <n-button @click="resetFilters">重置</n-button>
        <n-button type="primary" @click="openCreate">新增货架</n-button>
      </div>
      <n-data-table :columns="columns" :data="rows" :loading="loading" :bordered="false" :pagination="pagination" />
    </div>

    <n-modal v-model:show="showCreate">
      <div class="card" style="max-width: 520px; margin: 80px auto">
        <div class="section-title">新增货架</div>
        <n-form ref="createFormRef" :model="createForm" :rules="createRules" label-placement="top">
          <n-form-item label="编号" path="shelfCode">
            <n-input-number v-model:value="createForm.shelfCode" :min="1" />
          </n-form-item>
          <n-form-item label="层" path="shelfLayer">
            <n-input-number v-model:value="createForm.shelfLayer" :min="1" />
          </n-form-item>
          <n-form-item label="名称">
            <n-input v-model:value="createForm.shelfName" />
          </n-form-item>
          <n-form-item label="类型" path="shelfType">
            <n-select v-model:value="createForm.shelfType" :options="typeOptions" />
          </n-form-item>
          <n-form-item label="容量" path="totalCapacity">
            <n-input-number v-model:value="createForm.totalCapacity" :min="1" />
          </n-form-item>
          <div class="flex">
            <n-button @click="showCreate = false">取消</n-button>
            <n-button type="primary" :loading="creating" @click="handleCreate">保存</n-button>
          </div>
        </n-form>
      </div>
    </n-modal>

    <n-modal v-model:show="showEdit">
      <div class="card" style="max-width: 520px; margin: 80px auto">
        <div class="section-title">编辑货架</div>
        <n-form ref="editFormRef" :model="editForm" :rules="editRules" label-placement="top">
          <n-form-item label="编号" path="shelfCode">
            <n-input-number v-model:value="editForm.shelfCode" :min="1" />
          </n-form-item>
          <n-form-item label="层" path="shelfLayer">
            <n-input-number v-model:value="editForm.shelfLayer" :min="1" />
          </n-form-item>
          <n-form-item label="名称">
            <n-input v-model:value="editForm.shelfName" />
          </n-form-item>
          <n-form-item label="类型" path="shelfType">
            <n-select v-model:value="editForm.shelfType" :options="typeOptions" />
          </n-form-item>
          <n-form-item label="容量" path="totalCapacity">
            <n-input-number v-model:value="editForm.totalCapacity" :min="1" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="editForm.status" :options="statusOptions" />
          </n-form-item>
          <div class="flex">
            <n-button @click="showEdit = false">取消</n-button>
            <n-button type="primary" :loading="updating" @click="handleUpdate">保存</n-button>
          </div>
        </n-form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, useMessage } from 'naive-ui';
import { api } from '../../api';
import { usePagedTable } from '../../composables/usePagedTable';
import { buildRequiredNumberRule, buildRequiredSelectRule } from '../../constants/validation';
import { formatLabel } from '../../utils/format';

const message = useMessage();
const creating = ref(false);
const updating = ref(false);
const deletingId = ref(null);
const filters = reactive({
  shelfCode: null,
  shelfLayer: null,
  status: null
});

const showCreate = ref(false);
const createFormRef = ref(null);
const createForm = reactive({
  shelfCode: null,
  shelfLayer: null,
  shelfName: '',
  shelfType: 0,
  totalCapacity: null
});

const showEdit = ref(false);
const editFormRef = ref(null);
const editForm = reactive({
  id: null,
  shelfCode: null,
  shelfLayer: null,
  shelfName: '',
  shelfType: 0,
  totalCapacity: null,
  status: 1
});

const typeOptions = [
  { label: '标准', value: 0 },
  { label: '大件', value: 1 },
  { label: '冷链', value: 2 },
  { label: '易碎', value: 3 }
];

const statusOptions = [
  { label: '维修', value: 0 },
  { label: '可用', value: 1 }
];

const typeLabelMap = {
  0: '标准',
  1: '大件',
  2: '冷链',
  3: '易碎'
};

const statusLabelMap = {
  0: '维修',
  1: '可用'
};

const getTypeLabel = (value) => formatLabel(typeLabelMap, value);
const getStatusLabel = (value) => formatLabel(statusLabelMap, value);
const getLoadRatioLabel = (row) => {
  const ratio = getLoadRatioNumber(row);
  return `${Math.round(ratio * 100)}%`;
};

const getLoadRatioNumber = (row) => {
  if (typeof row.loadRate === 'number') {
    return row.loadRate;
  }
  if (row.loadRate !== null && row.loadRate !== undefined && row.loadRate !== '') {
    return Number(row.loadRate);
  }
  const total = Number(row.totalCapacity || 0);
  if (total <= 0) {
    return 0;
  }
  const current = Number(row.currentUsage || 0);
  return current / total;
};

const getLoadRatioClass = (row) => {
  const ratio = getLoadRatioNumber(row);
  if (ratio >= 1) {
    return 'load-rate-over';
  }
  if (ratio >= 0.8) {
    return 'load-rate-high';
  }
  if (ratio >= 0.5) {
    return 'load-rate-medium';
  }
  return 'load-rate-low';
};

const getLoadRatioStyle = (row) => {
  const ratio = getLoadRatioNumber(row);
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

const editRules = {
  shelfCode: buildRequiredNumberRule('编号不能为空'),
  shelfLayer: buildRequiredNumberRule('层不能为空'),
  shelfType: buildRequiredSelectRule('类型不能为空'),
  totalCapacity: buildRequiredNumberRule('容量不能为空'),
  status: buildRequiredSelectRule('状态不能为空')
};

const createRules = {
  shelfCode: buildRequiredNumberRule('编号不能为空'),
  shelfLayer: buildRequiredNumberRule('层不能为空'),
  shelfType: buildRequiredSelectRule('类型不能为空'),
  totalCapacity: buildRequiredNumberRule('容量不能为空')
};

const {
  rows,
  loading,
  pagination,
  fetchList: fetchPage,
  search: searchPage
} = usePagedTable(({ page, pageSize }) => api.listShelves({
  shelfCode: filters.shelfCode || undefined,
  shelfLayer: filters.shelfLayer || undefined,
  status: filters.status ?? undefined,
  page,
  pageSize
}));

const fetchList = async () => {
  try {
    await searchPage();
  } catch (err) {
    return;
  }
};

const resetFilters = () => {
  filters.shelfCode = null;
  filters.shelfLayer = null;
  filters.status = null;
  fetchList();
};

const resetCreateForm = () => {
  createForm.shelfCode = null;
  createForm.shelfLayer = null;
  createForm.shelfName = '';
  createForm.shelfType = 0;
  createForm.totalCapacity = null;
};

const openCreate = () => {
  resetCreateForm();
  showCreate.value = true;
};

const handleCreate = async () => {
  try {
    creating.value = true;
    await createFormRef.value?.validate();
    await api.createShelf({
      shelfCode: createForm.shelfCode,
      shelfLayer: createForm.shelfLayer,
      shelfName: createForm.shelfName,
      shelfType: createForm.shelfType,
      totalCapacity: createForm.totalCapacity
    });
    message.success('新增成功');
    showCreate.value = false;
    resetCreateForm();
    await fetchPage();
  } catch (err) {
    if (err?.errors) {
      return;
    }
    return;
  } finally {
    creating.value = false;
  }
};

const openEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    shelfCode: row.shelfCode ?? null,
    shelfLayer: row.shelfLayer ?? null,
    shelfName: row.shelfName,
    shelfType: row.shelfType,
    totalCapacity: row.totalCapacity ?? null,
    status: row.status
  });
  showEdit.value = true;
};

const handleUpdate = async () => {
  try {
    updating.value = true;
    await editFormRef.value?.validate();
    await api.updateShelf(editForm.id, {
      shelfCode: editForm.shelfCode,
      shelfLayer: editForm.shelfLayer,
      shelfName: editForm.shelfName,
      shelfType: editForm.shelfType,
      totalCapacity: editForm.totalCapacity,
      status: editForm.status
    });
    message.success('更新成功');
    showEdit.value = false;
    await fetchPage();
  } catch (err) {
    if (err?.errors) {
      return;
    }
    return;
  } finally {
    updating.value = false;
  }
};

const handleDelete = async (row) => {
  if (!window.confirm(`确认删除货架 ${row.shelfCode}-${row.shelfLayer} 吗？`)) {
    return;
  }
  try {
    deletingId.value = row.id;
    await api.deleteShelf(row.id);
    message.success('删除成功');
    await fetchPage();
  } catch (err) {
    return;
  } finally {
    deletingId.value = null;
  }
};

const columns = [
  { title: '编号', key: 'shelfCode' },
  { title: '层', key: 'shelfLayer' },
  { title: '名称', key: 'shelfName' },
  {
    title: '类型',
    key: 'shelfType',
    render(row) {
      return getTypeLabel(row.shelfType);
    }
  },
  { title: '容量', key: 'totalCapacity' },
  {
    title: '当前负载',
    key: 'currentUsage',
    render(row) {
      const current = row.currentUsage ?? 0;
      const total = row.totalCapacity ?? 0;
      return `${current}/${total}`;
    }
  },
  {
    title: '负载率',
    key: 'loadRate',
    render(row) {
      return h(
        'span',
        { style: getLoadRatioStyle(row), class: getLoadRatioClass(row) },
        getLoadRatioLabel(row)
      );
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
          { size: 'small', type: 'error', loading: deletingId.value === row.id, onClick: () => handleDelete(row) },
          { default: () => '删除' }
        )
      ];
    }
  }
];

onMounted(fetchList);
</script>

<style scoped>
.load-rate-tag {
  display: inline-block;
  min-width: 52px;
  padding: 2px 8px;
  border-radius: 999px;
  text-align: center;
  font-weight: 600;
  font-size: 12px;
}

.load-rate-low {
  background: #e8f7ef;
  color: #1f7a45;
}

.load-rate-medium {
  background: #fff6dc;
  color: #8a5b00;
}

.load-rate-high {
  background: #ffe8d9;
  color: #b54708;
}

.load-rate-over {
  background: #fde8e8;
  color: #b42318;
}
</style>
