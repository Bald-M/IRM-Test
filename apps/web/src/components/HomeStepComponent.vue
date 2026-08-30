<template>
  <div class="form-submission-wrapper" :style="{ alignItems: iconPostion === 'left' ? 'flex-start' : 'flex-end' }">
    <!-- Step Header outside the main content -->
    <div class="step-header">
      <span class="step-number">Step {{ stepNumber }}</span>
    </div>

    <!-- Main content inside the container -->
    <div class="form-submission-container">

      <el-row :gutter="20" class="form-row" v-if="iconPostion === 'left'">
        <el-col :span="10">
          <div class="icon-section">
            <img :src="icon" alt="Form Icon" />
          </div>
        </el-col>

        <el-col :span="2">
          <div class="divider"></div>
        </el-col>

        <el-col :span="10">

          <div class="text-section">
            <h3>{{ title }}</h3>
            <p class="mt-2">{{ description }}</p>
          </div>

        </el-col>

      </el-row>

      <el-row :gutter="20" class="form-row" v-if="iconPostion === 'right'">

        <el-col :span="10">
          <div class="text-section" style="padding-left: 4rem;">
            <!-- <h3>{{ title }}</h3>
            <p class="mt-2">{{ description }}</p> -->
            <h3>{{ title }}</h3>
            <p class="mt-2">{{ description }}</p>
          </div>
        </el-col>

        <el-col :span="2">
          <div class="divider"></div>
        </el-col>

        <el-col :span="10">
          <div class="icon-section">
            <img :src="icon" alt="Form Icon" />
          </div>
        </el-col>

      </el-row>

    </div>
  </div>


</template>

<script lang="ts" setup>
defineProps<{
  stepNumber: number,
  icon: string,
  iconPostion: string,
  title: string,
  description: string,
}>()

</script>


<style scoped>
.mt-2 {
  margin-top: 2rem;
}

.form-submission-wrapper {
  display: flex;
  /* Stack header and form vertically */
  flex-direction: column;
  width: 100%;
  padding: 20px;
}

.form-submission-container {
  /* background-color: white;
  padding: 20px;
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  width: 100%; */
  width: 100%;
  background-color: white;
  border-radius: 18px;
}

.step-header {
  margin-bottom: -20px;
  /* Pull the header up */
  z-index: 1;
  /* Make sure it is on top */
  position: relative;
  /* To adjust stacking context */
}

.step-number {
  background-color: white;
  color: #ff6600;
  border: 2px solid #ff6600;
  border-radius: 25px;
  padding: 5px 15px;
  font-weight: bold;
}

.icon-section {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-section img {
  width: 180px;
  height: auto;
  /* height: 200px; */
}

.form-row {
  display: flex;
  align-items: center;
  /* Vertically center all content within the row */
  margin: 4rem;
}

.divider {
  height: 180px;
  width: 4px;
  background-color: #ff6600;
  /* Orange divider line */
}

.text-section {
  height: 100%;
  /* width: 100%;
  background-color: lightblue;
  display: flex;
  justify-content: center;
  flex-direction: column; */
}

h3 {
  color: #FE6601;
  margin: 0;
  font-weight: bold;
  font-size: 30px;
}

p {
  color: #FE6601;
  font-size: 18px;
}

.highlight {
  font-weight: bold;
}
</style>
