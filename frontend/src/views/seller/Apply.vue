<template>
  <div class="apply-seller">
    <a-page-header
      title="Merchant application for entry"
      sub-title="Join our platform and start your sales journey"
      @back="goBack"
    />
    
    <a-card>
      <a-steps :current="currentStep" style="margin-bottom: 30px">
        <a-step title="Fill in the basic information" description="Store and contact information" />
        <a-step title="Upload your qualifications" description="Business license and ID card" />
        <a-step title="Submit an application" description="Wait for review" />
      </a-steps>
      
      <!-- 第一步：基本信息 -->
      <div v-if="currentStep === 0">
        <a-card title="Merchant application for entry" class="mt-4">
          <!-- 未登录提示 -->
          <a-alert v-if="!isLoggedIn" type="info" showIcon class="mb-4">
            <template #message>
              You are not currently logged in, please fill in
              the registration information below to create a new account
            </template>
            <template #description>
              Already have an account？<a @click="router.push('/login')">Log in now</a>
            </template>
          </a-alert>

          <a-form
            :model="formState"
            :rules="rules"
            layout="vertical"
            ref="formRef"
          >
            <!-- 用户注册信息，仅未登录用户需要填写 -->
            <div v-if="!isLoggedIn">
              <h3 class="mb-3">Account Information</h3>
              <a-form-item label="username" name="username">
                <a-input v-model:value="formState.username" placeholder="Please enter a username with 4-20 digits" />
              </a-form-item>
              <a-form-item label="password" name="password">
                <a-input-password v-model:value="formState.password" placeholder="Please enter a 6-20 digit password" />
              </a-form-item>
              <a-form-item label="nickname" name="nickname">
                <a-input v-model:value="formState.nickname" placeholder="Please enter a nickname (optional)" />
              </a-form-item>
              <a-divider />
            </div>

            <h3 class="mb-3">Shop Information</h3>
            <a-form-item label="shopName" name="shopName">
              <a-input v-model:value="formState.shopName" placeholder="Please enter a store name" />
            </a-form-item>
            <a-form-item label="description" name="description">
              <a-textarea v-model:value="formState.description" placeholder="Please provide a brief description of the scope of your store" :rows="4" />
            </a-form-item>

            <h3 class="mb-3 mt-4">Contact information</h3>
            <a-form-item label="contactName" name="contactName">
              <a-input v-model:value="formState.contactName" placeholder="Please enter a contact name" />
            </a-form-item>
            <a-form-item label="contactPhone" name="contactPhone">
              <a-input v-model:value="formState.contactPhone" placeholder="Please enter your contact number" />
            </a-form-item>
            <a-form-item label="contactEmail" name="contactEmail">
              <a-input v-model:value="formState.contactEmail" placeholder="Please enter your contact email address" />
            </a-form-item>

            <h3 class="mb-3 mt-4">Business qualifications</h3>
            <a-form-item label="businessLicense" name="businessLicense">
              <a-input v-model:value="formState.businessLicense" placeholder="Please enter your business license number" />
            </a-form-item>
          </a-form>
        </a-card>
        
        <div :style="{ textAlign: 'right' }">
          <a-button type="primary" @click="nextStep">next step</a-button>
        </div>
      </div>
      
      <!-- 第二步：上传资质 -->
      <div v-if="currentStep === 1">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="shopping Logo" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="logoFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'logo')"
              >
                <div v-if="!logoUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">upload Logo</div>
                </div>
              </a-upload>
              <div v-if="logoUrl" style="margin-top: 10px">
                <img :src="logoUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="License" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="licenseFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'businessLicenseImage')"
              >
                <div v-if="!licenseUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">Upload licenses</div>
                </div>
              </a-upload>
              <div v-if="licenseUrl" style="margin-top: 10px">
                <img :src="licenseUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="The front of the ID card" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="idCardFrontFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'idCardFront')"
              >
                <div v-if="!idCardFrontUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">The ID card is being uploaded to the front of the ID card</div>
                </div>
              </a-upload>
              <div v-if="idCardFrontUrl" style="margin-top: 10px">
                <img :src="idCardFrontUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="The back of the ID card" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="idCardBackFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'idCardBack')"
              >
                <div v-if="!idCardBackUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">Upload the back of your ID</div>
                </div>
              </a-upload>
              <div v-if="idCardBackUrl" style="margin-top: 10px">
                <img :src="idCardBackUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>
        
        <a-row :gutter="16" style="margin-top: 24px">
          <a-col :span="24">
            <h3 style="margin-bottom: 16px">Payment QR Codes</h3>
            <p style="margin-bottom: 24px">Upload your payment QR codes so customers can make payments to your account</p>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="WeChat Pay QR Code" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="wechatQrCodeFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'wechat')"
              >
                <div v-if="!wechatQrCodeUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">Upload WeChat Pay QR Code</div>
                </div>
              </a-upload>
              <div v-if="wechatQrCodeUrl" style="margin-top: 10px">
                <img :src="wechatQrCodeUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="Alipay QR Code" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="alipayQrCodeFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'alipay')"
              >
                <div v-if="!alipayQrCodeUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">Upload Alipay QR Code</div>
                </div>
              </a-upload>
              <div v-if="alipayQrCodeUrl" style="margin-top: 10px">
                <img :src="alipayQrCodeUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>
        
        <div :style="{ textAlign: 'right' }">
          <a-button style="margin-right: 10px" @click="prevStep">Previous</a-button>
          <a-button style="margin-right: 10px" type="dashed" @click="skipUpload">Skip uploads</a-button>
          <a-button type="primary" @click="nextStep">next step</a-button>
        </div>
      </div>
      
      <!-- 第三步：提交申请 -->
      <div v-if="currentStep === 2">
        <a-result
          title="The application materials are ready"
          sub-title="Please confirm that the information is correct and submit the application, and our staff will review it within 1-3 working days"
        >
          <template #extra>
            <a-button style="margin-right: 10px" @click="prevStep">Previous</a-button>
            <a-button type="primary" :loading="submitting" @click="submitApplication">Submit an application</a-button>
          </template>
          
          <div class="desc">
            <h3>Preview of application information</h3>
            <a-descriptions bordered>
              <a-descriptions-item label="The name of the store" :span="3">{{ formState.shopName }}</a-descriptions-item>
              <a-descriptions-item label="Description of the store" :span="3">{{ formState.description }}</a-descriptions-item>
              <a-descriptions-item label="Contact name">{{ formState.contactName }}</a-descriptions-item>
              <a-descriptions-item label="Contact number">{{ formState.contactPhone }}</a-descriptions-item>
              <a-descriptions-item label="Contact email">{{ formState.contactEmail }}</a-descriptions-item>
              <a-descriptions-item label="Business license number" :span="3">{{ formState.businessLicense }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </a-result>
      </div>
      
      <!-- 第四步：申请结果 -->
      <div v-if="currentStep === 3">
        <a-result
          status="success"
          title="The application was successfully submitted"
          sub-title="Your business application has been submitted, we will review it as soon as possible, please wait patiently for the review result"
        >
          <template #extra>
            <a-button type="primary" @click="goHome">Return to the top page</a-button>
          </template>
        </a-result>
      </div>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { applySeller, uploadQualification } from '@/api/seller'
import type { SellerApplyRequest } from '@/types/seller'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const formRef = ref<FormInstance>()
const currentStep = ref(0)
const submitting = ref(false)

// 表单初始值
const formState = reactive<SellerApplyRequest>({
  // 用户注册信息，仅未登录用户需要填写
  username: '',
  password: '',
  nickname: '',
  // 店铺信息
  shopName: '',
  description: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  businessLicense: ''
})

// 表单验证规则
const rules = {
  // 用户注册信息
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度在4-20个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  nickname: [
    { required: false, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' }
  ],
  // 店铺信息
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 30, message: '店铺名称长度在2-30个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入店铺描述', trigger: 'blur' },
    { max: 200, message: '店铺描述不能超过200个字符', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { required: true, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  businessLicense: [
    { required: true, message: '请输入营业执照号', trigger: 'blur' }
  ]
}

// 判断用户是否已登录
const userStore = useUserStore()
const isLoggedIn = ref(userStore.isLoggedIn())

// 如果用户已登录，自动填充部分表单字段
if (isLoggedIn.value) {
  formState.contactName = userStore.nickname || userStore.username
  formState.contactPhone = userStore.phone || ''
  formState.contactEmail = userStore.email || ''
}

// 上传文件列表
const logoFileList = ref<any[]>([])
const licenseFileList = ref<any[]>([])
const idCardFrontFileList = ref<any[]>([])
const idCardBackFileList = ref<any[]>([])
const wechatQrCodeFileList = ref<any[]>([])
const alipayQrCodeFileList = ref<any[]>([])

// 上传的文件URL
const logoUrl = ref('')
const licenseUrl = ref('')
const idCardFrontUrl = ref('')
const idCardBackUrl = ref('')
const wechatQrCodeUrl = ref('')
const alipayQrCodeUrl = ref('')

// 添加上传选项接口定义
interface UploadOptions {
  file: File;
  onSuccess: (response: any, file: File) => void;
  onError: (error: any) => void;
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 下一步
const nextStep = async () => {
  if (currentStep.value === 0) {
    try {
      await formRef.value?.validate()
      currentStep.value++
    } catch (error) {
      console.error('表单验证失败:', error)
    }
  } else if (currentStep.value === 1) {
    // 第二步：检查是否有上传必要的图片
    if (!logoUrl.value) {
      message.warning('请上传店铺Logo')
      return
    }
    if (!licenseUrl.value) {
      message.warning('请上传营业执照')
      return
    }
    
    // 支付二维码是推荐但不是必须的
    if (!wechatQrCodeUrl.value && !alipayQrCodeUrl.value) {
      message.info('建议上传至少一种支付二维码，以便顾客可以支付订单。您也可以之后在卖家中心设置。')
    }
    
    currentStep.value++
  } else {
    currentStep.value++
  }
}

// 提交申请
const submitApplication = async () => {
  submitting.value = true
  try {
    // console.log('准备提交商家入驻申请:', formState)
    
    // 必填字段校验
    const requiredFields = ['shopName', 'description', 'contactName', 'contactPhone', 'contactEmail', 'businessLicense']
    
    // 未登录用户需要验证用户名和密码
    if (!isLoggedIn.value) {
      if (!formState.username || !formState.password) {
        message.error('请填写用户名和密码')
        submitting.value = false
        return
      }
    }
    
    // 验证店铺必填信息
    for (const field of requiredFields) {
      if (!formState[field as keyof SellerApplyRequest]) {
        message.error(`请填写${field === 'shopName' ? '店铺名称' : 
                            field === 'description' ? '店铺描述' : 
                            field === 'contactName' ? '联系人姓名' : 
                            field === 'contactPhone' ? '联系电话' : 
                            field === 'contactEmail' ? '联系邮箱' : 
                            '营业执照号码'}`)
        submitting.value = false
        return
      }
    }
    
    // 已登录用户提交申请时不需要包含用户名和密码
    const submitData = { ...formState }
    if (isLoggedIn.value) {
      delete submitData.username
      delete submitData.password
      delete submitData.nickname
    }
    
    const res = await applySeller(submitData)
    // console.log('申请提交结果:', res)
    
    if (res) {
      message.success('商家入驻申请提交成功')
      currentStep.value = 3
    }
  } catch (error: any) {
    // console.error('提交申请时发生错误:', error)
    message.error('申请提交失败: ' + (error.message || '未知错误'))
    // 保持在当前步骤，让用户可以修改信息
  } finally {
    submitting.value = false
  }
}

// 上传前验证
const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  if (!isJpgOrPng) {
    message.error('只能上传JPG/PNG/GIF格式的图片!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 5
  if (!isLt2M) {
    message.error('图片必须小于5MB!')
    return false
  }
  return true
}

// 自定义上传
const customUpload = async (options: UploadOptions, fileType: string) => {
  const { file, onSuccess, onError } = options;
  try {
    // 使用普通资质上传API
    const res = await uploadQualification(fileType, file);

    // 设置对应的URL
    if (fileType === 'logo') {
      logoUrl.value = res;
    } else if (fileType === 'businessLicenseImage') {
      licenseUrl.value = res;
    } else if (fileType === 'idCardFront') {
      idCardFrontUrl.value = res;
    } else if (fileType === 'idCardBack') {
      idCardBackUrl.value = res;
    } else if (fileType === 'wechat') {
      wechatQrCodeUrl.value = res;
    } else if (fileType === 'alipay') {
      alipayQrCodeUrl.value = res;
    }
    message.success('上传成功');
    onSuccess(res, file);
  } catch (err: any) {
    message.error('上传失败: ' + err.message);
    onError(err);
  }
}

// 跳过上传步骤
const skipUpload = () => {
  // 设置默认图片URL以满足验证
  logoUrl.value = logoUrl.value || '/api/default-logo.png';
  licenseUrl.value = licenseUrl.value || '/api/default-license.png';
  idCardFrontUrl.value = idCardFrontUrl.value || '/api/default-id-front.png';
  idCardBackUrl.value = idCardBackUrl.value || '/api/default-id-back.png';
  wechatQrCodeUrl.value = wechatQrCodeUrl.value || '/api/default-wechat.png';
  alipayQrCodeUrl.value = alipayQrCodeUrl.value || '/api/default-alipay.png';
  
  // 直接进入下一步
  currentStep.value++;
  message.info('已跳过上传步骤，您可以之后在卖家中心完善资料');
}
</script>

<style scoped>
.apply-seller {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.desc {
  margin-top: 24px;
}

h3 {
  margin-bottom: 16px;
}
</style> 