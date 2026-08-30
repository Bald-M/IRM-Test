<template>

  <div class="container">

    <div class="section">

      <div class="text-section">
        <div>
          <el-text>{{ title }}</el-text>
        </div>

        <div>
          <el-text line-clamp="5">{{ content }}</el-text>
        </div>

        <div>
          <el-button type="primary">{{ button }}</el-button>
        </div>

      </div>

      <div class="image-section">
        <img :src="imgPath" class="shadow" />
      </div>

    </div>

  </div>

</template>

<script lang="ts" setup>

defineProps<{
  title: string,
  content: string,
  button: string,
  redirect: string,
  imgPath: string
}>()


</script>

<style scoped>
/* Container with padding on the sides */
.container {
  width: 100%;
  /* Padding as a percentage of the viewport width */
  padding: 0 10vw;
  box-sizing: border-box;
}

/* Section Flexbox Container */
.section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* Allow wrapping on smaller screens */
  flex-wrap: wrap;
}

/* Text Section */
.text-section {
  padding-right: 20px;
}

/* Image Section */
.image-section {
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-section>img {
  /* Make the image take full width 80% */
  width: 100%;
  /* Restrict max size */
  max-width: 500px;;
}

.shadow {
  display: block;
  max-width: 100%;
  height: auto;
  box-shadow: 10px 10px 0 #FE6601;
  border-radius: 10px;
}

/* Responsive Typography */
.text-section>div:first-child>.el-text {
  color: #FE6601;
  font-weight: bold;
   /* Use relative units */
  font-size: 2.5rem;
}

.text-section>div:nth-child(2)>.el-text {
  color: #777777;
  font-size: 1.5rem;
}

.text-section>div:last-child {
  margin-top: 20px;
}

.el-button {
  width: 80%;
  height: 40px;
}

/* Media Queries for Responsive Design */

/* Phone */
/* Dev in the future */
@media screen and (max-width: 768px) {
  /* .section {
    flex-direction: column;
    text-align: center;
  }

  .text-section {
    width: 100%;
    padding-right: 0;
  }

  .image-section {
    width: 100%;
    margin-top: 20px;
  }

  .image-section>img {
    max-width: 300px;
  } */
}


/* Tablet */
/* Dev in the future */
@media screen and (max-width: 992px) and (min-width: 768px) {
  /* .section {
    flex-direction: column;
  }

  .text-section, .image-section {
    width: 100%;
    text-align: center;
  } */
}

/* Computer */
@media screen and (min-width: 992px) {
  /* .container {
    padding: 0 150px;
  } */

  .text-section {
    width: 50%;
  }

  .image-section {
    width: 50%;
  }
}

</style>