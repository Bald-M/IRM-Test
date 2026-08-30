<template>
  <div class="container" v-loading="isLoading">
    <!-- Render the child components inside RouterView -->
    <!-- Shared layout for registration, login, and forgot password views -->
    <RouterView @loading="setLoading" />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

// Create a ref to manage loading state
const isLoading = ref(false)

// Method to update the loading state
const setLoading = (value: boolean) => {
  isLoading.value = value
}

</script>

<style scoped>
/* Container that wraps the content and ensures it takes full height */
.container {
  height: 100vh;
  background: linear-gradient(to bottom, #1E5192, #FFFFFF);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
