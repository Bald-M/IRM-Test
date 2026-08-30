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
        <!-- Dropdown for selecting programme -->
        <el-select v-model="programme" placeholder="Programme" size="large" class="select">
          <el-option v-for="item in programmes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

        <!-- Dropdown for selecting specialisation -->
        <el-select v-model="specialisation" placeholder="Specialisation" size="large" class="select">
          <el-option v-for="item in specialisations" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

        <!-- Dropdown for selecting internship status -->
        <el-select v-model="status" placeholder="Internship Status" size="large" class="select">
          <el-option v-for="item in internshipStatus" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>

      </div>

      <!-- Input for search functionality -->
      <div>
        <el-input v-model="input" size="large" placeholder="Type something" :prefix-icon="Search" />
      </div>
    </div>

    <!-- Table displaying student data -->
    <div class="table-section mt-2">
      <el-table :data="students" style="width: 100%" @row-click="handleRowClick">
        <el-table-column prop="name" label="NAME" width="120" />
        <el-table-column prop="wintec_id" label="STUDENT ID" width="120" />
        <el-table-column prop="area_of_study" label="SPECIALIZATION" width="200" />
        <el-table-column prop="programme_of_study" label="PROGRAMME" width="200" />
        <el-table-column prop="student_email" label="STUDENT EMAIL" width="220" />
        <el-table-column prop="phone_number" label="PHONE NUMBER" width="200" />
        <el-table-column prop="intern_status" label="STATUS" width="140" />
        <el-table-column fixed="right" label="OPERATIONS" min-width="250">
          <template #default>
            <el-button type="danger" size="large" :icon="Delete">
              Delete
            </el-button>
            <el-button type="primary" size="large" :icon="Edit">Edit</el-button>
          </template>
        </el-table-column>
      </el-table>
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
import { ref, inject, onMounted } from 'vue'
import { Search, Edit, Delete } from '@element-plus/icons-vue'
import type { AxiosInstance } from 'axios'
import { ElMessage, type ComponentSize } from 'element-plus'
import { useRouter } from 'vue-router'

// Axios instance for API calls
const axios: AxiosInstance = inject('$axios') as AxiosInstance
// Router instance for navigation
const router = useRouter()
// Reactive variables for dropdown selections
const programme = ref('')
const specialisation = ref('')
const status = ref('')

// Reactive variable for search input
const input = ref('')

// Pagination settings
const currentPage = ref(0)
const pageSize = ref(10)
const size = ref<ComponentSize>('default')
const background = ref(true)
const disabled = ref(false)

// Options for programmes dropdown
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

// Options for specialisations dropdown
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

// Options for internship status dropdown
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

// Student interface to define the structure of student data
interface Student {
  name: string
  app_user_id: string
  wintec_id: string
  area_of_study: string
  programme_of_study: string
  student_email: string
  phone_number: string
  intern_status: number
}

// Navigate to student details page on row click
const handleRowClick = (row: Student) => {
  router.push({
    name: 'studentDetails',  // Ensure this route is defined in your router
    params: { id: row.app_user_id }  // Pass the unique ID or other parameter
  })
}

// Reactive variable for student data
const students = ref([])

// Fetch student data when the component is mounted
onMounted(() => {
  axios({
    url: '/allStudents',
    method: 'get'
  }).then(res => {
    if (res.data && res.data.students) {
      students.value = res.data.students
    } else {
      ElMessage.error('Student data is unavailable.')
    }
  }).catch(() => {
    ElMessage.error('Unable to load students. Please try again later.')
  })
})

</script>

<style scoped>
.container {
  background-color: white;
  height: 100%;
}

.select-section {
  display: flex;
  justify-content: space-between;
}

.table-section {
  width: 100%;
}

.pagination-section {
  display: flex;
  justify-content: center;
}

.select {
  width: 240px;
  margin-right: 20px;
}

.mt-2 {
  margin-top: 2rem;
}

.mt-3 {
  margin-top: 3rem;
}

.mb-3 {
  margin-bottom: 3rem !important;
}
</style>
