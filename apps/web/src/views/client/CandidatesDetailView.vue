<template>
  <div>
    <!-- Breadcrumb navigation -->
    <div>
      <el-breadcrumb separator="/">
        <!-- Link to the client homepage -->
        <el-breadcrumb-item :to="{ path: '/clientLayout' }">Homepage</el-breadcrumb-item>
        <!-- Static breadcrumb for the current page -->
        <el-breadcrumb-item>Candidate Detail</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- Student detail section -->
    <!-- StudentBox component displays detailed information about a candidate -->
    <StudentBox class="mt-3" />

  </div>

</template>

<script lang="ts" setup>
// Importing the StudentBox component for displaying candidate details
import StudentBox from '@/components/ClientPanelStudentDetailComponent.vue'

// Defining a prop to receive the candidate's ID from the parent component
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