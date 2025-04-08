<template>
  <div class="seller-profile">
    <a-card title="Store information">
      <a-form
        :model="formState"
        :rules="rules"
        ref="formRef"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="shopping Logo">
          <div class="avatar-container">
            <img 
              v-if="displayLogo" 
              :src="displayLogo" 
              alt="shop-logo" 
              class="shop-logo"
              @error="handleImageError"
              ref="logoImg"
            />
            <div class="avatar-placeholder" v-else>
              <upload-outlined />
            </div>
            <div class="avatar-overlay">
              <a-upload 
                :show-upload-list="false"
                :before-upload="beforeLogoUpload"
                :customRequest="uploadLogo"
              >
                <div class="upload-text">change Logo</div>
              </a-upload>
            </div>
          </div>
          <div class="preview-hint" v-if="localLogoPreview">
            <small>预览图片将在保存后上传到服务器</small>
          </div>
        </a-form-item>
        
        <a-form-item label="shopName" name="shopName">
          <a-input v-model:value="formState.shopName" placeholder="Please enter the store name" />
        </a-form-item>
        
        <a-form-item label="shopName" name="shopName">
          <a-textarea v-model:value="formState.description" placeholder="Please enter a description of your store" :rows="4" />
        </a-form-item>
        
        <a-form-item label="contactName" name="contactName">
          <a-input v-model:value="formState.contactName" placeholder="Please enter a contact name" />
        </a-form-item>
        
        <a-form-item label="contactPhone" name="contactPhone">
          <a-input v-model:value="formState.contactPhone" placeholder="Please enter your phone number" />
        </a-form-item>
        
        <a-form-item label="contactEmail" name="contactEmail">
          <a-input v-model:value="formState.contactEmail" placeholder="Please enter your contact email address" />
        </a-form-item>
        
        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-button type="primary" @click="handleSubmit" :loading="loading">save</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import { UploadOutlined } from '@ant-design/icons-vue';
import type { FormInstance } from 'ant-design-vue';
import { getSellerInfo, updateSeller, uploadQualification } from '@/api/seller';
import { getImageUrl } from '@/utils/imageUtil';

interface SellerInfo {
  id?: number;
  shopName: string;
  description: string;
  shopLogo?: string;  // 图片URL
  logo?: string;      // 后端可能用logo字段
  shopDesc?: string;  // 后端使用的描述字段
  contactName: string;
  contactPhone: string;
  contactEmail: string;
  logoFile?: File;
}

const formRef = ref<FormInstance>();
const loading = ref(false);
const localLogoPreview = ref<string>('');

const formState = reactive<SellerInfo>({
  shopName: '',
  description: '',
  shopLogo: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  logoFile: undefined
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
    console.log('获取到的商家信息:', data);
    
    // 处理数据，考虑不同的字段名
    formState.shopName = data.shopName || '';
    formState.description = data.shopDesc || data.description || '';
    formState.shopLogo = data.shopLogo || data.logo || '';
    formState.contactName = data.contactName || '';
    formState.contactPhone = data.contactPhone || '';
    formState.contactEmail = data.contactEmail || '';
    
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

const handleImageError = (e: Event) => {
  console.error('图片加载失败:', (e.target as HTMLImageElement).src);
  if (localLogoPreview.value && (e.target as HTMLImageElement).src.startsWith('blob:')) {
    return;
  }
  
  if (formState.shopLogo) {
    const imgElement = e.target as HTMLImageElement;
    const currentSrc = imgElement.src;
    if (currentSrc.includes('?')) {
      const newSrc = currentSrc.split('?')[0];
      console.log('尝试不带时间戳加载图片:', newSrc);
      imgElement.src = newSrc;
    }
  }
};

const displayLogo = computed(() => {
  if (localLogoPreview.value) {
    return localLogoPreview.value;
  }
  
  if (!formState.shopLogo) return '';
  
  if (typeof formState.shopLogo === 'object' && formState.shopLogo !== null) {
    if ('data' in formState.shopLogo) {
      return "http://localhost:8080" + (formState.shopLogo as any).data;
    }
    return '';
  }
  
  return "http://localhost:8080" + formState.shopLogo;
});

const uploadLogo = async (options: any) => {
  const { file, onSuccess, onError } = options;
  
  try {
    console.log('开始预览Logo文件:', file.name);
    
    const reader = new FileReader();
    reader.onload = (e) => {
      localLogoPreview.value = e.target?.result as string;
      console.log('创建本地预览成功');
    };
    reader.readAsDataURL(file);
    
    message.success('Logo预览成功，保存信息后将上传到服务器');
    
    formState.logoFile = file;
    onSuccess('临时预览成功');
  } catch (error: any) {
    console.error('Logo预览失败:', error);
    message.error('Logo预览失败: ' + error.message);
    onError(error);
  }
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    loading.value = true;
    
    if (formState.logoFile) {
      console.log('开始上传Logo文件:', formState.logoFile.name);
      const response = await uploadQualification('logo', formState.logoFile);
      console.log('Logo上传响应:', response);
      
      let logoUrl = '';
      if (typeof response === 'string') {
        logoUrl = response;
      } else if (response && typeof response === 'object') {
        if ('data' in response) {
          logoUrl = (response as any).data;
        }
      }
      
      console.log('提取的Logo URL:', logoUrl);
      if (!logoUrl) {
        throw new Error('无法获取上传的Logo URL');
      }
      
      formState.shopLogo = logoUrl;
      
      localLogoPreview.value = '';
      delete formState.logoFile;
    }
    
    const updateData: Record<string, any> = {
      shopName: formState.shopName,
      shopDesc: formState.description,
      contactName: formState.contactName,
      contactPhone: formState.contactPhone,
      contactEmail: formState.contactEmail
    };
    
    if (formState.shopLogo) {
      if (typeof formState.shopLogo === 'object' && formState.shopLogo !== null) {
        console.log('Logo is an object, extracting URL', formState.shopLogo);
        if ('data' in formState.shopLogo) {
          updateData.shopLogo = (formState.shopLogo as any).data;
        }
      } else {
        updateData.shopLogo = formState.shopLogo;
      }
    }
    
    console.log('发送更新数据:', updateData);
    await updateSeller(updateData);
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

.preview-hint {
  margin-top: 5px;
  color: #888;
  font-size: 12px;
}
</style> 