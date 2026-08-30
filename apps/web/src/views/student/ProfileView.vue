<template>
  <div>
    <!-- Container for the student profile page -->
    <div class="mt-3 profile-container mb-5">
      <!-- Profile header section -->
      <div class="profile-header">
        <div>
          <!-- Title for the profile section -->
          <el-text class="header">Student Profile</el-text>
        </div>

        <!-- Information bar with navigation link -->
        <div class="profile-bar">
          <el-text class="profile-bar-text">To edit your profile, go to&nbsp;</el-text>
          <el-text class="profile-bar-text" style="text-decoration: underline; cursor: pointer;"
            @click="handleNavigation('/student/application')">Application&nbsp;</el-text>
          <el-text class="profile-bar-text">and edit your application form.</el-text>
        </div>
      </div>

      <!-- Student profile details section -->
      <Profile class="mt-3" />

    </div>

  </div>
</template>

<script lang="ts" setup>
import Profile from '@/components/StudentDetailComponent.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// Function to handle navigation to a specified URL
const handleNavigation = (url: string) => {
  router.push(url)
}
</script>

<style scoped>
.mt-3 {
  margin-top: 3rem !important;
}

.mt-6 {
  margin-top: 6rem !important;
}

.mb-5 {
  margin-bottom: 5rem !important;
}

.profile-container {
  width: 1200px;
  height: 1000px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
}

.profile-bar {
  border: #FE6601 1px solid;
  background-color: #FE6601;
  border-radius: 12px;
  display: flex;
  margin-left: 3rem;
  padding: 0 2rem;
}

.profile-bar-text {
  color: white;
  font-weight: bold;
}

.header {
  font-size: 32px;
  font-weight: bold;
  color: #FE6601;
}
</style>