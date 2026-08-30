<template>

  <div class="container">

    <div class="box-image">
      <img :src="imgPath" />
    </div>

    <div class="mt-2">
      <el-text class="title">{{ title }}</el-text>
    </div>

    <div class="mt-1">
      <el-text class="content">{{ content }}</el-text>
    </div>

  </div>

</template>

<script lang="ts" setup>

defineProps<{
  title: string,
  content: string,
  imgPath: string
}>()

</script>

<style scoped>
.container {
  height: 380px;
  width: 380px;
  border-radius: 27px;
  background-color: white;
  padding: 50px 30px;
}

.box-image {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 70px;
  height: 70px;
  background-color: #FE7235;
  border-radius: 180px;
  margin: 0 auto;
}

.box-image>img {
  width: 40px;
  height: 40px;
}

.title {
  color: #FE7235;
  font-weight: bold;
  font-size: 20px;
}

.content {
  color: #6B6B6B;
  font-size: 18px;
}

.mt-1 {
  margin-top: 1rem;
}

.mt-2 {
  margin-top: 2rem;
}


</style>