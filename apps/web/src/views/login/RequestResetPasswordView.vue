<template>
  <div class="container">

    <el-form :model="form" :rules="rules" ref="ruleFormRef" label-width="auto" class="form" :label-position="top" status-icon>

      <!-- Wintec Logo -->
      <div style="display: flex; justify-content: center;">
        <img src="@/assets/Logo/Industry Internship System Logo_Orange and Blue.svg" class="industry-internship-system-logo" />
      </div>

      <!-- Instruction text for password reset -->
      <el-form-item class="mt-1">
        <el-text class="email-reset-text">Enter the email you used to create your account so we can
          send you instructions on how to reset your password.</el-text>
      </el-form-item>

      <!-- Email input field -->
      <el-form-item label="Email" prop="email">
        <el-input v-model="form.email" :prefix-icon="Message" clearable placeholder="id@student.wintec.ac.nz" />
      </el-form-item>

      <!-- Button to send password reset request -->
      <el-form-item>
        <el-button type="primary" :loading="isSubmitting" :disabled="isSubmitting"
          @click="handleSendButton(ruleFormRef)">Send</el-button>
      </el-form-item>

      <!-- Button to navigate back to login -->
      <el-form-item>
        <el-button style="color: #FB9333;" @click="handleNavButton">Back to Login</el-button>
      </el-form-item>

    </el-form>

  </div>


</template>

<script lang="ts" setup>
import { ref, reactive, inject } from 'vue'
import { ElMessage, type FormProps, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { Message } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import type { AxiosInstance } from 'axios'

// Inject Axios instance for making HTTP requests
const axios: AxiosInstance = inject('$axios') as AxiosInstance
// Use Vue Router for navigation
const router = useRouter()
// Access the authentication store
const authStore = useAuthStore()
// Emit loading state changes
const emit = defineEmits(['loading'])
// Reference for the form instance
const ruleFormRef = ref<FormInstance>()
const isSubmitting = ref(false)

// Define the form data structure
interface RuleForm {
  email: string,
}

// Reactive form data
const form = reactive<RuleForm>({
  email: '',
})

// Validation rules for the form
const rules = reactive<FormRules<RuleForm>>({
  email: [
    { required: true, message: 'This field is required', trigger: 'blur' },
    { type: 'email', message: 'Invalid email address', trigger: 'blur' }
  ]
})

// Position of the label in the form
const top = ref<FormProps['labelPosition']>('top')

// Function to trigger loading state
const triggerLoading = (value: boolean) => {
  emit('loading', value)
}

// Function to handle the send button click event
const handleSendButton = async (formEl: FormInstance | undefined) => {
  if (!formEl || isSubmitting.value) return

  isSubmitting.value = true
  let loadingStarted = false

  try {
    let valid = false
    try {
      valid = await formEl.validate()
    } catch {
      valid = false
    }

    if (!valid) return

    triggerLoading(true)
    loadingStarted = true

    try {
      const res = await axios({
        url: '/forgotPassRequest',
        method: 'post',
        data: {
          email: form.email,
        },
        headers: {
          'Content-Type': 'application/json'
        }
      })

      authStore.setServerRef(res.data.server_ref, form.email)
      ElMessage.success(res.data.description)
      router.push('/resetPassVerification')
    } catch (rawError: unknown) {
      const error = rawError as { response?: { data?: { error?: string } } }
      const message = error.response?.data?.error

      if (message) {
        ElMessage.error(message)
      } else {
        ElMessage.error('Network error or server not responding. Please try again later.')
      }
    }
  } finally {
    if (loadingStarted) {
      triggerLoading(false)
    }
    isSubmitting.value = false
  }
}

// Function to handle navigation back to the login page
const handleNavButton = () => {
  // Navigate to the login page
  router.push('/login')
}

</script>


<style scoped>
/* .container {
  display: flex;
  height: 100%;
  display: flex;
  background: linear-gradient(to bottom, #1E5192, #FFFFFF);
  align-items: center;
  justify-content: center;
} */

.mt-1 {
  margin-top: 1rem;
}

.mt-2 {
  margin-top: 2rem;
}

.el-button {
  width: 100%;
}

.email-reset-text {
  color: #6C6B6B; 
  font-weight: bold;
}


/* Phone */
@media screen and (max-width: 768px) {
  .form {
    width: 280px;
    height: 460px;
    border-radius: 24px;
    padding: 1rem;
    background-color: rgba(250, 250, 250, 0.8);
  }


  .industry-internship-system-logo {
    height: 120px;
  }

  .email-reset-text {
    font-size: 12px;
  }
}

/* Tablet */
@media screen and (max-width: 992px) and (min-width: 768px) {
  .form {
    width: 480px;
    height: 500px;
    border-radius: 24px;
    padding: 2rem;
    background-color: rgba(250, 250, 250, 0.8);
  }


  .industry-internship-system-logo {
    height: 150px;
  }

  .email-reset-text {
    font-size: 16px;
  }
}

/* Computer */
@media screen and (min-width: 992px) {
  .form {
    width: 480px;
    height: 500px;
    border-radius: 24px;
    padding: 2rem;
    background-color: rgba(250, 250, 250, 0.8);
  }

  .industry-internship-system-logo {
    height: 150px;
  }

  .email-reset-text {
    font-size: 16px;
  }
}
</style>
