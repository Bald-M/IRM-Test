<template>
  <div class="container">
    <!-- Breadcrumb navigation -->
    <div>
      <el-breadcrumb separator="/">
        <!-- Link to the admin homepage -->
        <el-breadcrumb-item :to="{ path: '/adminLayout' }">Homepage</el-breadcrumb-item>
        <el-breadcrumb-item></el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Filter and search section -->
    <div class="select-section mt-3">

      <div>
        <!-- Programme selection dropdown -->
        <el-select v-model="programme" placeholder="Programme" size="large" class="select">
          <el-option v-for="item in programmes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

        <!-- Specialisation selection dropdown -->
        <el-select v-model="specialisation" placeholder="Specialisation" size="large" class="select">
          <el-option v-for="item in specialisations" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

        <!-- Internship status selection dropdown -->
        <el-select v-model="status" placeholder="Internship Status" size="large" class="select">
          <el-option v-for="item in internshipStatus" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

      </div>

      <!-- Search input field -->
      <div>
        <el-input v-model="input" size="large" placeholder="Type something" :prefix-icon="Search" />
      </div>
    </div>

    <!-- Section displaying candidate cards -->
    <div class="card-section mt-3">
      <el-row :gutter="20">
        <!-- Candidate card generated dynamically for each candidate -->
        <el-col :xl="6" :lg="8" v-for="item in candidates" :key="item.student_id">
          <CandidateCard :name="item.name" :status="item.intern_status" :cv_link="item.cv_link" :linkedin_link="item.linkedin_link" :github_link="item.github_link" :portfolio_link="item.portfolio_link" :personal_statement="item.personal_statement" :internship_options="item.internship_options" @click="navigate(item.app_user_id)" />
        </el-col>
      </el-row>

    </div>

    <!-- Pagination section -->
    <div class="pagination-section">
      <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[100, 200, 300, 400]"
        :size="size" :disabled="disabled" :background="background" layout="total, sizes, prev, pager, next, jumper"
        :total="400" class="mt-3 mb-3" />
    </div>


  </div>

</template>

<script lang="ts" setup>
import { ref, onMounted, inject } from 'vue'
import CandidateCard from '@/components/StudentCardComponent.vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, type ComponentSize } from 'element-plus'
import { useRouter } from 'vue-router'
import type { AxiosInstance } from 'axios'

// Injecting Axios instance
const axios: AxiosInstance = inject('$axios') as AxiosInstance
// Using Vue Router for navigation
const router = useRouter()
// Dropdown selections for programme, specialisation, and internship status
const programme = ref('')
const specialisation = ref('')
const status = ref('')

// Search input value
const input = ref('')

// Programme options for the dropdown
const programmes = [
  {
    value: 'Bachelor',
    label: 'Bachelor',
  },
  {
    value: 'Postgraduate',
    label: 'Postgraduate',
  },
  {
    value: 'Master',
    label: 'Master',
  }
]

// Specialisation options for the dropdown
const specialisations = [
  {
    value: 'Software Engineering',
    label: 'Software Engineering',
  },
  {
    value: 'Networking Engineering',
    label: 'Networing Engineering',
  },
  {
    value: 'Database or Data Analytics',
    label: 'Database or Data Analytics',
  }
]

// Internship status options for the dropdown
const internshipStatus = [
  {
    value: 'For Review',
    label: 'For Review'
  },
  {
    value: 'Avilable',
    label: 'Avilable'
  },
  {
    value: 'Placed',
    label: 'Placed'
  }
]

// Function to navigate to a candidate's detailed page
const navigate = (app_user_id: number) => {
  router.push({
    name: 'candidateDetails',
    params: { id: app_user_id }
  })
}

// Pagination settings
const currentPage = ref(0)
const pageSize = ref(10)
const size = ref<ComponentSize>('default')
const background = ref(true)
const disabled = ref(false)

// Interface for candidate details
interface Student {
  student_id: string
  app_user_id: number
  name: string
  intern_status: number
  cv_link: string
  linkedin_link: string
  github_link: string
  portfolio_link: string
  personal_statement: string
  internship_options: string
}

// List of candidates fetched from the API
const candidates = ref<Student[]>([])

// Fetching all students when the component is mounted
onMounted(() => {
  axios({
    url: '/allStudents',
    method: 'get'
  }).then(res => {
    if (res.data && res.data.students) {
      candidates.value = res.data.students
    } else {
      ElMessage.error('Candidate data is unavailable.')
    }
  }).catch(() => {
    ElMessage.error('Unable to load candidates. Please try again later.')
  })
})


</script>

<style scoped>

.mt-3 {
  margin-top: 3rem;
}

.mb-3 {
  margin-bottom: 3rem !important;
}

.el-col {
  margin-top: 2rem;
}

.container {
  background-color: white;
  height: 100%;
}

.select-section {
  display: flex;
  justify-content: space-between;
}

.pagination-section {
  display: flex;
  justify-content: center;
}

.select {
  width: 240px;
  margin-right: 20px;
}


</style>
