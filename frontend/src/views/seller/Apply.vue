<template>
  <div class="apply-seller">
    <a-page-header
      title="商家入驻申请"
      sub-title="加入我们的平台，开始您的销售之旅"
      @back="goBack"
    />
    
    <a-card>
      <a-steps :current="currentStep" style="margin-bottom: 30px">
        <a-step title="填写基本信息" description="店铺和联系信息" />
        <a-step title="上传资质证明" description="营业执照和身份证" />
        <a-step title="提交申请" description="等待审核" />
      </a-steps>
      
      <!-- 第一步：基本信息 -->
      <div v-if="currentStep === 0">
        <a-card title="商家入驻申请" class="mt-4">
          <!-- 未登录提示 -->
          <a-alert v-if="!isLoggedIn" type="info" showIcon class="mb-4">
            <template #message>
              您当前未登录，请填写以下注册信息创建新账户
            </template>
            <template #description>
              已有账户？<a @click="router.push('/login')">立即登录</a>
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
              <h3 class="mb-3">账户信息</h3>
              <a-form-item label="用户名" name="username">
                <a-input v-model:value="formState.username" placeholder="请输入4-20位的用户名" />
              </a-form-item>
              <a-form-item label="密码" name="password">
                <a-input-password v-model:value="formState.password" placeholder="请输入6-20位的密码" />
              </a-form-item>
              <a-form-item label="昵称" name="nickname">
                <a-input v-model:value="formState.nickname" placeholder="请输入昵称（选填）" />
              </a-form-item>
              <a-divider />
            </div>

            <h3 class="mb-3">店铺信息</h3>
            <a-form-item label="店铺名称" name="shopName">
              <a-input v-model:value="formState.shopName" placeholder="请输入店铺名称" />
            </a-form-item>
            <a-form-item label="店铺描述" name="description">
              <a-textarea v-model:value="formState.description" placeholder="请简要描述您的店铺经营范围" :rows="4" />
            </a-form-item>

            <h3 class="mb-3 mt-4">联系人信息</h3>
            <a-form-item label="联系人姓名" name="contactName">
              <a-input v-model:value="formState.contactName" placeholder="请输入联系人姓名" />
            </a-form-item>
            <a-form-item label="联系电话" name="contactPhone">
              <a-input v-model:value="formState.contactPhone" placeholder="请输入联系电话" />
            </a-form-item>
            <a-form-item label="联系邮箱" name="contactEmail">
              <a-input v-model:value="formState.contactEmail" placeholder="请输入联系邮箱" />
            </a-form-item>

            <h3 class="mb-3 mt-4">营业资质</h3>
            <a-form-item label="营业执照号码" name="businessLicense">
              <a-input v-model:value="formState.businessLicense" placeholder="请输入营业执照号码" />
            </a-form-item>
          </a-form>
        </a-card>
        
        <div :style="{ textAlign: 'right' }">
          <a-button type="primary" @click="nextStep">下一步</a-button>
        </div>
      </div>
      
      <!-- 第二步：上传资质 -->
      <div v-if="currentStep === 1">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="店铺Logo" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="logoFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'logo')"
              >
                <div v-if="!logoUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">上传Logo</div>
                </div>
              </a-upload>
              <div v-if="logoUrl" style="margin-top: 10px">
                <img :src="logoUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="营业执照" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="licenseFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'businessLicenseImage')"
              >
                <div v-if="!licenseUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">上传执照</div>
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
            <a-form-item label="身份证正面" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="idCardFrontFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'idCardFront')"
              >
                <div v-if="!idCardFrontUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">上传身份证正面</div>
                </div>
              </a-upload>
              <div v-if="idCardFrontUrl" style="margin-top: 10px">
                <img :src="idCardFrontUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
          
          <a-col :span="12">
            <a-form-item label="身份证背面" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
              <a-upload
                v-model:file-list="idCardBackFileList"
                list-type="picture-card"
                :show-upload-list="true"
                :before-upload="beforeUpload"
                :custom-request="(options: UploadOptions) => customUpload(options, 'idCardBack')"
              >
                <div v-if="!idCardBackUrl">
                  <plus-outlined />
                  <div style="margin-top: 8px">上传身份证背面</div>
                </div>
              </a-upload>
              <div v-if="idCardBackUrl" style="margin-top: 10px">
                <img :src="idCardBackUrl" style="max-width: 200px" />
              </div>
            </a-form-item>
          </a-col>
        </a-row>
        
        <div :style="{ textAlign: 'right' }">
          <a-button style="margin-right: 10px" @click="prevStep">上一步</a-button>
          <a-button style="margin-right: 10px" type="dashed" @click="skipUpload">跳过上传</a-button>
          <a-button type="primary" @click="nextStep">下一步</a-button>
        </div>
      </div>
      
      <!-- 第三步：提交申请 -->
      <div v-if="currentStep === 2">
        <a-result
          title="申请资料已准备就绪"
          sub-title="请确认信息无误后提交申请，我们的工作人员将在1-3个工作日内进行审核"
        >
          <template #extra>
            <a-button style="margin-right: 10px" @click="prevStep">上一步</a-button>
            <a-button type="primary" :loading="submitting" @click="submitApplication">提交申请</a-button>
          </template>
          
          <div class="desc">
            <h3>申请信息预览</h3>
            <a-descriptions bordered>
              <a-descriptions-item label="店铺名称" :span="3">{{ formState.shopName }}</a-descriptions-item>
              <a-descriptions-item label="店铺描述" :span="3">{{ formState.description }}</a-descriptions-item>
              <a-descriptions-item label="联系人姓名">{{ formState.contactName }}</a-descriptions-item>
              <a-descriptions-item label="联系电话">{{ formState.contactPhone }}</a-descriptions-item>
              <a-descriptions-item label="联系邮箱">{{ formState.contactEmail }}</a-descriptions-item>
              <a-descriptions-item label="营业执照号" :span="3">{{ formState.businessLicense }}</a-descriptions-item>
            </a-descriptions>
          </div>
        </a-result>
      </div>
      
      <!-- 第四步：申请结果 -->
      <div v-if="currentStep === 3">
        <a-result
          status="success"
          title="申请提交成功"
          sub-title="您的商家入驻申请已提交，我们将尽快审核，请耐心等待审核结果"
        >
          <template #extra>
            <a-button type="primary" @click="goHome">返回首页</a-button>
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

// 上传的文件URL
const logoUrl = ref('')
const licenseUrl = ref('')
const idCardFrontUrl = ref('')
const idCardBackUrl = ref('')

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
    currentStep.value++
  } else {
    currentStep.value++
  }
}

// 提交申请
const submitApplication = async () => {
  submitting.value = true
  try {
    console.log('准备提交商家入驻申请:', formState)
    
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
    console.log('申请提交结果:', res)
    
    if (res) {
      message.success('商家入驻申请提交成功')
      currentStep.value = 3
    }
  } catch (error: any) {
    console.error('提交申请时发生错误:', error)
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