<template>
  <div class="page">
    <div class="card flex-column">
      <div class="section-title">用户管理</div>
      <div class="flex">
        <n-input v-model:value="filters.username" placeholder="手机号" />
        <n-select v-model:value="filters.role" :options="roleOptions" placeholder="角色" />
        <n-button type="primary" :loading="loading" @click="fetchList">查询</n-button>
        <n-button type="primary" @click="openCreate">新增用户</n-button>
      </div>
      <n-data-table :columns="columns" :data="rows" :loading="loading" :bordered="false" :pagination="pagination" />
    </div>

    <n-modal v-model:show="showCreate">
      <div class="card" style="max-width: 520px; margin: 80px auto">
        <div class="section-title">新增用户</div>
        <n-form ref="createFormRef" :model="createForm" :rules="createRules" label-placement="top">
          <n-form-item label="手机号" path="username">
            <n-input v-model:value="createForm.username" />
          </n-form-item>
          <n-form-item label="密码" path="password">
            <n-input v-model:value="createForm.password" type="password" />
          </n-form-item>
          <n-form-item label="角色" path="role">
            <n-select v-model:value="createForm.role" :options="roleOptions" />
          </n-form-item>
          <n-form-item label="昵称">
            <n-input v-model:value="createForm.nickname" />
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
        <div class="section-title">编辑用户</div>
        <n-form ref="editFormRef" :model="editForm" :rules="editRules" label-placement="top">
          <n-form-item label="手机号" path="username">
            <n-input v-model:value="editForm.username" />
          </n-form-item>
          <n-form-item label="密码(留空不修改)" path="password">
            <n-input v-model:value="editForm.password" type="password" />
          </n-form-item>
          <n-form-item label="昵称">
            <n-input v-model:value="editForm.nickname" />
          </n-form-item>
          <n-form-item label="邮箱">
            <n-input v-model:value="editForm.email" />
          </n-form-item>
          <n-form-item label="角色" path="role">
            <n-select v-model:value="editForm.role" :options="roleOptions" />
          </n-form-item>
          <n-form-item label="状态" path="status">
            <n-select v-model:value="editForm.status" :options="statusOptions" />
          </n-form-item>
          <div class="flex">
            <n-button @click="showEdit = false">取消</n-button>
            <n-button type="primary" :loading="updating" @click="handleUpdate">保存</n-button>
          </div>
        </n-form>
        <n-text depth="3">提示：管理员不能修改其他管理员账号，也不能修改自己的角色。</n-text>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { h, onMounted, reactive, ref } from 'vue';
import { NButton, useMessage } from 'naive-ui';
import { api } from '../../api';
import { usePagedTable } from '../../composables/usePagedTable';
import { buildRequiredSelectRule, PASSWORD_RULE, PHONE_RULE } from '../../constants/validation';
import { formatLabel, toTrimmedOrUndefined } from '../../utils/format';

const message = useMessage();
const creating = ref(false);
const updating = ref(false);
const deletingId = ref(null);

const filters = reactive({
  username: '',
  role: null
});

const showCreate = ref(false);
const createFormRef = ref(null);
const createForm = reactive({
  username: '',
  password: '',
  role: 'STAFF',
  nickname: ''
});

const showEdit = ref(false);
const editFormRef = ref(null);
const editForm = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  role: 'STAFF',
  status: 1
});

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '员工', value: 'STAFF' },
  { label: '用户', value: 'USER' }
];

const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '正常', value: 1 }
];

const editRules = {
  username: { ...PHONE_RULE },
  password: {
    validator: () =>
      editForm.password === '' || (editForm.password.length >= 6 && editForm.password.length <= 20),
    message: '密码长度需在6-20之间',
    trigger: ['blur', 'input']
  },
  role: buildRequiredSelectRule('角色不能为空'),
  status: buildRequiredSelectRule('状态不能为空')
};

const createRules = {
  username: { ...PHONE_RULE },
  password: { ...PASSWORD_RULE },
  role: buildRequiredSelectRule('角色不能为空')
};

const roleLabelMap = {
  ADMIN: '管理员',
  STAFF: '员工',
  USER: '用户'
};

const statusLabelMap = {
  0: '禁用',
  1: '正常'
};

const {
  rows,
  loading,
  pagination,
  fetchList: fetchPage,
  search: searchPage
} = usePagedTable(({ page, pageSize }) => api.listUsers({
  username: toTrimmedOrUndefined(filters.username),
  role: filters.role || undefined,
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

const resetCreateForm = () => {
  createForm.username = '';
  createForm.password = '';
  createForm.role = 'STAFF';
  createForm.nickname = '';
};

const openCreate = () => {
  resetCreateForm();
  showCreate.value = true;
};

const handleCreate = async () => {
  try {
    creating.value = true;
    await createFormRef.value?.validate();
    await api.createUser({
      username: createForm.username,
      password: createForm.password,
      role: createForm.role,
      nickname: createForm.nickname
    });
    message.success('新增成功');
    showCreate.value = false;
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

const openEdit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname,
    email: row.email,
    role: row.role,
    status: row.status ?? 1
  });
  showEdit.value = true;
};

const handleUpdate = async () => {
  try {
    updating.value = true;
    await editFormRef.value?.validate();
    const payload = {
      username: editForm.username,
      password: editForm.password || null,
      nickname: editForm.nickname,
      email: editForm.email,
      role: editForm.role,
      status: editForm.status
    };
    await api.updateUser(editForm.id, payload);
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
  if (!window.confirm(`确认删除用户 ${row.username} 吗？`)) {
    return;
  }
  try {
    deletingId.value = row.id;
    await api.deleteUser(row.id);
    message.success('删除成功');
    await fetchPage();
  } catch (err) {
    return;
  } finally {
    deletingId.value = null;
  }
};

const columns = [
  { title: 'ID', key: 'id' },
  { title: '手机号', key: 'username' },
  { title: '昵称', key: 'nickname' },
  {
    title: '角色',
    key: 'role',
    render(row) {
      return formatLabel(roleLabelMap, row.role);
    }
  },
  {
    title: '状态',
    key: 'status',
    render(row) {
      return formatLabel(statusLabelMap, row.status);
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
