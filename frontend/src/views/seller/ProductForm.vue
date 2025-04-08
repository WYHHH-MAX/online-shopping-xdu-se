<template>
  <div class="product-form">
    <a-page-header
      :title="isEdit ? 'Edit the product' : 'Add a product'"
      @back="goBack"
    />
    
    <a-form
      :model="formState"
      :rules="rules"
      ref="formRef"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="name" name="name">
        <a-input v-model:value="formState.name" placeholder="Please enter the product name" />
      </a-form-item>
      
      <a-form-item label="categoryId" name="categoryId">
        <a-cascader
          v-model:value="formState.categoryId"
          :options="categoryOptions"
          :field-names="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="Please select a product category"
          @change="handleCategoryChange"
          :show-search="{ 
            filter: (inputValue: string, path: any[]) => 
              path.some(option => option.name.toLowerCase().indexOf(inputValue.toLowerCase()) > -1) 
          }"
        />
      </a-form-item>
      
      <a-form-item label="price" name="price">
        <a-input-number 
          v-model:value="formState.price" 
          :precision="2"
          :min="0"
          style="width: 200px" 
        />
      </a-form-item>
      
      <a-form-item label="stock" name="stock">
        <a-input-number 
          v-model:value="formState.stock" 
          :min="0"
          style="width: 200px" 
        />
      </a-form-item>
      
      <a-form-item label="status" name="status">
        <a-radio-group v-model:value="formState.status">
          <a-radio :value="1">Shelves</a-radio>
          <a-radio :value="0">Taken off the shelves</a-radio>
        </a-radio-group>
      </a-form-item>
      
      <a-form-item label="images" name="images">
        <a-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :customRequest="customUpload"
          :beforeUpload="beforeUpload"
          :remove="onRemove"
        >
          <div v-if="fileList.length < 5">
            <upload-outlined />
            <div style="margin-top: 8px">upload</div>
          </div>
        </a-upload>
      </a-form-item>
      
      <a-form-item label="description" name="description">
        <a-textarea 
          v-model:value="formState.description" 
          placeholder="Please enter your product details"
          :rows="6" 
        />
      </a-form-item>
      
      <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
        <a-button type="primary" @click="handleSubmit" :loading="submitting">submit</a-button>
        <a-button style="margin-left: 10px" @click="goBack">cancel</a-button>
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
import { addProduct, updateProduct, uploadProductImage, deleteAllProductImages } from '@/api/seller';
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
    // 处理分类数据，确保显示名称
    categoryOptions.value = data.map(category => ({
      ...category,
      label: category.name,
      value: category.id,
      children: category.children?.map(child => ({
        ...child,
        label: child.name,
        value: child.id
      }))
    }));
    // console.log('处理后的分类数据:', categoryOptions.value);
  } catch (error: any) {
    message.error('获取分类数据失败: ' + error.message);
  }
};

// 获取商品详情
const fetchProductDetail = async (id: number) => {
  try {
    const data = await getProductDetail(id);
    console.log('获取到的商品详情:', data);
    
    // 填充表单数据
    Object.assign(formState, data);
    
    // 清空当前图片列表和formState.images数组，避免累积
    fileList.value = [];
    formState.images = [];
    
    // 设置图片列表 - 后端只会返回未删除的图片
    if (data.images && data.images.length > 0) {
      fileList.value = data.images.map((url: string, index: number) => ({
        uid: -(index + 1),
        name: `image-${index + 1}.jpg`,
        status: 'done',
        url
      }));
      
      // 同步更新formState.images数组
      formState.images = [...data.images];
      console.log('加载商品详情时的图片列表:', formState.images);
    }
  } catch (error: any) {
    console.error('获取商品详情失败:', error);
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
    console.log("开始上传图片:", file.name);
    let url;
    if (isEdit.value && route.params.id) {
      // 如果是编辑模式，传递商品ID和图片序号
      const productId = Number(route.params.id);
      // 使用当前fileList长度+1作为新图片的序号
      const imageIndex = fileList.value.length + 1;
      url = await uploadProductImage(file, productId, imageIndex);
    } else {
      // 新增商品模式，不传递商品ID
      url = await uploadProductImage(file);
    }
    
    console.log("上传图片成功，返回URL:", url);
    
    // 验证URL是否有效
    if (!url || typeof url !== 'string' || url.trim() === '') {
      throw new Error('上传图片返回的URL无效');
    }
    
    // 成功上传后，将URL添加到formState.images数组中
    if (!formState.images.includes(url)) {
      formState.images.push(url);
      console.log('图片已添加到formState.images中:', url);
      console.log('当前images数组:', formState.images);
    }
    
    // 如果是第一张图片，设置为主图
    if (fileList.value.length === 0) {
      formState.mainImage = url;
    }
    
    onSuccess(url, file);
    message.success('图片上传成功');
  } catch (error: any) {
    console.error('上传图片失败:', error, '文件信息:', file.name, file.size);
    message.error('上传图片失败: ' + error.message);
    onError(error);
  }
};

// 处理图片移除操作
const onRemove = (file: any) => {
  const url = file.url || file.response;
  
  if (url && typeof url === 'string') {
    console.log('从前端移除图片:', url);
    
    // 从formState.images数组中移除图片
    formState.images = formState.images.filter(item => item !== url);
    console.log('删除图片后的formState.images:', formState.images);
    
    // 检查是否删除的是主图
    if (url === formState.mainImage) {
      console.log('主图被删除，重新设置主图');
      // 如果还有其他图片，将第一张设为主图
      if (formState.images.length > 0) {
        formState.mainImage = formState.images[0];
        console.log('新的主图设置为:', formState.mainImage);
      } else {
        // 没有图片了，清空主图
        formState.mainImage = '';
        console.log('没有图片了，清空主图');
      }
    }
    
    message.success('图片已从列表中移除');
  }
  
  return true;
};

// 添加处理分类选择的方法
const handleCategoryChange = (value: number[]) => {
  if (value && value.length > 0) {
    // 使用最后一个选中的分类ID
    formState.categoryId = value[value.length - 1];
  } else {
    formState.categoryId = null;
  }
};

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    // 确保至少有一张图片
    if (fileList.value.length === 0) {
      message.error('请至少上传一张商品图片');
      return;
    }
    
    // 确保categoryId不为null
    if (formState.categoryId === null) {
      message.error('请选择商品分类');
      return;
    }
    
    submitting.value = true;
    
    // 从fileList中获取当前实际显示的图片URLs
    const currentImages = fileList.value
      .filter(file => file.status === 'done') // 只包含上传成功的文件
      .map(file => file.url || file.response)
      .filter(url => url && typeof url === 'string' && url.trim() !== '')
      .map(url => url.trim());
    
    if (currentImages.length === 0) {
      message.error('没有有效的商品图片');
      submitting.value = false;
      return;
    }
    
    // 更新formState.images，确保与当前显示的图片完全一致
    formState.images = [...currentImages];
    console.log('提交前的images数组:', formState.images);
    
    // 确保设置主图为第一张图片
    if (currentImages.length > 0 && !formState.mainImage) {
      formState.mainImage = currentImages[0];
      console.log('自动设置主图为第一张图片:', formState.mainImage);
    }
    
    // 构建请求数据对象
    const productData = {
      ...formState,
      categoryId: formState.categoryId as number,
      price: Number(formState.price),
      stock: Number(formState.stock),
      status: Number(formState.status),
      images: formState.images, // 使用更新后的formState.images
      mainImage: formState.mainImage // 确保包含主图
    };
    
    console.log('提交的商品数据:', productData);
    
    try {
      if (isEdit.value) {
        const productId = Number(route.params.id);
        
        // 编辑模式下，先尝试删除所有旧图片
        try {
          console.log('尝试批量删除商品所有图片:', productId);
          await deleteAllProductImages(productId);
          console.log('成功批量删除商品所有图片');
        } catch (error) {
          console.warn('批量删除商品图片失败，继续更新商品:', error);
          // 继续执行，不中断流程
        }
        
        // 编辑商品 - 后端会处理图片的重命名和设置主图
        await updateProduct(productId, productData);
        message.success('商品更新成功');
      } else {
        // 新增商品 - 后端也会处理设置主图
        await addProduct(productData);
        message.success('商品添加成功');
      }
      
      // 返回商品列表
      router.push('/seller/products');
    } catch (error: any) {
      console.error('API请求失败:', error);
      message.error('提交失败: ' + error.message);
      submitting.value = false;
    }
  } catch (error: any) {
    console.error('表单验证失败:', error);
    message.error('表单验证失败: ' + error.message);
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