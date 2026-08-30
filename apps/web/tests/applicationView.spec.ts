// @vitest-environment jsdom

import { createPinia, setActivePinia, type Pinia } from 'pinia'
import { defineComponent, h, nextTick, type Component } from 'vue'
import { flushPromises, shallowMount, type VueWrapper } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'
import ApplicationView from '@/views/student/ApplicationView.vue'

const {
  axiosMock,
  errorMessageMock,
  routerPushMock,
  successMessageMock,
  warningMessageMock
} = vi.hoisted(() => ({
  axiosMock: vi.fn(),
  errorMessageMock: vi.fn(),
  routerPushMock: vi.fn(),
  successMessageMock: vi.fn(),
  warningMessageMock: vi.fn()
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    error: errorMessageMock,
    success: successMessageMock,
    warning: warningMessageMock
  }
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerPushMock })
}))

const profileStudent = {
  name: 'Alex Student',
  wintec_id: '1234567',
  personal_email: 'alex.personal@example.com',
  phone_number: '021 555 0101',
  personal_statement: 'I enjoy building dependable software for real users.',
  cv_link: 'https://example.com/alex/cv',
  linkedin_link: 'https://www.linkedin.com/in/alex-student',
  portfolio_link: 'https://example.com/alex',
  github_link: 'https://github.com/alex-student',
  programme_of_study: 'Bachelor of Applied IT',
  area_of_study: 'Software Engineering',
  skills: 'TypeScript, Java, testing',
  reference: 'Tutor One; Tutor Two',
  average_grade: 'A grade',
  favourite_courses: 'Software Engineering and Databases',
  first_preference: 'Internship',
  second_preference: 'Industry',
  internship_options: JSON.stringify(['API Development', 'Web Development']),
  preferred_companies: JSON.stringify(['Datacom', 'Gallagher']),
  gender: 'Female',
  student_type: 'Domestic Student'
}

const expectedApplication = {
  name: 'Alex Student',
  wintec_id: '1234567',
  gender: 'Female',
  student_type: 'Domestic Student',
  personal_email: 'alex.personal@example.com',
  student_email: 'signed-in.student@wintec.ac.nz',
  phone_number: '021 555 0101',
  personal_statement: 'I enjoy building dependable software for real users.',
  cv_link: 'https://example.com/alex/cv',
  linkedin_link: 'https://www.linkedin.com/in/alex-student',
  portfolio_link: 'https://example.com/alex',
  github_link: 'https://github.com/alex-student',
  average_grade: 'A grade',
  programme_of_study: 'Bachelor of Applied IT',
  area_of_study: 'Software Engineering',
  internship_options: ['API Development', 'Web Development'],
  preferred_companies: ['Datacom', 'Gallagher'],
  first_preference: 'Internship',
  second_preference: 'Industry',
  skills: 'TypeScript, Java, testing',
  favourite_courses: 'Software Engineering and Databases',
  references: 'Tutor One; Tutor Two'
}

type ApplicationViewState = {
  currentPage: number
  term: boolean
}

const FormStub = defineComponent({
  name: 'ElForm',
  setup(_props, { expose, slots }) {
    expose({
      validate: async (callback: (valid: boolean) => void) => {
        callback(true)
        return true
      }
    })

    return () => h('div', { class: 'form-stub' }, slots.default?.())
  }
})

const ButtonStub = defineComponent({
  name: 'ElButton',
  props: {
    disabled: Boolean,
    loading: Boolean
  },
  emits: ['click'],
  setup(props, { emit, slots }) {
    return () => h(
      'button',
      {
        'data-loading': props.loading ? 'true' : 'false',
        disabled: props.disabled,
        onClick: () => emit('click')
      },
      slots.default?.()
    )
  }
})

const PassThroughStub = defineComponent({
  setup(_props, { slots }) {
    return () => h('div', slots.default?.())
  }
})

const stubs: Record<string, Component> = {
  Banner: PassThroughStub,
  Box: PassThroughStub,
  'el-button': ButtonStub,
  'el-checkbox': PassThroughStub,
  'el-checkbox-group': PassThroughStub,
  'el-col': PassThroughStub,
  'el-form': FormStub,
  'el-form-item': PassThroughStub,
  'el-input': PassThroughStub,
  'el-option': PassThroughStub,
  'el-pagination': PassThroughStub,
  'el-radio': PassThroughStub,
  'el-radio-group': PassThroughStub,
  'el-row': PassThroughStub,
  'el-scrollbar': PassThroughStub,
  'el-select': PassThroughStub,
  'el-text': PassThroughStub
}

let pinia: Pinia

const mountApplication = () => shallowMount(ApplicationView, {
  global: {
    plugins: [pinia],
    provide: {
      $axios: axiosMock
    },
    stubs
  }
})

const showSubmitButton = async (wrapper: VueWrapper) => {
  const state = wrapper.vm as unknown as ApplicationViewState
  state.currentPage = 4
  state.term = true
  await nextTick()

  const submitButton = wrapper.findAll('button').find(button => (
    button.text() === 'Submit' && button.attributes('disabled') === undefined
  ))

  expect(submitButton).toBeDefined()
  return submitButton!
}

const submitApplication = async (wrapper: VueWrapper) => {
  const submitButton = await showSubmitButton(wrapper)
  await submitButton.trigger('click')
  await flushPromises()
}

const createDeferred = <T,>() => {
  let resolve!: (value: T) => void
  let reject!: (reason: unknown) => void
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve
    reject = promiseReject
  })

  return { promise, reject, resolve }
}

describe('student application page', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()

    pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().setAuthData(
      'test-token',
      'Alex Student',
      'signed-in.student@wintec.ac.nz',
      42,
      'Student'
    )
  })

  it('submits the populated application for the currently signed-in student and confirms success', async () => {
    axiosMock
      .mockResolvedValueOnce({ data: { student: profileStudent } })
      .mockResolvedValueOnce({ data: {} })

    const wrapper = mountApplication()
    await flushPromises()

    expect(axiosMock).toHaveBeenNthCalledWith(1, {
      url: '/userProfileData',
      method: 'post',
      data: { user_id: 42 },
      headers: { 'Content-Type': 'application/json' }
    })

    await submitApplication(wrapper)

    expect(axiosMock).toHaveBeenNthCalledWith(2, {
      url: '/completeApplication',
      method: 'post',
      data: expectedApplication,
      headers: { 'Content-Type': 'application/json' }
    })
    expect(successMessageMock).toHaveBeenCalledWith('Application saved')
    expect(routerPushMock).toHaveBeenCalledWith('/student/profile')
    expect(errorMessageMock).not.toHaveBeenCalled()
  })

  it('shows the API error and keeps the student on the form when submission fails', async () => {
    axiosMock
      .mockResolvedValueOnce({ data: { student: profileStudent } })
      .mockRejectedValueOnce({
        response: {
          data: { error: 'Application could not be saved' }
        }
      })

    const wrapper = mountApplication()
    await flushPromises()
    await submitApplication(wrapper)

    expect(errorMessageMock).toHaveBeenCalledWith('Application could not be saved')
    expect(successMessageMock).not.toHaveBeenCalled()
    expect(routerPushMock).not.toHaveBeenCalled()
  })

  it('prevents duplicate saves while a request is pending and allows retry after failure', async () => {
    const pendingSave = createDeferred<{ data: Record<string, never> }>()
    let saveAttempts = 0
    axiosMock.mockImplementation((request: { url: string }) => {
      if (request.url === '/userProfileData') {
        return Promise.resolve({ data: { student: profileStudent } })
      }

      saveAttempts += 1
      return saveAttempts === 1
        ? pendingSave.promise
        : Promise.resolve({ data: {} })
    })

    const wrapper = mountApplication()
    await flushPromises()
    const submitButton = await showSubmitButton(wrapper)

    await submitButton.trigger('click')
    await submitButton.trigger('click')
    await nextTick()

    expect(saveAttempts).toBe(1)
    expect(submitButton.attributes('disabled')).toBeDefined()
    expect(submitButton.attributes('data-loading')).toBe('true')

    pendingSave.reject({
      response: {
        data: { error: 'Please try saving again' }
      }
    })
    await flushPromises()

    expect(errorMessageMock).toHaveBeenCalledWith('Please try saving again')
    expect(submitButton.attributes('disabled')).toBeUndefined()
    expect(submitButton.attributes('data-loading')).toBe('false')

    await submitButton.trigger('click')
    await flushPromises()

    expect(saveAttempts).toBe(2)
    expect(successMessageMock).toHaveBeenCalledWith('Application saved')
  })

  it('recovers from damaged or empty saved choices and asks the student to review them', async () => {
    axiosMock
      .mockResolvedValueOnce({
        data: {
          student: {
            ...profileStudent,
            internship_options: '{damaged-json',
            preferred_companies: ''
          }
        }
      })
      .mockResolvedValueOnce({ data: {} })

    const wrapper = mountApplication()
    await flushPromises()

    expect(wrapper.exists()).toBe(true)
    expect(warningMessageMock).toHaveBeenCalledWith(
      'Some saved application choices could not be loaded. Please review and select them again.'
    )

    await submitApplication(wrapper)

    expect(axiosMock).toHaveBeenNthCalledWith(2, {
      url: '/completeApplication',
      method: 'post',
      data: {
        ...expectedApplication,
        internship_options: [],
        preferred_companies: []
      },
      headers: { 'Content-Type': 'application/json' }
    })
  })
})
