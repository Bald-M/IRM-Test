<template>

  <div class="container">


    <!-- Form component from Element Plus with validation rules and status icons. It uses `model` to bind form data and `rules` for validating the input fields. The `label-width` is set to "auto" so that labels adjust based on content. -->
    <el-form :model="form" :rules="rules" ref="ruleFormRef" class="form" label-width="auto" :label-position="top"
      status-icon>

      <!-- Wintec Logo section centered horizontally using Flexbox -->
      <div style="display: flex; justify-content: center;">
        <img src="@/assets/Logo/Industry Internship System Logo_Orange and Blue.svg"
          class="industry-internship-system-logo" />
      </div>

      <!-- Form Header: Displayed text asking the user to choose a new password -->
      <el-form-item class="mt-1">
        <el-text class="text-header">Choose a new password for your account</el-text>
      </el-form-item>

      <!-- New Password input field: Bound to the `form.password` model and validated by the `prop="password"` rule. The input field is of type "password", with options to toggle visibility (`show-password`) and clear the content (`clearable`). -->
      <el-form-item label="New Password" prop="password">
        <el-input v-model="form.password" type="password" show-password clearable></el-input>
      </el-form-item>

      <!-- Confirm Password input field: Bound to the `form.confirmedPassword` model and validated by the `prop="confirmedPassword"` rule. This input field is also a password field with similar options as above for visibility and clearing content. -->
      <el-form-item label="Confirm Password" prop="confirmedPassword">
        <el-input v-model="form.confirmedPassword" type="password" show-password clearable></el-input>
      </el-form-item>

      <!-- Reset Password Button -->
      <el-form-item>
        <el-button type="primary" :loading="isSubmitting" :disabled="isSubmitting"
          @click="resetPassword(ruleFormRef)">Reset Password</el-button>
      </el-form-item>

      <!-- Button to navigate back to the login page -->
      <el-form-item>
        <el-button style="color: #FB9333;" @click="backToLogin">Back to Login</el-button>
      </el-form-item>

    </el-form>

  </div>

</template>

<script lang="ts" setup>
import { ref, reactive, inject } from 'vue'
import { ElMessage, type FormProps, type FormInstance, type FormRules, type FormItemRule } from 'element-plus'
import type { AxiosInstance } from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

// Injecting the Vue Router instance for programmatic navigation
const router = useRouter()
// Injecting Axios for making HTTP requests
const axios: AxiosInstance = inject('$axios') as AxiosInstance
// Using Pinia store for authentication-related data
const authStore = useAuthStore()
// Setting the form label position to "top"
const top = ref<FormProps['labelPosition']>('top')
// Reference to the form instance for validation and other form operations
const ruleFormRef = ref<FormInstance>()
const isSubmitting = ref(false)
// Emit function to send loading state to the parent component
const emit = defineEmits(['loading'])
// Fetching email and server reference from the authentication store
const email = authStore.email
const server_ref = authStore.server_ref

// Defining the interface for the form data
interface RuleForm {
  password: string,
  confirmedPassword: string,
}

// Reactive object to hold form input values
const form = reactive<RuleForm>({
  password: '',
  confirmedPassword: ''
})

// Function to trigger the loading state by emitting an event to the parent component
const triggerLoading = (value: boolean) => {
  emit('loading', value)
}

// Custom password validation function
// Ensures that the password meets certain criteria like length and inclusion of upper/lower case letters and numbers
const passwordValidation: FormItemRule['validator'] = (_rule, value, callback) => {
  const password = typeof value === 'string' ? value : ''
  if (!password) {
    return callback(new Error('This field is required'))
  }

  // Regular expressions to check for different character types
  const hasUpperCase = /[A-Z]/.test(password);
  const hasLowerCase = /[a-z]/.test(password);
  const hasNumber = /\d/.test(password);

  // Validating password length and character composition
  if (password.length < 8) {
    callback(new Error('Password must be at least 8 characters long.'))
  } else if (!hasUpperCase || !hasLowerCase || !hasNumber) {
    callback(new Error('Password must include uppercase, lowercase letters, and numbers.'))
  } else {
    callback()
  }
}

// Custom validation for confirming that the two password fields match
const confirmPasswordValidation: FormItemRule['validator'] = (_rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('Passwords do not match.'))
  } else {
    callback()
  }
}

// Form validation rules for the password fields
const rules = reactive<FormRules<RuleForm>>({
  password: [
    { required: true, message: 'This field is requried', trigger: 'blur' },
    { validator: passwordValidation, trigger: 'blur' }
  ],
  confirmedPassword: [
    { required: true, message: 'This field is required', trigger: 'blur' },
    { validator: confirmPasswordValidation, trigger: 'blur' }
  ]
})

// Function to handle password reset process
// It validates the form, sends a request to the server, and handles the response
const resetPassword = async (formEl: FormInstance | undefined) => {
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
        url: '/forgotPassChange',
        method: 'post',
        data: {
          email,
          server_ref,
          otp: localStorage.getItem('otp'),
          password: form.password
        },
        headers: {
          'Content-Type': 'application/json'
        }
      })

      ElMessage.success(res.data.description)
      router.push('/login')
      authStore.clearAuthData()
      localStorage.removeItem('otp')
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
// Function to navigate back to the login page when the "Back to Login" button is clicked
const backToLogin = () => {
  router.push('/login')
}

</script>

<style scoped>
/* .container {
  height: 100%;
  display: flex;
  background: linear-gradient(to bottom, #1E5192, #FFFFFF);
  align-items: center;
  justify-content: center;
} */

.mt-1 {
  margin-top: 1rem;
}

.el-button {
  width: 100%;
}


.text-header {
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

  .radio {
    flex: 0 0 43%;
    border-radius: 9px;
    background-color: white;
  }

  .text-header {
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

  .radio {
    flex: 0 0 45%;
    border-radius: 9px;
    background-color: white;
  }

  .text-header {
    font-size: 14px;
  }
}


/* Computer */
@media screen and (min-width: 992px) {
  .form {
    width: 480px;
    height: 520px;
    border-radius: 24px;
    padding: 2rem;
    background-color: rgba(250, 250, 250, 0.8);
  }

  .industry-internship-system-logo {
    height: 150px;
  }

  .radio {
    flex: 0 0 45%;
    border-radius: 1.5rem;
    background-color: white;
  }

  .text-header {
    font-size: 14px;
  }
}
</style>
