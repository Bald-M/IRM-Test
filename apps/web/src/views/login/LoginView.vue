<template>

  <div class="container">
    <!-- El Form component for user login -->
    <el-form :model="form" :rules="rules" ref="ruleFormRef" label-width="auto" class="form" :label-position="top"
      status-icon>

      <!-- Wintec Logo displayed at the center -->
      <div style="display: flex; justify-content: center;">
        <img src="@/assets/Logo/Industry Internship System Logo_Orange and Blue.svg" class="industry-internship-system-logo" />
      </div>

      <!-- Form item for email input -->
      <el-form-item label="Email" class="mt-2" prop="email">
        <!-- Bind email input to form model -->
        <!-- Allow clearing of the input field -->
        <el-input v-model="form.email" clearable placeholder="mail@example.com" data-test="username-field" />
      </el-form-item>

      <!-- Form item for password input -->
      <el-form-item label="Password" prop="password">
        <!-- Bind password input to form model -->
        <!-- Mask the input as password -->
        <!-- Allow clearing of the input field -->
        <!-- Show password toggle option -->
        <el-input v-model="form.password" type="password" clearable placeholder="******" show-password data-test="password-field" />
      </el-form-item>

      <!-- Link for forgotten password, aligned to the right -->
      <div style="display: flex; flex-flow: row-reverse;">
        <el-text size="small">
          <RouterLink to="/requestResetPassword" style="color: #6B7280;">Forgot password?</RouterLink>
        </el-text>
      </div>

      <!-- Button for logging in -->
      <el-form-item class="mt-1">
        <el-button type="primary" :loading="isSubmitting" :disabled="isSubmitting"
          @click="handleLogin(ruleFormRef)">Log In</el-button>
      </el-form-item>

      <!-- Link to create a new account, centered -->
      <div style="text-align: center;">
        <el-text size="small">No account yet? <RouterLink to="/registration">Create account
          </RouterLink></el-text>
      </div>

    </el-form>

  </div>

</template>


<script lang="ts" setup>
import { reactive, ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { type FormProps, type FormRules, type FormInstance, ElMessage } from 'element-plus'
import type { Router } from 'vue-router'
import type { AxiosInstance } from 'axios'

// Initialize the authentication store
const authStore = useAuthStore()
// Initialize the router for navigation
const router: Router = useRouter() as Router
// Inject the Axios instance for making HTTP requests
const axios: AxiosInstance = inject('$axios') as AxiosInstance
// Create a reference for the form instance
const ruleFormRef = ref<FormInstance>()
const isSubmitting = ref(false)
// Define emits for loading state
const emit = defineEmits(['loading'])

// Define an interface for the form data structure
interface RuleForm {
  email: string,
  password: string,
  role: string
}

// Create a reactive object for the form data
const form = reactive<RuleForm>({
  email: '',
  password: '',
  role: 'Student'
})

// Define validation rules for the form fields
const rules = reactive<FormRules<RuleForm>>({
  email: [
    { required: true, message: 'This field is required', trigger: 'blur' },
    { type: 'email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'This field is requried', trigger: 'blur' }
  ]
})

// Define the label position for the form
const top = ref<FormProps['labelPosition']>('top')

// Function to emit loading state changes
const triggerLoading = (value: boolean) => {
  emit('loading', value)
}

// Function to handle the login process
const handleLogin = async (formEl: FormInstance | undefined) => {
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
        url: '/login',
        method: 'post',
        data: {
          email: form.email,
          password: form.password
        },
        headers: {
          'Content-Type': 'application/json'
        }
      })

      authStore.setAuthData(res.data.auth_key, res.data.profile_data.name, res.data.profile_data.email, res.data.profile_data.app_uid, res.data.profile_data.user_type)
      ElMessage.success('Successfully Login')

      switch (res.data.profile_data.user_type) {
        case 'Student':
          router.push('/studentLayout')
          break
        case 'Industry':
          router.push('/clientLayout')
          break
        case 'Admin':
          router.push('/adminLayout')
          break
        default:
          router.push('/')
          break
      }
    } catch (rawError: unknown) {
      const error = rawError as {
        response?: { data?: { error?: string, server_ref?: string } }
      }
      const responseData = error.response?.data

      if (responseData?.error) {
        ElMessage.error(responseData.error)

        if (
          responseData.error === 'OTP sent, Email verification required'
          && responseData.server_ref
        ) {
          authStore.setServerRef(responseData.server_ref, form.email)
          router.push('/emailVerification')
        }
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

</script>

<style scoped>
a {
  text-decoration: none;
  color: #FB9333;
}

button {
  width: 100%;
}

.el-radio-group {
  width: 100%;
}

.el-radio {
  display: flex;
  justify-content: center;
}

.mt-1 {
  margin-top: 1rem;
}

.mt-2 {
  margin-top: 2rem;
}

.mt-3 {
  margin-top: 3rem;
}

/* .container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
} */

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

  .radio {
    flex: 0 0 43%;
    border-radius: 9px;
    background-color: white;
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

  .radio {
    flex: 0 0 45%;
    border-radius: 9px;
    background-color: white;
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

  .radio {
    flex: 0 0 45%;
    border-radius: 9px;
    background-color: white;
  }
}
</style>
