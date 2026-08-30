// @vitest-environment jsdom

import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { defineComponent, h, nextTick, type Component } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'
import EmailVerificationView from '@/views/login/EmailVerificationView.vue'
import LoginView from '@/views/login/LoginView.vue'
import RegistrationView from '@/views/login/RegistrationView.vue'
import RequestResetPasswordView from '@/views/login/RequestResetPasswordView.vue'
import ResetPassVerificationView from '@/views/login/ResetPassVerificationView.vue'
import ResetPasswordView from '@/views/login/ResetPasswordView.vue'

const { messageError, messageSuccess, routerPush } = vi.hoisted(() => ({
  messageError: vi.fn(),
  messageSuccess: vi.fn(),
  routerPush: vi.fn()
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    error: messageError,
    success: messageSuccess
  }
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerPush })
}))

type AxiosMock = ReturnType<typeof vi.fn>

const FormStub = defineComponent({
  name: 'ElForm',
  setup(_, { expose, slots }) {
    expose({
      validate: (callback?: (valid: boolean) => void) => callback ? callback(true) : true
    })

    return () => h('form', slots.default?.())
  }
})

const InputStub = defineComponent({
  name: 'ElInput',
  inheritAttrs: false,
  props: {
    modelValue: {
      type: [String, Number],
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    type: {
      type: String,
      default: 'text'
    }
  },
  emits: ['update:modelValue', 'blur'],
  setup(props, { attrs, emit }) {
    return () => h('input', {
      ...attrs,
      value: props.modelValue,
      placeholder: props.placeholder,
      type: props.type,
      onBlur: (event: FocusEvent) => emit('blur', event),
      onInput: (event: Event) => {
        emit('update:modelValue', (event.target as HTMLInputElement).value)
      }
    })
  }
})

const ButtonStub = defineComponent({
  name: 'ElButton',
  props: {
    disabled: Boolean,
    loading: Boolean
  },
  emits: ['click'],
  setup(props, { attrs, emit, slots }) {
    return () => h('button', {
      ...attrs,
      'aria-busy': String(props.loading),
      disabled: props.disabled || props.loading,
      type: 'button',
      onClick: (event: MouseEvent) => emit('click', event)
    }, slots.default?.())
  }
})

const SlotStub = defineComponent({
  inheritAttrs: false,
  setup(_, { attrs, slots }) {
    return () => h('div', attrs, slots.default?.())
  }
})

const TextStub = defineComponent({
  name: 'ElText',
  emits: ['click'],
  setup(_, { attrs, emit, slots }) {
    return () => h('span', {
      ...attrs,
      onClick: (event: MouseEvent) => emit('click', event)
    }, slots.default?.())
  }
})

const mountView = (component: Component, axios: AxiosMock): VueWrapper => mount(component, {
  global: {
    provide: {
      $axios: axios
    },
    stubs: {
      ElButton: ButtonStub,
      ElForm: FormStub,
      ElFormItem: SlotStub,
      ElInput: InputStub,
      ElRadio: SlotStub,
      ElRadioGroup: SlotStub,
      ElText: TextStub,
      RouterLink: SlotStub
    }
  }
})

const deferred = <T>() => {
  let reject!: (reason?: unknown) => void
  let resolve!: (value: T | PromiseLike<T>) => void
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve
    reject = promiseReject
  })

  return { promise, reject, resolve }
}

const getButton = (wrapper: VueWrapper, label: string) => {
  const button = wrapper.findAll('button').find((candidate) => candidate.text().trim() === label)

  expect(button, `Expected a button labelled "${label}"`).toBeDefined()
  return button!
}

const clickButton = async (wrapper: VueWrapper, label: string) => {
  await getButton(wrapper, label).trigger('click')
  await flushPromises()
}

const enterOtp = async (wrapper: VueWrapper, otp: string) => {
  const inputs = wrapper.findAll<HTMLInputElement>('.code-input')

  expect(inputs).toHaveLength(otp.length)
  for (const [index, input] of inputs.entries()) {
    await input.setValue(otp[index])
  }
}

const expectLoadingCycle = (wrapper: VueWrapper) => {
  expect(wrapper.emitted('loading')).toEqual([[true], [false]])
}

beforeEach(() => {
  setActivePinia(createPinia())
  localStorage.clear()
  messageError.mockReset()
  messageSuccess.mockReset()
  routerPush.mockReset()
})

describe('LoginView', () => {
  it('prevents duplicate login requests while pending and unlocks after failure', async () => {
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({
        data: {
          auth_key: 'student-token',
          profile_data: {
            app_uid: 42,
            email: 'student@example.com',
            name: 'Student',
            user_type: 'Student'
          }
        }
      })
    const wrapper = mountView(LoginView, axios)

    await wrapper.get('[data-test="username-field"]').setValue('student@example.com')
    await wrapper.get('[data-test="password-field"]').setValue('SecurePass1')
    const submit = getButton(wrapper, 'Log In')
    await submit.trigger('click')
    await nextTick()

    expect(submit.attributes('disabled')).toBeDefined()
    expect(submit.attributes('aria-busy')).toBe('true')
    await submit.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject(new Error('offline'))
    await flushPromises()
    expect(submit.attributes('disabled')).toBeUndefined()
    expect(submit.attributes('aria-busy')).toBe('false')

    await submit.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(routerPush).toHaveBeenCalledWith('/studentLayout')
  })

  it('submits credentials, stores the authenticated profile and follows the role route', async () => {
    const axios = vi.fn().mockResolvedValue({
      data: {
        auth_key: 'industry-token',
        profile_data: {
          app_uid: 41,
          email: 'partner@example.com',
          name: 'Industry Partner',
          user_type: 'Industry'
        }
      }
    })
    const wrapper = mountView(LoginView, axios)

    await wrapper.get('[data-test="username-field"]').setValue('partner@example.com')
    await wrapper.get('[data-test="password-field"]').setValue('SecurePass1')
    await clickButton(wrapper, 'Log In')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      url: '/login',
      method: 'post',
      data: {
        email: 'partner@example.com',
        password: 'SecurePass1'
      }
    }))
    expect(useAuthStore().authKey).toBe('industry-token')
    expect(useAuthStore().user_type).toBe('Industry')
    expect(messageSuccess).toHaveBeenCalledWith('Successfully Login')
    expect(routerPush).toHaveBeenCalledWith('/clientLayout')
    expectLoadingCycle(wrapper)
  })

  it('preserves the verification state when login requires an email OTP', async () => {
    const axios = vi.fn().mockRejectedValue({
      response: {
        data: {
          error: 'OTP sent, Email verification required',
          server_ref: 'verify-ref'
        }
      }
    })
    const wrapper = mountView(LoginView, axios)

    await wrapper.get('[data-test="username-field"]').setValue('student@example.com')
    await wrapper.get('[data-test="password-field"]').setValue('SecurePass1')
    await clickButton(wrapper, 'Log In')

    expect(messageError).toHaveBeenCalledWith('OTP sent, Email verification required')
    expect(useAuthStore().server_ref).toBe('verify-ref')
    expect(useAuthStore().email).toBe('student@example.com')
    expect(routerPush).toHaveBeenCalledWith('/emailVerification')
    expectLoadingCycle(wrapper)
  })
})

describe('RegistrationView', () => {
  it('prevents duplicate registrations while pending and unlocks after failure', async () => {
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({
        data: {
          description: 'Verification code sent',
          server_ref: 'registration-ref'
        }
      })
    const wrapper = mountView(RegistrationView, axios)

    await wrapper.get('[data-test="username-field"]').setValue('student@student.wintec.ac.nz')
    await wrapper.get('[data-test="password-field"]').setValue('SecurePass1')
    await wrapper.get('[data-test="confirm-password-field"]').setValue('SecurePass1')
    const submit = getButton(wrapper, 'Create Account')
    await submit.trigger('click')
    await nextTick()

    expect(submit.attributes('disabled')).toBeDefined()
    expect(submit.attributes('aria-busy')).toBe('true')
    await submit.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject(new Error('offline'))
    await flushPromises()
    expect(submit.attributes('disabled')).toBeUndefined()
    expect(submit.attributes('aria-busy')).toBe('false')

    await submit.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(routerPush).toHaveBeenCalledWith('/emailVerification')
  })

  it('submits a student registration and starts email verification', async () => {
    const axios = vi.fn().mockResolvedValue({
      data: {
        description: 'Verification code sent',
        server_ref: 'registration-ref'
      }
    })
    const wrapper = mountView(RegistrationView, axios)

    await wrapper.get('[data-test="username-field"]').setValue('student@student.wintec.ac.nz')
    await wrapper.get('[data-test="password-field"]').setValue('SecurePass1')
    await wrapper.get('[data-test="confirm-password-field"]').setValue('SecurePass1')
    await clickButton(wrapper, 'Create Account')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      url: '/registration',
      method: 'post',
      data: {
        email: 'student@student.wintec.ac.nz',
        password: 'SecurePass1',
        type: 'Student'
      }
    }))
    expect(useAuthStore().server_ref).toBe('registration-ref')
    expect(messageSuccess).toHaveBeenCalledWith('Verification code sent')
    expect(routerPush).toHaveBeenCalledWith('/emailVerification')
    expectLoadingCycle(wrapper)
  })

  it('shows the server rejection without advancing the registration flow', async () => {
    const axios = vi.fn().mockRejectedValue({
      response: { data: { error: 'Email address is already registered' } }
    })
    const wrapper = mountView(RegistrationView, axios)

    await wrapper.get('[data-test="username-field"]').setValue('student@student.wintec.ac.nz')
    await wrapper.get('[data-test="password-field"]').setValue('SecurePass1')
    await wrapper.get('[data-test="confirm-password-field"]').setValue('SecurePass1')
    await clickButton(wrapper, 'Create Account')

    expect(messageError).toHaveBeenCalledWith('Email address is already registered')
    expect(routerPush).not.toHaveBeenCalled()
    expect(useAuthStore().server_ref).toBe('')
    expectLoadingCycle(wrapper)
  })
})

describe('RequestResetPasswordView', () => {
  it('prevents duplicate reset requests while pending and unlocks after failure', async () => {
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({
        data: {
          description: 'If the account exists, a code has been sent',
          server_ref: 'reset-ref'
        }
      })
    const wrapper = mountView(RequestResetPasswordView, axios)

    await wrapper.get('input[type="text"]').setValue('student@example.com')
    const submit = getButton(wrapper, 'Send')
    await submit.trigger('click')
    await nextTick()

    expect(submit.attributes('disabled')).toBeDefined()
    expect(submit.attributes('aria-busy')).toBe('true')
    await submit.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject(new Error('offline'))
    await flushPromises()
    expect(submit.attributes('disabled')).toBeUndefined()
    expect(submit.attributes('aria-busy')).toBe('false')

    await submit.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(routerPush).toHaveBeenCalledWith('/resetPassVerification')
  })

  it('submits the email, stores reset state and advances to OTP verification', async () => {
    const axios = vi.fn().mockResolvedValue({
      data: {
        description: 'If the account exists, a code has been sent',
        server_ref: 'reset-ref'
      }
    })
    const wrapper = mountView(RequestResetPasswordView, axios)

    await wrapper.get('input[type="text"]').setValue('student@example.com')
    await clickButton(wrapper, 'Send')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      url: '/forgotPassRequest',
      method: 'post',
      data: { email: 'student@example.com' }
    }))
    expect(useAuthStore().server_ref).toBe('reset-ref')
    expect(useAuthStore().email).toBe('student@example.com')
    expect(messageSuccess).toHaveBeenCalledWith('If the account exists, a code has been sent')
    expect(routerPush).toHaveBeenCalledWith('/resetPassVerification')
    expectLoadingCycle(wrapper)
  })

  it('reports an unavailable server and keeps the user on the request page', async () => {
    const axios = vi.fn().mockRejectedValue(new Error('offline'))
    const wrapper = mountView(RequestResetPasswordView, axios)

    await wrapper.get('input[type="text"]').setValue('student@example.com')
    await clickButton(wrapper, 'Send')

    expect(messageError).toHaveBeenCalledWith(
      'Network error or server not responding. Please try again later.'
    )
    expect(routerPush).not.toHaveBeenCalled()
    expectLoadingCycle(wrapper)
  })
})

describe('ResetPassVerificationView', () => {
  it('prevents duplicate reset-code verification and unlocks after failure', async () => {
    useAuthStore().setServerRef('reset-ref', 'student@example.com')
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({ data: { description: 'Code verified' } })
    const wrapper = mountView(ResetPassVerificationView, axios)

    await enterOtp(wrapper, '123456')
    const verify = getButton(wrapper, 'Verify')
    await verify.trigger('click')
    await nextTick()

    expect(verify.attributes('disabled')).toBeDefined()
    expect(verify.attributes('aria-busy')).toBe('true')
    await verify.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject({ response: { data: { error: 'Code expired' } } })
    await flushPromises()
    expect(verify.attributes('disabled')).toBeUndefined()
    expect(verify.attributes('aria-busy')).toBe('false')

    await enterOtp(wrapper, '654321')
    await verify.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(routerPush).toHaveBeenCalledWith('/resetPassword')
  })

  it('prevents duplicate reset-code resends and unlocks after failure', async () => {
    useAuthStore().setServerRef('reset-ref', 'student@example.com')
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({ data: { description: 'A new code was sent' } })
    const wrapper = mountView(ResetPassVerificationView, axios)
    const resend = getButton(wrapper, 'Resend')

    await resend.trigger('click')
    await nextTick()

    expect(resend.attributes('disabled')).toBeDefined()
    expect(resend.attributes('aria-busy')).toBe('true')
    await resend.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject(new Error('offline'))
    await flushPromises()
    expect(messageError).toHaveBeenCalledWith(
      'Network error or server not responding. Please try again later.'
    )
    expect(resend.attributes('disabled')).toBeUndefined()
    expect(resend.attributes('aria-busy')).toBe('false')

    await resend.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(messageSuccess).toHaveBeenCalledWith('A new code was sent')
  })

  it('verifies the reset OTP, persists it for the next step and advances', async () => {
    useAuthStore().setServerRef('reset-ref', 'student@example.com')
    const axios = vi.fn().mockResolvedValue({
      data: { description: 'Code verified' }
    })
    const wrapper = mountView(ResetPassVerificationView, axios)

    await enterOtp(wrapper, '123456')
    await clickButton(wrapper, 'Verify')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      url: '/forgotPassVerify',
      method: 'post',
      data: {
        server_ref: 'reset-ref',
        email: 'student@example.com',
        otp: '123456'
      }
    }))
    expect(localStorage.getItem('otp')).toBe('123456')
    expect(messageSuccess).toHaveBeenCalledWith('Code verified')
    expect(routerPush).toHaveBeenCalledWith('/resetPassword')
    expectLoadingCycle(wrapper)
  })

  it('clears a rejected OTP and exposes the server error for retry', async () => {
    useAuthStore().setServerRef('reset-ref', 'student@example.com')
    const axios = vi.fn().mockRejectedValue({
      response: { data: { error: 'Invalid or expired verification code' } }
    })
    const wrapper = mountView(ResetPassVerificationView, axios)

    await enterOtp(wrapper, '654321')
    await clickButton(wrapper, 'Verify')

    expect(messageError).toHaveBeenCalledWith('Invalid or expired verification code')
    expect(wrapper.findAll<HTMLInputElement>('.code-input').map((input) => input.element.value))
      .toEqual(['', '', '', '', '', ''])
    expect(localStorage.getItem('otp')).toBeNull()
    expect(routerPush).not.toHaveBeenCalled()
    expectLoadingCycle(wrapper)
  })
})

describe('ResetPasswordView', () => {
  it('prevents duplicate password changes while pending and unlocks after failure', async () => {
    const authStore = useAuthStore()
    authStore.setServerRef('reset-ref', 'student@example.com')
    localStorage.setItem('otp', '123456')
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({ data: { description: 'Password changed' } })
    const wrapper = mountView(ResetPasswordView, axios)
    const passwordInputs = wrapper.findAll('input[type="password"]')

    await passwordInputs[0].setValue('NewSecure1')
    await passwordInputs[1].setValue('NewSecure1')
    const submit = getButton(wrapper, 'Reset Password')
    await submit.trigger('click')
    await nextTick()

    expect(submit.attributes('disabled')).toBeDefined()
    expect(submit.attributes('aria-busy')).toBe('true')
    await submit.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject(new Error('offline'))
    await flushPromises()
    expect(submit.attributes('disabled')).toBeUndefined()
    expect(submit.attributes('aria-busy')).toBe('false')

    await submit.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(routerPush).toHaveBeenCalledWith('/login')
  })

  it('submits the verified reset state, clears it and returns to login', async () => {
    const authStore = useAuthStore()
    authStore.setServerRef('reset-ref', 'student@example.com')
    localStorage.setItem('otp', '123456')
    const axios = vi.fn().mockResolvedValue({
      data: { description: 'Password changed' }
    })
    const wrapper = mountView(ResetPasswordView, axios)
    const passwordInputs = wrapper.findAll('input[type="password"]')

    await passwordInputs[0].setValue('NewSecure1')
    await passwordInputs[1].setValue('NewSecure1')
    await clickButton(wrapper, 'Reset Password')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      url: '/forgotPassChange',
      method: 'post',
      data: {
        email: 'student@example.com',
        server_ref: 'reset-ref',
        otp: '123456',
        password: 'NewSecure1'
      }
    }))
    expect(messageSuccess).toHaveBeenCalledWith('Password changed')
    expect(routerPush).toHaveBeenCalledWith('/login')
    expect(authStore.server_ref).toBe('')
    expect(localStorage.getItem('otp')).toBeNull()
    expectLoadingCycle(wrapper)
  })

  it('shows a reset failure while retaining the verified state for a retry', async () => {
    const authStore = useAuthStore()
    authStore.setServerRef('reset-ref', 'student@example.com')
    localStorage.setItem('otp', '123456')
    const axios = vi.fn().mockRejectedValue({
      response: { data: { error: 'Reset request has expired' } }
    })
    const wrapper = mountView(ResetPasswordView, axios)
    const passwordInputs = wrapper.findAll('input[type="password"]')

    await passwordInputs[0].setValue('NewSecure1')
    await passwordInputs[1].setValue('NewSecure1')
    await clickButton(wrapper, 'Reset Password')

    expect(messageError).toHaveBeenCalledWith('Reset request has expired')
    expect(authStore.server_ref).toBe('reset-ref')
    expect(localStorage.getItem('otp')).toBe('123456')
    expect(routerPush).not.toHaveBeenCalled()
    expectLoadingCycle(wrapper)
  })
})

describe('EmailVerificationView', () => {
  it('prevents duplicate email verification and unlocks after failure', async () => {
    useAuthStore().setServerRef('registration-ref', 'student@example.com')
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({ data: { description: 'Email verified' } })
    const wrapper = mountView(EmailVerificationView, axios)

    await enterOtp(wrapper, '123456')
    const verify = getButton(wrapper, 'Verify')
    await verify.trigger('click')
    await nextTick()

    expect(verify.attributes('disabled')).toBeDefined()
    expect(verify.attributes('aria-busy')).toBe('true')
    await verify.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject({ response: { data: { error: 'Code expired' } } })
    await flushPromises()
    expect(verify.attributes('disabled')).toBeUndefined()
    expect(verify.attributes('aria-busy')).toBe('false')

    await enterOtp(wrapper, '654321')
    await verify.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(routerPush).toHaveBeenCalledWith('/login')
  })

  it('prevents duplicate email-code resends and unlocks after failure', async () => {
    useAuthStore().setServerRef('registration-ref', 'student@example.com')
    const firstRequest = deferred<never>()
    const axios = vi.fn()
      .mockReturnValueOnce(firstRequest.promise)
      .mockResolvedValueOnce({ data: { description: 'A new code was sent' } })
    const wrapper = mountView(EmailVerificationView, axios)
    const resend = getButton(wrapper, 'Resend')

    await resend.trigger('click')
    await nextTick()

    expect(resend.attributes('disabled')).toBeDefined()
    expect(resend.attributes('aria-busy')).toBe('true')
    await resend.trigger('click')
    expect(axios).toHaveBeenCalledTimes(1)

    firstRequest.reject(new Error('offline'))
    await flushPromises()
    expect(messageError).toHaveBeenCalledWith(
      'Network error or server not responding. Please try again later.'
    )
    expect(resend.attributes('disabled')).toBeUndefined()
    expect(resend.attributes('aria-busy')).toBe('false')

    await resend.trigger('click')
    await flushPromises()
    expect(axios).toHaveBeenCalledTimes(2)
    expect(messageSuccess).toHaveBeenCalledWith('A new code was sent')
  })

  it('submits the registration OTP, clears verification state and returns to login', async () => {
    const authStore = useAuthStore()
    authStore.setServerRef('registration-ref', 'student@example.com')
    const axios = vi.fn().mockResolvedValue({
      data: { description: 'Email verified' }
    })
    const wrapper = mountView(EmailVerificationView, axios)

    await enterOtp(wrapper, '246810')
    await clickButton(wrapper, 'Verify')

    expect(axios).toHaveBeenCalledWith(expect.objectContaining({
      url: '/emailVerification',
      method: 'post',
      data: {
        server_ref: 'registration-ref',
        otp: '246810'
      }
    }))
    expect(messageSuccess).toHaveBeenCalledWith('Email verified')
    expect(routerPush).toHaveBeenCalledWith('/login')
    expect(authStore.server_ref).toBe('')
    expect(authStore.email).toBe('')
    expectLoadingCycle(wrapper)
  })

  it('clears a rejected OTP, reports the reason and remains in verification', async () => {
    useAuthStore().setServerRef('registration-ref', 'student@example.com')
    const axios = vi.fn().mockRejectedValue({
      response: { data: { error: 'Verification code has expired' } }
    })
    const wrapper = mountView(EmailVerificationView, axios)

    await enterOtp(wrapper, '135790')
    await clickButton(wrapper, 'Verify')

    expect(messageError).toHaveBeenCalledWith('Verification code has expired')
    expect(wrapper.findAll<HTMLInputElement>('.code-input').map((input) => input.element.value))
      .toEqual(['', '', '', '', '', ''])
    expect(useAuthStore().server_ref).toBe('registration-ref')
    expect(routerPush).not.toHaveBeenCalled()
    expectLoadingCycle(wrapper)
  })
})
