<template>
  <div class="seller-profile">
    <a-card title="店铺信息">
      <a-form
        :model="formState"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="店铺Logo">
          <div class="avatar-container">
            <img v-if="formState.shopLogo" :src="formState.shopLogo" alt="店铺Logo" class="shop-logo" />
            <div class="avatar-placeholder" v-else>
              <upload-outlined />
            </div>
            <div class="avatar-overlay">
              <a-upload 
                :show-upload-list="false"
                :before-upload="beforeLogoUpload"
                :customRequest="uploadLogo"
              >
                <div class="upload-text">更换Logo</div>
              </a-upload>
            </div>
          </div>
        </a-form-item>
        
        <a-form-item label="店铺名称" name="shopName">
          <a-input v-model:value="formState.shopName" placeholder="请输入店铺名称" />
        </a-form-item>
        
        <a-form-item label="店铺描述" name="description">
          <a-textarea v-model:value="formState.description" placeholder="请输入店铺描述" :rows="4" />
        </a-form-item>
        
        <a-form-item label="联系人" name="contactName">
          <a-input v-model:value="formState.contactName" placeholder="请输入联系人姓名" />
        </a-form-item>
        
        <a-form-item label="联系电话" name="contactPhone">
          <a-input v-model:value="formState.contactPhone" placeholder="请输入联系电话" />
        </a-form-item>
        
        <a-form-item label="联系邮箱" name="contactEmail">
          <a-input v-model:value="formState.contactEmail" placeholder="请输入联系邮箱" />
        </a-form-item>
        
        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-button type="primary" @click="handleSubmit" :loading="loading">保存</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import type { FormInstance } from 'ant-design-vue';
import { getSellerInfo, updateSeller, uploadQualification } from '@/api/seller';

interface SellerInfo {
  id?: number;
  shopName: string;
  description: string;
  shopLogo: string;
  contactName: string;
  contactPhone: string;
  contactEmail: string;
}

const formRef = ref<FormInstance>();
const loading = ref(false);

const formState = reactive<SellerInfo>({
  shopName: '',
  description: '',
  shopLogo: '',
  contactName: '',
  contactPhone: '',
  contactEmail: ''
});

const rules = {
  shopName: [{ required: true, message: '请输入店铺名称' }],
  contactName: [{ required: true, message: '请输入联系人姓名' }],
  contactPhone: [{ required: true, message: '请输入联系电话' }]
};

const fetchSellerInfo = async () => {
  loading.value = true;
  try {
    const data = await getSellerInfo();
    Object.assign(formState, {
      id: data.id,
      shopName: data.shopName,
      description: data.description,
      shopLogo: data.logo,
      contactName: data.contactName,
      contactPhone: data.contactPhone,
      contactEmail: data.contactEmail
    });
  } catch (error: any) {
    message.error('获取店铺信息失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

const beforeLogoUpload = (file: File) => {
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

const uploadLogo = async (options: any) => {
  const { file, onSuccess, onError } = options;
  
  try {
    const url = await uploadQualification('logo', file);
    
    // 更新表单状态
    formState.shopLogo = url;
    
    message.success('Logo上传成功');
    onSuccess(url, file);
  } catch (error: any) {
    message.error('Logo上传失败: ' + error.message);
    onError(error);
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;
    await updateSeller(formState);
    message.success('店铺信息更新成功');
  } catch (error: any) {
    message.error('保存失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchSellerInfo();
});
</script>

<style scoped>
.seller-profile {
  padding: 20px;
}

.shop-logo {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  border-radius: 4px;
  font-size: 24px;
  color: #999;
}

.avatar-container {
  position: relative;
  width: 100px;
  height: 100px;
  margin-bottom: 10px;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  cursor: pointer;
  border-radius: 4px;
}

.avatar-container:hover .avatar-overlay {
  opacity: 1;
}

.upload-text {
  color: white;
  font-size: 14px;
}
</style> 