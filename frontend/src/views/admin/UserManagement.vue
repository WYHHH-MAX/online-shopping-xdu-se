<template>
  <div class="user-management">
    <h2>用户管理</h2>
    
    <!-- 搜索表单 -->
    <a-form layout="inline" class="search-form">
      <a-form-item label="Username">
        <a-input v-model:value="searchForm.username" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="role">
        <a-select
          v-model:value="searchForm.role"
          placeholder="Please select a role"
          style="width: 120px"
          :allowClear="true"
        >
          <a-select-option :value="0">Regular users</a-select-option>
          <a-select-option :value="1">Seller</a-select-option>
          <a-select-option :value="2">administrator</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="state">
        <a-select
          v-model:value="searchForm.status"
          placeholder="Please select a status"
          style="width: 120px"
          :allowClear="true"
        >
          <a-select-option :value="0">disable</a-select-option>
          <a-select-option :value="1">enable</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="handleSearch">Search</a-button>
        <a-button style="margin-left: 8px" @click="handleReset">reset</a-button>
      </a-form-item>
    </a-form>
    
    <!-- 用户表格 -->
    <a-table
      :columns="columns"
      :data-source="userData"
      :pagination="pagination"
      :loading="loading"
      @change="handleTableChange"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a-switch
              :checked="record.status === 1"
              @change="(checked: boolean) => changeUserStatus(record.id, checked ? 1 : 0)"
              :checkedChildren="'启用'"
              :unCheckedChildren="'禁用'"
            />
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getUserList, updateUserStatus } from '@/api/admin';

interface User {
  id: number;
  username: string;
  phone: string;
  email: string;
  role: number;
  status: number;
  createTime: string;
  avatar: string;
}

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    customRender: ({ text }: { text: number }) => {
      const roleMap = {
        0: 'Regular users',
        1: 'Seller',
        2: 'administrator',
      };
      return roleMap[text as keyof typeof roleMap] || 'Unknown';
    },
  },
  {
    title: 'status',
    dataIndex: 'status',
    key: 'status',
    customRender: ({ text }: { text: number }) => {
      const statusMap = {
        0: 'disable',
        1: 'enable',
      };
      return statusMap[text as keyof typeof statusMap] || 'Unknown';
    },
  },
  {
    title: 'regist time',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: 'operation',
    key: 'action',
  },
];

const searchForm = ref({
  username: '',
  role: undefined,
  status: undefined,
});

const userData = ref<User[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
});
const loading = ref(false);

// 加载用户数据
const loadUserData = async () => {
  loading.value = true;
  try {
    // console.log('正在获取用户数据...');
    const params = {
      username: searchForm.value.username || undefined,
      role: searchForm.value.role,
      status: searchForm.value.status,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize,
    };
    
    // console.log('请求参数:', params);
    const res: any = await getUserList(params);
    // console.log('用户列表响应原始数据:', res);
    
    // 处理响应数据，确保可以正确获取列表和总数
    if (res.code === 1 || res.code === 200) {
      // console.log('请求成功, 状态码:', res.code);
      if (res.data && res.data.list) {
        // console.log('标准格式: {data: {list: [], total: number}}');
        userData.value = res.data.list;
        pagination.value.total = res.data.total || 0;
      } else if (res.data && Array.isArray(res.data)) {
        // console.log('数组格式: {data: []}');
        userData.value = res.data;
        pagination.value.total = res.data.length;
      } else if (res.list && Array.isArray(res.list)) {
        // console.log('直接分页格式: {list: [], total: number}');
        userData.value = res.list;
        pagination.value.total = res.total || 0;
      } else if (res.records && Array.isArray(res.records)) {
        // console.log('MyBatis-Plus格式: {records: [], total: number}');
        userData.value = res.records;
        pagination.value.total = res.total || 0;
      } else {
        // console.error('未识别的响应数据格式:', res);
        userData.value = [];
        pagination.value.total = 0;
        message.error('获取用户列表数据格式错误');
      }
    } else {
      // console.error('请求失败, 状态码:', res.code, '消息:', res.msg);
      userData.value = [];
      pagination.value.total = 0;
      message.error(res.msg || '获取用户列表失败');
    }
    
    // console.log('处理后的用户数据:', userData.value);
    // console.log('分页信息:', pagination.value);
  } catch (error) {
    console.error('加载用户数据失败', error);
    userData.value = [];
    pagination.value.total = 0;
    message.error('加载用户数据失败');
  } finally {
    loading.value = false;
  }
};

// 处理分页变化
const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadUserData();
};

// 搜索
const handleSearch = () => {
  pagination.value.current = 1;
  loadUserData();
};

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    username: '',
    role: undefined,
    status: undefined,
  };
  pagination.value.current = 1;
  loadUserData();
};

// 启用/禁用用户
const changeUserStatus = async (id: number, status: number) => {
  try {
    await updateUserStatus(id, status);
    message.success(`${status === 1 ? '启用' : '禁用'}用户成功`);
    loadUserData();
  } catch (error) {
    // console.error('更新用户状态失败', error);
    message.error('操作失败');
  }
};

onMounted(() => {
  loadUserData();
});
</script>

<style scoped>
.user-management {
  padding: 24px;
}
.search-form {
  margin-bottom: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 2px;
}
.ant-form-item {
  margin-bottom: 16px;
}
</style> 