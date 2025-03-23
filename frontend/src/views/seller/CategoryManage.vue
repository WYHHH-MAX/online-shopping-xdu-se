<template>
  <div class="category-manage">
    <div class="header">
      <a-button type="primary" @click="showCreateModal">新建分类</a-button>
    </div>
    
    <a-table
      :columns="columns"
      :data-source="categoryTree"
      :pagination="false"
      :loading="loading"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="showUpdateModal(record)">编辑</a>
            <a-popconfirm
              title="确定要删除这个分类吗？"
              @confirm="handleDelete(record.id)"
            >
              <a class="danger">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="父级分类" name="parentId">
          <a-cascader
            v-model:value="formData.parentId"
            :options="categoryOptions"
            :field-names="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择父级分类"
            change-on-select
            @change="handleParentChange"
          />
        </a-form-item>
        <a-form-item label="分类名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入分类名称" />
        </a-form-item>
        <a-form-item label="排序" name="sort">
          <a-input-number v-model:value="formData.sort" :min="0" />
        </a-form-item>
        <a-form-item label="图标" name="icon">
          <a-input v-model:value="formData.icon" placeholder="请输入图标URL" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import type { CategoryVO } from '@/types/category'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory
} from '@/api/category'

// 定义一个包含可能有id的类型
type CategoryForm = Partial<CategoryVO> & { id?: number };

const columns = [
  {
    title: '分类名称',
    dataIndex: 'name',
    key: 'name'
  },
  {
    title: '层级',
    dataIndex: 'level',
    key: 'level'
  },
  {
    title: '排序',
    dataIndex: 'sort',
    key: 'sort'
  },
  {
    title: '操作',
    key: 'action',
    width: 200
  }
]

const loading = ref(false)
const categoryTree = ref<CategoryVO[]>([])
const categoryOptions = ref<CategoryVO[]>([])

const modalVisible = ref(false)
const modalTitle = ref('')
const formRef = ref<FormInstance>()
const formData = ref<CategoryForm>({
  parentId: 0,
  name: '',
  level: 1,
  sort: 0,
  icon: ''
})

const rules = {
  parentId: [{ required: true, message: '请选择父级分类' }],
  name: [{ required: true, message: '请输入分类名称' }],
  level: [{ required: true, message: '请选择分类层级' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getCategoryTree()
    categoryTree.value = data
    // 添加一个顶级分类选项
    const rootCategory: CategoryVO = {
      id: 0,
      parentId: -1,
      name: '顶级分类',
      level: 0,
      sort: 0
    }
    categoryOptions.value = [rootCategory, ...data]
  } catch (error) {
    message.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateModal = () => {
  modalTitle.value = '新建分类'
  formData.value = {
    parentId: 0,
    name: '',
    level: 1,
    sort: 0,
    icon: ''
  }
  modalVisible.value = true
}

const showUpdateModal = (record: CategoryVO) => {
  modalTitle.value = '编辑分类'
  formData.value = { ...record }
  modalVisible.value = true
}

const handleParentChange = (value: number[], selectedOptions: CategoryVO[]) => {
  const parent = selectedOptions[selectedOptions.length - 1]
  formData.value.level = parent ? parent.level + 1 : 1
}

const handleModalOk = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const formValues = { ...formData.value }
    
    if (formValues.id) {
      // 更新分类
      await updateCategory(formValues as CategoryVO)
      message.success('更新成功')
    } else {
      // 创建分类时移除id字段
      delete formValues.id
      await createCategory(formValues as CategoryVO)
      message.success('创建成功')
    }
    
    modalVisible.value = false
    fetchData()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleModalCancel = () => {
  modalVisible.value = false
}

const handleDelete = async (id: number) => {
  try {
    await deleteCategory(id)
    message.success('删除成功')
    fetchData()
  } catch (error) {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.category-manage {
  padding: 24px;
}

.header {
  margin-bottom: 16px;
}

.danger {
  color: #ff4d4f;
}
</style> 