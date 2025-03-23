<template>
  <div class="product-form">
    <a-page-header
      :title="isEdit ? '编辑商品' : '添加商品'"
      @back="goBack"
    />
    
    <a-form
      :model="formState"
      :rules="rules"
      ref="formRef"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="商品名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入商品名称" />
      </a-form-item>
      
      <a-form-item label="商品分类" name="categoryId">
        <a-cascader
          v-model:value="formState.categoryId"
          :options="categoryOptions"
          :field-names="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择商品分类"
        />
      </a-form-item>
      
      <a-form-item label="商品价格" name="price">
        <a-input-number 
          v-model:value="formState.price" 
          :precision="2"
          :min="0"
          style="width: 200px" 
        />
      </a-form-item>
      
      <a-form-item label="商品库存" name="stock">
        <a-input-number 
          v-model:value="formState.stock" 
          :min="0"
          style="width: 200px" 
        />
      </a-form-item>
      
      <a-form-item label="商品状态" name="status">
        <a-radio-group v-model:value="formState.status">
          <a-radio :value="1">上架</a-radio>
          <a-radio :value="0">下架</a-radio>
        </a-radio-group>
      </a-form-item>
      
      <a-form-item label="商品图片" name="images">
        <a-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :customRequest="customUpload"
          :beforeUpload="beforeUpload"
        >
          <div v-if="fileList.length < 5">
            <upload-outlined />
            <div style="margin-top: 8px">上传</div>
          </div>
        </a-upload>
      </a-form-item>
      
      <a-form-item label="商品详情" name="description">
        <a-textarea 
          v-model:value="formState.description" 
          placeholder="请输入商品详情" 
          :rows="6" 
        />
      </a-form-item>
      
      <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
        <a-button type="primary" @click="handleSubmit" :loading="submitting">提交</a-button>
        <a-button style="margin-left: 10px" @click="goBack">取消</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import type { FormInstance } from 'ant-design-vue';
import { getCategoryTree } from '@/api/category';
import { addProduct, updateProduct, uploadProductImage } from '@/api/seller';
import { getProductDetail } from '@/api/product';

// 定义表单状态接口
interface ProductForm {
  id?: number;
  name: string;
  categoryId: number | null;
  price: number;
  stock: number;
  status: number;
  description: string;
  mainImage: string;
  images: string[];
}

const router = useRouter();
const route = useRoute();
const formRef = ref<FormInstance>();
const submitting = ref(false);
const fileList = ref<any[]>([]);

// 判断是否为编辑模式
const isEdit = ref(!!route.params.id);

// 初始化表单数据
const formState = reactive<ProductForm>({
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  status: 1,
  description: '',
  mainImage: '',
  images: []
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入商品名称' }],
  categoryId: [{ required: true, message: '请选择商品分类' }],
  price: [{ required: true, message: '请输入商品价格' }],
  stock: [{ required: true, message: '请输入商品库存' }]
};

// 分类选项
const categoryOptions = ref<any[]>([]);

// 获取分类数据
const fetchCategories = async () => {
  try {
    const data = await getCategoryTree();
    categoryOptions.value = data;
  } catch (error: any) {
    message.error('获取分类数据失败: ' + error.message);
  }
};

// 获取商品详情
const fetchProductDetail = async (id: number) => {
  try {
    const data = await getProductDetail(id);
    
    // 填充表单数据
    Object.assign(formState, data);
    
    // 设置图片列表
    if (data.images && data.images.length > 0) {
      fileList.value = data.images.map((url: string, index: number) => ({
        uid: -(index + 1),
        name: `image-${index + 1}.jpg`,
        status: 'done',
        url
      }));
    }
  } catch (error: any) {
    message.error('获取商品详情失败: ' + error.message);
  }
};

// 上传前校验
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('只能上传JPG/PNG格式的图片!');
    return false;
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('图片必须小于2MB!');
    return false;
  }
  
  return true;
};

// 自定义上传
const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options;
  
  try {
    const url = await uploadProductImage(file);
    
    // 如果是第一张图片，设置为主图
    if (fileList.value.length === 0) {
      formState.mainImage = url;
    }
    
    // 添加到图片列表
    formState.images.push(url);
    
    onSuccess(url, file);
  } catch (error: any) {
    message.error('上传图片失败: ' + error.message);
    onError(error);
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    // 确保至少有一张图片
    if (formState.images.length === 0) {
      message.error('请至少上传一张商品图片');
      return;
    }
    
    // 确保categoryId不为null
    if (formState.categoryId === null) {
      message.error('请选择商品分类');
      return;
    }
    
    submitting.value = true;
    
    // 构建请求数据对象，过滤掉可能为null的属性
    const productData = {
      ...formState,
      categoryId: formState.categoryId as number
    };
    
    if (isEdit.value) {
      // 编辑商品
      await updateProduct(Number(route.params.id), productData);
      message.success('商品更新成功');
    } else {
      // 新增商品
      await addProduct(productData);
      message.success('商品添加成功');
    }
    
    // 返回商品列表
    router.push('/seller/products');
  } catch (error: any) {
    message.error('提交失败: ' + error.message);
  } finally {
    submitting.value = false;
  }
};

// 返回上一页
const goBack = () => {
  router.push('/seller/products');
};

onMounted(async () => {
  // 获取分类数据
  await fetchCategories();
  
  // 如果是编辑模式，获取商品详情
  if (isEdit.value && route.params.id) {
    await fetchProductDetail(Number(route.params.id));
  }
});
</script>

<style scoped>
.product-form {
  padding: 20px;
}
</style> 