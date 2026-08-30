<template>

  <div class="container">
    <el-form ref="ruleFormRef" class="form" status-icon>

      <!-- Wintec Logo -->
      <div style="display: flex; justify-content: center;">
        <img src="@/assets/Logo/Industry Internship System Logo_Orange and Blue.svg"
          class="industry-internship-system-logo" />
      </div>

      <!-- Header for the email verification prompt -->
      <div style="display: flex; justify-content: center;" class="mt-1">
        <el-text class="text-header">Please check your email</el-text>
      </div>

      <!-- Sub-header displaying the email address -->
      <div style="display: flex; justify-content: center;">
        <el-text class="text-sub-header">We've sent a code to {{ email }}</el-text>
      </div>

      <!-- Input fields for the OTP code -->
      <div class="code-container">
        <input v-model="codes[index]" v-for="(code, index) in codes" :key="index" maxlength="1" class="code-input"
          @input="handleInput(index, $event)" @keydown="handleKeydown(index, $event)" ref="inputRef" />
      </div>

      <!-- Button to verify the entered code -->
      <el-form-item class="mt-1">
        <el-button type="primary" :loading="isVerifying" :disabled="isVerifying || isResending"
          @click="handleVerify">Verify</el-button>
      </el-form-item>

      <!-- Button to navigate back to the login page -->
      <el-form-item class="mt-1">
        <el-button style="color: #FB9333;" @click="handleNavButton">Back to Login</el-button>
      </el-form-item>

      <!-- Option to resend the verification email -->
      <div style="text-align: center;">
        <el-text>Didn't receive an email?&nbsp;</el-text>
        <el-button link class="resend-button" :loading="isResending" :disabled="isVerifying || isResending"
          @click="handleResend">Resend</el-button>
      </div>

    </el-form>

  </div>

</template>


<script lang="ts" setup>
import { ref, nextTick, onMounted, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import type { Router } from 'vue-router'
import type { AxiosInstance } from 'axios'

// Array to hold individual OTP digits
const codes = ref(['', '', '', '', '', ''])
// References to input elements
const inputRef = ref<(HTMLInputElement | null)[]>([])
const isVerifying = ref(false)
const isResending = ref(false)
// Access the authentication store
const authStore = useAuthStore()
// Inject Axios instance for making HTTP requests
const axios: AxiosInstance = inject('$axios') as AxiosInstance
// Use Vue Router for navigation
const router: Router = useRouter()
// Emit loading state to parent component
const emit = defineEmits(['loading'])

// Retrieve email and server reference from the authentication store
const email = computed(() => authStore.email)
const serverRef = authStore.server_ref

// Function to trigger loading state
const triggerLoading = (value: boolean) => {
  emit('loading', value)
}

// Handle input in the OTP fields and move to the next field
const handleInput = (index: number, event: Event) => {
  const input = event.currentTarget
  if (!(input instanceof HTMLInputElement)) return

  const value = input.value
  codes.value[index] = value
  // Jump over to the next field
  if (value && index < codes.value.length - 1) {
    const nextInput = input.nextElementSibling
    if (nextInput instanceof HTMLElement) {
      nextTick(() => {
        nextInput.focus()
      })
    }
  }
}

// Handle keyboard navigation (Backspace, Arrow keys) in OTP fields
const handleKeydown = (index: number, event: KeyboardEvent) => {
  const input = event.currentTarget
  if (!(input instanceof HTMLInputElement)) return

  // Handle Backspace to delete current input and focus previous field
  if (event.key === 'Backspace' && !input.value && index > 0) {
    const prevInput = input.previousElementSibling
    if (prevInput instanceof HTMLElement) {
      nextTick(() => {
        prevInput.focus()
      })
    }
  } else if (event.key === 'ArrowRight') {
    const nextInput = input.nextElementSibling
    if (nextInput instanceof HTMLElement) {
      nextTick(() => {
        nextInput.focus()
      })
    }
  } else if (event.key === 'ArrowLeft') {
    const prevInput = input.previousElementSibling
    if (prevInput instanceof HTMLElement) {
      nextTick(() => {
        prevInput.focus()
      })
    }
  }
}

// Handle OTP verification process
const handleVerify = async () => {
  if (isVerifying.value || isResending.value) return

  isVerifying.value = true
  triggerLoading(true)
  let code = ''
  for (let i = 0; i < codes.value.length; i++) {
    code += codes.value[i]
  }

  try {
    const res = await axios({
      url: '/forgotPassVerify',
      method: 'post',
      data: {
        server_ref: serverRef,
        email: email.value,
        otp: code
      },
      headers: {
        'Content-Type': 'application/json'
      }
    })

    localStorage.setItem('otp', code)
    ElMessage.success(res.data.description)
    router.push('/resetPassword')
  } catch (rawError: unknown) {
    codes.value = ['', '', '', '', '', '']
    const error = rawError as { response?: { data?: { error?: string } } }
    const message = error.response?.data?.error

    if (message) {
      ElMessage.error(message)
    } else {
      ElMessage.error('Network error or server not responding. Please try again later.')
    }
  } finally {
    triggerLoading(false)
    isVerifying.value = false
  }
}

// Handle resend OTP functionality
const handleResend = async () => {
  if (isVerifying.value || isResending.value) return

  isResending.value = true
  triggerLoading(true)

  try {
    const res = await axios({
      url: '/sendOTP',
      method: 'post',
      data: {
        server_ref: serverRef,
      },
      headers: {
        'Content-Type': 'application/json'
      }
    })

    codes.value = ['', '', '', '', '', '']
    ElMessage.success(res.data.description)
  } catch (rawError: unknown) {
    const error = rawError as { response?: { data?: { error?: string } } }
    const message = error.response?.data?.error

    if (message) {
      ElMessage.error(message)
    } else {
      ElMessage.error('Network error or server not responding. Please try again later.')
    }
  } finally {
    triggerLoading(false)
    isResending.value = false
  }
}

// Navigate back to the login page
const handleNavButton = () => {
  router.push('/login')
}

// Focus the first input field when the component is mounted
onMounted(() => {
  if (inputRef.value && inputRef.value[0]) {
    inputRef.value[0].focus()
  }
})

</script>

<style scoped>
a {
  text-decoration: none;
  color: #FB9333;
}

button {
  width: 100%;
}

.resend-button {
  width: auto;
  padding: 0;
  color: black;
  font-weight: bold;
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
  background: linear-gradient(to bottom, #1E5192, #FFFFFF);
  align-items: center;
  justify-content: center;
} */

.text-header {
  font-weight: bold;
  color: black;
}

.text-sub-header {
  font-weight: bold;
  color: #7B7A7B;
}

.code-container {
  display: flex;
  justify-content: center;
}

.code-input {
  border: gray 2px solid;
  border-radius: 8px;
  margin: 20px 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: text;
  text-align: center;
}

.code-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 5px #007bff;
}


/* Phone */
@media screen and (max-width: 768px) {
  .form {
    width: 280px;
    height: 420px;
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
    font-size: 14px;
  }

  .text-sub-header {
    font-size: 10px;
  }

  .code-input {
    height: 30px;
    width: 30px;
    line-height: 30px;
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
    font-size: 28px;
  }

  .text-sub-header {
    font-size: 14px;
  }

  .code-input {
    height: 40px;
    width: 40px;
    line-height: 40px;
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

  .text-header {
    font-size: 28px;
  }

  .text-sub-header {
    font-size: 14px;
  }

  .code-input {
    height: 40px;
    width: 40px;
    line-height: 40px;
  }

}
</style>
