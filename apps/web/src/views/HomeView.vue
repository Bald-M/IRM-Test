<template>
  <div class="home-container">
    <Header />

    <div>
      <Banner :title="banner.title" :content="banner.content" :imgPath="banner.imgPath" :button="banner.button"
        :redirect="banner.redirect" class="mt-3" />

      <div class="box-container mt-6">

        <div>
          <el-text class="header">Who Benefits?</el-text>
        </div>


        <div class="box-section mt-3">

          <div v-for="item in boxes" :key="item.id" class="box">
            <Box :title="item.title" :content="item.content" :imgPath="item.imgPath" />
          </div>

        </div>

      </div>

      <div class="instruction-container mt-3">

        <div>
          <el-text class="header">How It Works?</el-text>
        </div>

        <div class="step-section mt-3">

          <div style="display: flex; justify-content: center;" v-for="item in steps" :key="item.id">
            <Step :stepNumber="item.stepNumber" :icon="item.icon" :iconPostion="item.iconPosition" :title="item.title" :description="item.description" />
          </div>

        </div>

      </div>

    </div>

    <Footer class="mt-3" />

    <el-backtop :right="100" :bottom="100" />


  </div>
</template>

<script lang="ts" setup>
import Header from '@/components/SiteHeaderComponent.vue'
import Footer from '@/components/SiteFooterComponent.vue'
import Banner from '@/components/BannerComponent.vue'
import Box from '@/components/SiteBoxComponent.vue'
import Step from '@/components/HomeStepComponent.vue'
import backgroundImage from '@/assets/Home/background.svg'
import studentIcon from '@/assets/Home/student.svg'
import shakeHandsIcon from '@/assets/Home/shake_hands.svg'
import eyesIcon from '@/assets/Home/eyes.svg'
import fillOutFormIcon from '@/assets/Home/fill_out_form_icon 1.svg'
import interviewWithIRMIcon from '@/assets/Home/interview_with_IRM_icon 2.svg'
import fileIcon from '@/assets/Home/hand_with_file_icon 3.svg'
import interviewWithClientIcon from '@/assets/Home/interview_with_client_icon 4.svg'
import bellIcon from '@/assets/Home/bell_icon 5.svg'
import signIcon from '@/assets/Home/sign_in_paper_icon 6.svg'
import targetIcon from '@/assets/Home/target_icon 7.svg'
import { reactive } from 'vue'

const banner = reactive({
  title: 'Helping Students Acheive Their Future',
  content: 'The Industry Internship System connects students with real-world industry opportunities, enabling them to gain practical experience, develop professional skills, and build a strong foundation for their future careers.',
  button: 'Get Involved',
  redirect: 'test',
  imgPath: backgroundImage
})

const boxes = reactive([
  { id: 1, title: 'Students', content: 'Easily submit your application, attend interviews, and track the status of your internship process.', imgPath: studentIcon },
  { id: 2, title: 'Industry Clients', content: 'Browse student profiles, invite students for interviews, and select the best candidates for internships.', imgPath: shakeHandsIcon },
  { id: 3, title: 'Industry Relationship Manager', content: 'Review applications, manage student profiles, and oversee the entire internship process.', imgPath: eyesIcon },
])

const steps = reactive([
  { id: 1, stepNumber: 1, icon: fillOutFormIcon, iconPosition: 'left', title: 'Form Submission', description: 'The student creates an account here and submits the application form.'  },
  { id: 2, stepNumber: 2, icon: interviewWithIRMIcon, iconPosition: 'right', title: 'IRM Interview', description: 'The Industry Relationship Manager (IRM) reviews the application and conducts an interview with the student.'  },
  { id: 3, stepNumber: 3, icon: fileIcon, iconPosition: 'left', title: 'Profile Publication', description: 'Once approved by the IRM, the student\'s profile is published for industry clients to review.'  },
  { id: 4, stepNumber: 4, icon: interviewWithClientIcon, iconPosition: 'right', title: 'Industry Interview', description: 'The Industry reviews the student profiles and conducts interviews with the student.'  },
  { id: 5, stepNumber: 5, icon: bellIcon, iconPosition: 'left', title: 'Offer Notification', description: 'The industry notifies both the student and the IRM about successful applications.'  },
  { id: 6, stepNumber: 6, icon: signIcon, iconPosition: 'right', title: 'Sign Contract', description: 'Intern and industry contracts are created, signed, and returned to IRM.'  },
  { id: 7, stepNumber: 7, icon: targetIcon, iconPosition: 'left', title: 'Start Internship', description: 'Internship days, times, and start date are finalized, and the student starts their internship.'  },
])

</script>

<style scoped>
.mt-3 {
  margin-top: 3rem !important;
}

.mt-1 {
  margin-top: 1rem !important;
}

.mt-2 {
  margin-top: 2rem !important;
}

.mt-6 {
  margin-top: 6rem !important;
}

.home-container {
  height: 100%;
  background: linear-gradient(to bottom, white 0%, #FFEDD7);
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