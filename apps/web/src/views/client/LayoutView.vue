<template>
  <div class="grid-container">
    <!-- Sidebar navigation for the admin panel -->
    <aside class="nav">
      <Nav />
    </aside>
    <!-- Header section for the admin panel -->
    <header class="header">
      <Header title="INDUSTRY INTERNSHIP SYSTEM ADMINISTRATION" />
    </header>
    <!-- Main content area where routed views are displayed -->
    <main class="main">
      <RouterView />
    </main>
    <!-- Back-to-top button positioned 100px from the right and bottom -->
    <el-backtop :right="100" :bottom="100" />
  </div>

</template>

<script lang="ts" setup>
import Nav from '@/components/ClientPanelLeftNavComponent.vue'
import Header from '@/components/PanelHeaderComponent.vue'

</script>

<style scoped>
.mt-2 {
  margin-top: 2rem;
}

.grid-container {
  display: grid;
  grid-template-areas:
    "nav header"
    "nav main";
  grid-template-columns: auto 1fr;
  grid-template-rows: auto 1fr;
  height: 100vh;
}

.nav {
  grid-area: nav;
  background-color: #FE6601;
  width: 240px;
}

.header {
  grid-area: header;
  background-color: #FFF;
  height: 100px;
}

.main {
  grid-area: main;
  overflow-y: auto;
  background-color: white;
  padding: 30px 25px;
}
</style>