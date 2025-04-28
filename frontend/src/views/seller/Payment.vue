<template>
  <div class="payment-settings">
    <a-page-header
      title="Payment Settings"
      sub-title="Manage your payment QR codes"
    />
    
    <a-card>
      <a-alert
        v-if="!seller.wechatQrCode && !seller.alipayQrCode"
        type="info"
        showIcon
        message="Payment QR Codes Not Set"
        description="Upload your WeChat and Alipay QR codes to receive payments from customers."
        style="margin-bottom: 24px"
      />
      
      <a-row :gutter="24">
        <a-col :span="12">
          <a-card title="WeChat Pay QR Code" :bordered="false">
            <template #extra>
              <a-tooltip title="Customers will scan this QR code to pay with WeChat Pay">
                <info-circle-outlined style="color: #1890ff" />
              </a-tooltip>
            </template>
            
            <div class="qr-code-container">
              <div v-if="wechatQrCodeUrl" class="qr-code-image">
                <img :src="wechatQrCodeUrl" alt="WeChat Pay QR Code" />
                <div class="qr-code-overlay">
                  <a-button type="primary" @click="showUploadModal('wechat')">Replace</a-button>
                </div>
              </div>
              <div v-else class="qr-code-placeholder">
                <a-empty description="No QR code uploaded">
                  <template #description>
                    <span>No WeChat Pay QR code uploaded</span>
                  </template>
                  <a-button type="primary" @click="showUploadModal('wechat')">Upload Now</a-button>
                </a-empty>
              </div>
            </div>
          </a-card>
        </a-col>
        
        <a-col :span="12">
          <a-card title="Alipay QR Code" :bordered="false">
            <template #extra>
              <a-tooltip title="Customers will scan this QR code to pay with Alipay">
                <info-circle-outlined style="color: #1890ff" />
              </a-tooltip>
            </template>
            
            <div class="qr-code-container">
              <div v-if="alipayQrCodeUrl" class="qr-code-image">
                <img :src="alipayQrCodeUrl" alt="Alipay QR Code" />
                <div class="qr-code-overlay">
                  <a-button type="primary" @click="showUploadModal('alipay')">Replace</a-button>
                </div>
              </div>
              <div v-else class="qr-code-placeholder">
                <a-empty description="No QR code uploaded">
                  <template #description>
                    <span>No Alipay QR code uploaded</span>
                  </template>
                  <a-button type="primary" @click="showUploadModal('alipay')">Upload Now</a-button>
                </a-empty>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
      
      <a-modal
        v-model:visible="uploadModalVisible"
        :title="`Upload ${currentPayType === 'wechat' ? 'WeChat Pay' : 'Alipay'} QR Code`"
        @ok="handleModalOk"
        :confirmLoading="uploading"
        @cancel="handleModalCancel"
      >
        <a-upload
          list-type="picture-card"
          :file-list="fileList"
          :before-upload="beforeUpload"
          @preview="handlePreview"
          @change="handleChange"
        >
          <div v-if="fileList.length < 1">
            <plus-outlined />
            <div style="margin-top: 8px">Upload</div>
          </div>
        </a-upload>
        
        <a-modal
          v-model:visible="previewVisible"
          :title="previewTitle"
          :footer="null"
        >
          <img alt="QR Code Preview" style="width: 100%" :src="previewImage" />
        </a-modal>
        
        <template #footer>
          <a-button key="back" @click="handleModalCancel">Cancel</a-button>
          <a-button
            key="submit"
            type="primary"
            :loading="uploading"
            :disabled="fileList.length === 0"
            @click="uploadQrCode"
          >
            Upload
          </a-button>
        </template>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadFile } from 'ant-design-vue'
import { PlusOutlined, InfoCircleOutlined } from '@ant-design/icons-vue'
import { getSellerInfo, uploadPaymentQrCode } from '@/api/seller'

const seller = ref<any>({})
const wechatQrCodeUrl = computed(() => {
  let url = '';
  
  if (seller.value && seller.value.data && seller.value.data.wechatQrCode) {
    url = seller.value.data.wechatQrCode;
  } else if (seller.value && seller.value.wechatQrCode) {
    url = seller.value.wechatQrCode;
  }
  
  // If URL is empty, return empty string
  if (!url) return '';
  
  // Fix path if needed
  if (url.startsWith('/api/')) {
    url = url.replace('/api', '');
  }
  
  // Add proper prefix for development mode
  const basePath = import.meta.env.DEV ? '/api' : '';
  return `${basePath}${url}`;
})
const alipayQrCodeUrl = computed(() => {
  let url = '';
  
  if (seller.value && seller.value.data && seller.value.data.alipayQrCode) {
    url = seller.value.data.alipayQrCode;
  } else if (seller.value && seller.value.alipayQrCode) {
    url = seller.value.alipayQrCode;
  }
  
  // If URL is empty, return empty string
  if (!url) return '';
  
  // Fix path if needed
  if (url.startsWith('/api/')) {
    url = url.replace('/api', '');
  }
  
  // Add proper prefix for development mode
  const basePath = import.meta.env.DEV ? '/api' : '';
  return `${basePath}${url}`;
})

const uploadModalVisible = ref(false)
const previewVisible = ref(false)
const previewImage = ref('')
const previewTitle = ref('')
const fileList = ref<UploadFile[]>([])
const uploading = ref(false)
const currentPayType = ref<'wechat' | 'alipay'>('wechat')

// Fetch seller information on component mount
onMounted(async () => {
  try {
    const result = await getSellerInfo()
    seller.value = result
    console.log('Seller info loaded:', seller.value)
  } catch (error: any) {
    message.error(`Failed to load seller information: ${error.message}`)
  }
})

// Show upload modal
const showUploadModal = (payType: 'wechat' | 'alipay') => {
  currentPayType.value = payType
  fileList.value = []
  uploadModalVisible.value = true
}

// Handle modal OK button
const handleModalOk = () => {
  if (fileList.value.length === 0) {
    message.warning('Please select a QR code image to upload')
    return
  }
  uploadQrCode()
}

// Handle modal cancel
const handleModalCancel = () => {
  uploadModalVisible.value = false
  fileList.value = []
}

// Handle file preview
const handlePreview = async (file: UploadFile) => {
  if (file.url) {
    previewImage.value = file.url
    previewVisible.value = true
    previewTitle.value = file.name || file.url.substring(file.url.lastIndexOf('/') + 1)
  }
}

// Validate file before upload
const beforeUpload = (file: File) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  if (!isImage) {
    message.error('You can only upload JPG/PNG/GIF file!')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('Image must be smaller than 5MB!')
    return false
  }
  
  return false // Return false to prevent auto upload
}

// Handle file list changes
const handleChange = ({ fileList: newFileList }: UploadChangeParam) => {
  fileList.value = newFileList
}

// Upload QR code
const uploadQrCode = async () => {
  if (fileList.value.length === 0) {
    message.warning('Please select a QR code image to upload')
    return
  }
  
  const file = fileList.value[0]
  if (!file.originFileObj) {
    message.error('File object not found')
    return
  }
  
  // Check if seller ID is available
  let sellerId = null
  // Check if the seller data is nested in a 'data' property (API response format)
  if (seller.value && seller.value.data && seller.value.data.id) {
    sellerId = seller.value.data.id
    console.log('Using seller ID from nested data object:', sellerId)
  } else if (seller.value && seller.value.id) {
    sellerId = seller.value.id
    console.log('Using direct seller ID:', sellerId)
  }
  
  if (!sellerId) {
    console.error('Seller ID is undefined. Seller object:', seller.value)
    message.error('Unable to upload: Seller ID not found. The system will use your user ID instead.')
  }
  
  uploading.value = true
  
  try {
    console.log(`Uploading ${currentPayType.value} QR code`)
    // We don't pass seller ID - the backend will use the authenticated user ID
    const result = await uploadPaymentQrCode(currentPayType.value, file.originFileObj)
    
    // Update the local URL
    if (currentPayType.value === 'wechat') {
      if (seller.value.data) {
        seller.value.data.wechatQrCode = result
      } else {
        seller.value.wechatQrCode = result
      }
    } else {
      if (seller.value.data) {
        seller.value.data.alipayQrCode = result
      } else {
        seller.value.alipayQrCode = result
      }
    }
    
    message.success(`${currentPayType.value === 'wechat' ? 'WeChat Pay' : 'Alipay'} QR code uploaded successfully`)
    uploadModalVisible.value = false
    fileList.value = []
  } catch (error: any) {
    message.error(`Upload failed: ${error.message}`)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.payment-settings {
  max-width: 1200px;
  margin: 0 auto;
}

.qr-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 250px;
}

.qr-code-image {
  position: relative;
  max-width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.qr-code-image img {
  max-width: 100%;
  max-height: 220px;
  object-fit: contain;
}

.qr-code-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.qr-code-image:hover .qr-code-overlay {
  opacity: 1;
}

.qr-code-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 