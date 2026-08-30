<template>
  <div class="admin-panel-left-nav-comp">

    <!-- Dynamic -->
    <div class="avater-container">
      <img src="@/assets/sue.jfif" />
    </div>

    <!-- Dynamic -->
    <div class="greeting-container">
      <el-text class="greeting">Kia ora,</el-text>
      <br />
      <el-text class="name">Sue Pearson</el-text>
    </div>

    <!-- Navigation -->
    <div class="mt-3">

      <div class="dashboard-text">
        <el-text>Dashboards</el-text>
      </div>

      <nav>

        <div class="nav-item" :class="{ active: selected === 1 }" :index="1" @click="handleRouter">
          <img src="@/assets/LeftNavIcon/students.png" class="icon" />
          <a>Students</a>
        </div>

        <div class="nav-item" :class="{ active: selected === 2 }" :index="2" @click="handleRouter">
          <img src="@/assets/LeftNavIcon/candidates.png" class="icon" />
          <a>Candidates</a>
        </div>

        <!-- Setting /Change Password /Log Out -->
        <div class="nav-item" :class="{ active: selected === 3 }" :index="3" @click="handleRouter">
          <img src="@/assets/LeftNavIcon/lock.png" class="icon" />
          <a>Change Password</a>
        </div>

        <div class="nav-item" :class="{ active: selected === 4 }" :index="4" @click="logOut">
          <img src="@/assets/LeftNavIcon/logout.png" class="icon" />
          <a>Log Out</a>
        </div>

      </nav>

    </div>

    <div class="logo-section">
      <img src="@/assets/Logo/Industry Internship System Logo_White.svg">
    </div>

  </div>

</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const selected = ref(0)


const route = useRoute()
const authStore = useAuthStore()

// Watch for route changes and update the selected value accordingly
watch(
  () => route.path,
  (newPath) => {
    if (newPath.includes('studentsList')) {
      selected.value = 1
    } else if (newPath.includes('candidatesList')) {
      selected.value = 2
    } else if (newPath.includes('changePassword')) {
      selected.value = 3
    } else if (newPath.includes('logOut')) {
      selected.value = 4
    } else {
      selected.value = 0 // Default case
    }
  },
  { immediate: true } // Run immediately on component mount
)


const logOut = () => {
  authStore.clearAuthData()
  window.location.reload()
}

const handleRouter = (event: MouseEvent) => {
  const targetElement = event.currentTarget
  if (!(targetElement instanceof HTMLElement)) return

  const indexValue = targetElement.getAttribute('index')

  selected.value = Number(indexValue)

  switch (selected.value) {
    case 1:
      break
    case 2:
      break
    case 3:
      break
    case 4:
      break
  }
}

</script>


<style scoped>
.mt-3 {
  margin-top: 3rem;
}

.mt-6 {
  margin-top: 6rem;
}

.admin-panel-left-nav-comp {
  display: flex;
  flex-direction: column; /* Align children in a column */
  height: 100vh; /* Use full height of the viewport */
  background-color: #FE6601; /* Uncomment if needed */
}

.avater-container {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.avater-container > img {
  width: 100px;
}

.greeting-container,
.dashboard-text {
  padding: 0 14px;
}

.avater-container,
.greeting-container,
.mt-3 {
  flex-shrink: 0; /* Prevent these elements from shrinking */
}

.logo-section {
  margin-top: auto; /* Pushes this section to the bottom */
  display: flex;
  justify-content: center;
  align-items: flex-end; /* Align logo at the bottom */
}

.logo-section > img {
  width: 100%;
  height: 160px;
}

.name,
a {
  color: white;
}

.greeting,
.dashboard-text .el-text {
  font-size: 20px;
  color: rgb(253, 190, 133);
}

.name {
  font-size: 24px;
  font-weight: 500;
}

nav {
  padding: 0 8px;
}

.nav-item {
  width: 100%;
  height: 40px;
  line-height: 40px;
  display: flex;
  align-items: center;
  padding-left: 10px;
}

.nav-item:hover,
.active {
  background-color: rgb(241, 97, 1);
  border-radius: 8px;
}

a {
  text-decoration: none;
  font-weight: 500;
}

.icon {
  margin-right: 4px;
  width: 18px;
  height: 18px;
}
</style>
