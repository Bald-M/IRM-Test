<template>

  <div>

    <Banner :title="banner.title" :content="banner.content" :button="banner.button" :imgPath="banner.backgroundImg"
      :redirect="banner.redirect" class="mt-3" />

    <div class="box-container mt-3">

      <div>
        <el-text class="header">Why Join the IIS?</el-text>
      </div>


      <div class="box-section mt-3">

        <div v-for="item in boxes" :key="item.id" class="box">
          <Box :title="item.title" :content="item.content" :imgPath="item.imgPath" />
        </div>

      </div>

    </div>

    <div class="instruction-container mt-3">

      <div>
        <el-text class="header">How does the IIS works?</el-text>
      </div>

      <div class="instruction-section mt-3">

        <div class="instruction-box">
          <div class="instruction-box-image">
            <img src="@/assets/fill_out_form_icon.svg" class="instruction-container-icon">
          </div>

          <div class="instruction-box-content">
            <div>
              <el-text class="title">Fill out this Form</el-text>
            </div>

            <div>
              <el-text class="explanation-content">Start your journey by filling out the Internship Application Form. Provide your basic information, academic background, and preferred internship details.</el-text>
            </div>

            <div class="mt-2">
              <el-input disabled placeholder="Full Name"></el-input>
            </div>

            <div class="mt-1">
              <el-input disabled placeholder="Student ID"></el-input>
            </div>

            <div class="mt-1">
              <el-button class="button">Apply Now</el-button>
            </div>

          </div>

        </div>

      </div>

      <div class="instruction-section mt-3">

        <div class="instruction-box">
          <div class="instruction-box-image">
            <img src="@/assets/interview_with_IRM_icon.svg" class="instruction-container-icon">
          </div>

          <div class="instruction-box-content">
            <div>
              <el-text class="title">Attend interview with IRM</el-text>
            </div>

            <div>
              <el-text class="explanation-content">After submitting your form, the industry relationship manager will
                review your application and schedule an interview to discuss your internship preferences and career
                goals.</el-text>
            </div>

            <div class="mt-2">
              <div class="info-section">

                <div class="info-image">
                  <img src="@/assets/exclamation_mark_icon.svg">
                </div>

                <div class="info-content">
                  <el-text>Students must submit their forms before interview.</el-text>
                </div>

              </div>
            </div>

            <div class="mt-1">
              <el-button class="button">Learn More Information</el-button>
            </div>

          </div>

        </div>


      </div>


      <div class="instruction-section mt-3">

        <div class="instruction-box">
          <div class="instruction-box-image">
            <img src="@/assets/interview_with_client_icon.svg" class="instruction-container-icon">
          </div>

          <div class="instruction-box-content">
            <div>
              <el-text class="title">Attend interview with client</el-text>
            </div>

            <div>
              <el-text class="explanation-content">Successful candidates will be forwarded to potential employers for an
                interview. The client will review your application and assess your fit for their internship
                opportunities.</el-text>
            </div>

            <div class="mt-2">

              <div class="info-section">

                <div class="info-image">
                  <img src="@/assets/exclamation_mark_icon.svg">
                </div>

                <div class="info-content">
                  <el-text>Ensure you have prepared all required documents.</el-text>
                </div>

              </div>

              <div class="mt-1">
                <el-button class="button">Learn More Information</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="instruction-section mt-3 mb-3">

        <div class="instruction-box">
          <div class="instruction-box-image">
            <img src="@/assets/offer_received_icon.svg" class="instruction-container-icon">
          </div>

          <div class="instruction-box-content">
            <div>
              <el-text class="title">Internship Offer Received</el-text>
            </div>

            <div>
              <el-text class="explanation-content">Upon successful completion of the interviews, you will receive an
                internship offer from the client. Confirm your placement and get ready to start your internship
                journey.</el-text>
            </div>

            <div class="mt-2">

              <div class="info-section">

                <div class="info-image">
                  <img src="@/assets/success_icon.svg">
                </div>

                <div class="info-content">
                  <el-text>Congratulations! You're closer to your future career.</el-text>
                </div>

              </div>

              <div class="mt-1">
                <el-button class="button">Learn More Information</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

  </div>


</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import Banner from '@/components/BannerComponent.vue'
import Box from '@/components/SiteBoxComponent.vue'
import backgroundImage from '@/assets/Background Image.jpg'
import experienceIcon from '@/assets/experience_icon.svg'
import networkIcon from '@/assets/networks_icon.svg'
import prospectsIcon from '@/assets/prospects_icon.svg'

const banner = reactive({
  title: 'Welcome to the Industry Internship System',
  content: 'Designed to streamline and automate the internship process for students, industry clients, and industry relationship managers.',
  button: 'Get Involved',
  backgroundImg: backgroundImage,
  redirect: ''
})

const boxes = reactive([
  { id: 1, title: 'Gain Real-World Experience', content: 'Engage in internships that provide you with hands-on experience, helping you to apply what you\'ve learned in class to real-life scenarios.', imgPath: experienceIcon },
  { id: 2, title: 'Build Professional Networks', content: 'Connect with industry professionals, build valuable relationships, and expand your network within your field of study.', imgPath: networkIcon },
  { id: 3, title: 'Enhance Your Career Prospects', content: 'Leverage the Industry Internship System to strengthen your resume, gain recommendations, and increase your employability after graduation.', imgPath: prospectsIcon },
])

</script>

<style scoped>

.mt-1 {
  margin-top: 1rem !important;
}

.mt-2 {
  margin-top: 2rem !important;
}

.mt-3 {
  margin-top: 3rem !important;
}

.mb-3 {
  margin-bottom: 3rem !important;
}

.el-input {
  width: 90%;
}

.el-button {
  width: 90%;
}

.box-container,
.instruction-container {
  padding: 0 10vw;
  box-sizing: border-box;
}

.instruction-box {
  width: 100%;
  height: 100%;
  background-color: white;
  border-radius: 27px;
  display: flex;
  align-items: stretch;
}

.instruction-box-image {
  flex: 1;
  background-color: #FE6601;
  border-radius: 27px 0 0 27px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.instruction-container-icon {
  width: 150px;
  height: 150px;
}

.instruction-box-content {
  flex: 2;
  padding: 30px 60px;
}

.info-section {
  width: 90%;
  border-radius: 7px;
  display: flex;
}

.info-image {
  width: 10%;
  border-radius: 7px 0 0 7px;
  background-color: black;
  display: flex;
  justify-content: center;
  align-items: center;
}

.info-image>img {
  height: 20px;
  width: 20px;
}

.info-content {
  width: 90%;
  border-radius: 0 7px 7px 0;
  background-color: #ECECEC;
  padding: 5px 18px;
}

.info-content>.el-text {
  font-weight: bold;
  font-size: 16px;
}

.el-input {
  width: 90%;
}

.el-button {
  width: 90%;
}

.client-info-section {
  display: flex;
  justify-content: space-between;
  border: #BBBBBB 1px solid;
  border-radius: 7px;
}

.client-title,
.client-link {
  text-align: center;
  padding: 10px;
}

a {
  color: #FE7235;
  text-decoration: none;
  font-weight: bold;
}

.button {
  background-color: #FE6601;
  color: white;
}

.header {
  color: #FE7235;
  font-weight: bold;
  font-size: 45px;
}

.box-section {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  grid-template-rows: auto;
  gap: 20px;
}

.title {
  color: #FE7235;
  font-weight: bold;
  font-size: 50px;
}

.explanation-content {
  color: #9C9C9C;
  font-size: 15px;
}
</style>
