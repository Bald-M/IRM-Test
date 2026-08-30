<template>
  <div>
    <!-- Breadcrumb navigation -->
    <div>
      <el-breadcrumb separator="/">
        <!-- Link to the admin homepage -->
        <el-breadcrumb-item :to="{ path: '/adminLayout' }">Homepage</el-breadcrumb-item>
        <!-- Static breadcrumb for the current page -->
        <el-breadcrumb-item>Student Detail</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Section containing action buttons -->
    <div class="button-section">
      <!-- Button to invite the student for an interview -->
      <el-button type="primary">Invite For Interview</el-button>
      <!-- Button to view or update the student's internship status -->
      <el-button>Internship Status</el-button>
    </div>

    <!-- Student detail section -->
    <!-- StudentBox component displays detailed information about the student -->
    <StudentBox class="mt-3" />

  </div>

</template>

<script lang="ts" setup>
import StudentBox from '@/components/AdminPanelStudentDetailComponent.vue'

// Defining a prop to receive the student's ID from the parent component
defineProps<{
  id: string
}>()

</script>

<style scoped>

.mt-3 {
  margin-top: 3rem;
}

.button-section {
  display: flex;
  justify-content: end;
}


</style>