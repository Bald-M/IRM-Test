<template>
  <div class="container">
    <!-- Header component for the student interface -->
    <Header />

    <!-- Main content area where routed views are displayed -->
    <RouterView />

    <!-- Footer component for the student interface -->
    <Footer />

    <!-- Back-to-top button positioned 100px from the right and bottom -->
    <el-backtop :right="100" :bottom="100" />

  </div>
</template>

<script lang="ts" setup>
// This is the shared component file for the student interface.
// It defines common elements such as the header and footer for the student pages.
// You can use this file to define other shared elements or the background color
// for the student interface. Components used here are located in the `src/components` directory.
import Header from '@/components/SiteHeaderComponent.vue'
import Footer from '@/components/SiteFooterComponent.vue'


</script>


<style scoped>
/* Container that wraps the content and ensures it takes full height */
.container {
  height: 100%;
  background: linear-gradient(to bottom, white 0%, #FFEDD7);
}
</style>