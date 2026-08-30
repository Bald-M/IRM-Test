<template>
  <footer>

    <div class="first-layer-container">

      <div><img src="@/assets/Logo/Te Pukenga_Logo_Blue.svg" class="tepukenga-logo"></div>

      <div class="divider"></div>

      <div class="first-layer-text-section">
        <el-text>Wintec is part of Te Pūkenga - New Zealand Institute of Skills and Technology &nbsp;</el-text>
      </div>
    </div>

    <div class="second-layer-container">

      <div class="second-layer-iis-section">

        <div>
          <img src="@/assets/Logo/IIS logo_without_name.svg" class="iis-logo">
        </div>

        <div class="second-layer-iis-text-section">

          <div>
            <el-text>Industry Internship System</el-text>
            <br />
            <el-text>Copyright @ IIS 2024. All rights reserved.</el-text>
          </div>

        </div>

      </div>

      <div style="padding-top: 20px;">
        <div>
          <el-text style="font-weight: bold;">Contact Us</el-text>
        </div>

        <div class="nav-logo">
          <img src="@/assets/FooterIcon/facebook_logo.svg" class="logo" @click="navigate('https://www.facebook.com/wintec/?_rdr')" />
          <img src="@/assets/FooterIcon/instagram_logo.svg" class="logo" @click="navigate('https://www.instagram.com/wintecnz/?hl=en')" />
          <img src="@/assets/FooterIcon/youtube_logo.svg" class="logo"  @click="navigate('https://www.youtube.com/@WintecHamiltonNZ?app=desktop')"/>
          <img src="@/assets/FooterIcon/linkedin_logo.svg" class="logo" @click="navigate('https://www.linkedin.com/school/waikato-institute-of-technology/posts/?feedView=all')" />
        </div>

      </div>

    </div>

  </footer>
</template>

<script lang="ts" setup>
import { openExternalUrl } from '@/utils/navigation'

const navigate = (url: string) => {
  openExternalUrl(url)
}

</script>


<style scoped>
footer {
  width: 100%;
  background-color: white;
}

.first-layer-container {
  display: flex;
  justify-content: space-between;
  padding: 20px 30px;
}


.first-layer-text-section {
  display: flex;
  align-items: center;
}

.first-layer-text-section>.el-text {
  font-weight: bold;
}

.second-layer-container {
  border-top: #1E5192 10px solid;
  display: flex;
  justify-content: space-between;
  padding: 20px 50px;
}

.second-layer-iis-section {
  display: flex;
}

.second-layer-iis-text-section {
  display: flex;
  align-items: center;
}

.second-layer-iis-text-section>div>.el-text:first-child {
  font-size: 32px;
  font-weight: bold;
}

.second-layer-iis-text-section>div>.el-text:last-child {
  font-size: 16px;
  font-weight: 500;
  display: flex;
  justify-content: center;
}

.nav-logo {
  margin-top: 10px;
}


.el-text {
  font-size: 18px;
  color: #1E5192;
}

.tepukenga-logo {
  width: 400px;
  height: 60px;
}

.iis-logo {
  width: 100px;
  height: 100px;
}

.logo {
  height: 40px;
  width: 40px;
  margin: 0 8px;
  cursor: pointer;
}

.divider::before {
  content: '';
  display: inline-block;
  height: 60px;
  /* as same as tepukenga logo */
  width: 3px;
  background-color: #1a468c;
  margin: 0 20px;
}
</style>
