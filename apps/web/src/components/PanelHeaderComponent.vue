<template>

  <div class="container">
    <p>{{ title }}</p>
  </div>

</template>

<script lang="ts" setup>
defineProps<{
  title: string
}>()

</script>


<style scoped>

.container {
  background-color: #F1F1F1;
  height: 100%;
  width: 100%;
  border-bottom: rgb(215,215,215) 3px solid;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
}

p {
  font-size: 28px;
  font-weight: bold;
  text-align: center;
}

</style>