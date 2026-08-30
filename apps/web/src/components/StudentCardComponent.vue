<template>
  <div class="container">

    <div class="header-section">

      <div class="header-text">
        <div>
          <el-text class="name">{{ name }}</el-text>
        </div>

        <div v-if="status === 1">
          <el-text>Avilable</el-text>
          <div class="available"></div>
        </div>

        <div v-else-if="status === 2">
          <el-text>For Review</el-text>
          <div class="review"></div>
        </div>

        <div v-else-if="status === 0">
          <el-text>Placed</el-text>
          <div class="unavailable"></div>
        </div>

      </div>

      <div class="header-logo">
        <img src="@/assets/SocialIcon/cv_icon_orange.svg" @click="navigate(cv_link)" />
        <img src="@/assets/SocialIcon/linkedin_icon_orange.svg" @click="navigate(linkedin_link)" />
        <img src="@/assets/SocialIcon/github_icon_orange.svg" @click="navigate(github_link)" />
        <img src="@/assets/SocialIcon/portfolio_icon_orange.svg" @click="navigate(portfolio_link)" />
      </div>

    </div>

    <div class="info-section">

      <div class="mt-2">
        <el-text class="statement">
          {{ personal_statement }}
        </el-text>
      </div>

      <div class="mt-2">
        <el-text class="option-key">Internship Options: </el-text>
        <el-text class="option-value">{{ internship_options.replace(/[\[\]"" ]/g, ' ') }}</el-text>
      </div>
    </div>

    <div class="footer-section">
      <img src="@/assets/SocialIcon/web_development_icon.svg">
      <img src="@/assets/SocialIcon/business_analysis_icon.svg">
      <img src="@/assets/SocialIcon/design_icon.svg">
    </div>

  </div>

</template>

<script lang="ts" setup>
import { openExternalUrl } from '@/utils/navigation'

defineProps<{
  name: string,
  status: number,
  cv_link: string,
  linkedin_link: string,
  github_link: string,
  portfolio_link: string,
  personal_statement: string,
  internship_options: string
}>()

const navigate = (url: string) => {
  openExternalUrl(url)
}

</script>


<style scoped>
.mt-2 {
  margin-top: 2rem !important;
}

.container {
  height: 800px;
  width: 400px;
  border: #e3e3e3 2px solid;
  padding: 15px 30px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  height: 110px
}

.header-logo>img {
  width: 40px;
  height: 40px;
  margin-left: 5px;
  cursor: pointer;
}

.info-section {
  height: 350px;
}

.info-section>:first-child {
  height: 150px;
}

.footer-section>img {
  margin: 20px 8px 0 0;
  width: 40px;
  height: 40px;
}

.statement,
.option-value {
  color: #3A3541;
  font-weight: 500;
}

.available,
.unavailable,
.review {
  width: 10px;
  height: 10px;
  content: '';
  border-radius: 180px;
  display: inline-block;
  margin: 0 10px;
}

.available {
  background-color: #09FF5C;
}

.unavailable {
  background-color: #FF0808;
}

.review {
  background-color: #FE6601;
}

.name,
.option-key {
  font-weight: bold;
  color: #0D99FF;
}

.name {
  font-size: 24px;
  text-decoration: underline;
}
</style>
