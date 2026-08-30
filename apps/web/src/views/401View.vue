<template>
  <!-- Main container with full-screen background -->
  <div class="container">
    <!-- Centered content section within the main container -->
    <div class="section">
      <!-- Center-aligned unauthorized response message -->
      <div style="text-align: center;">
        <!-- Large heading displaying the "Unauthorized" code -->
        <h1>401</h1>
        <!-- Subheading indicating the error message -->
        <h2>Unauthorized Response!</h2>
      </div>

      <!-- Link to return to the homepage -->
      <div style="text-align: center;" class="mt-6">
        <RouterLink to="/">&#8592; Go to homepage</RouterLink>
      </div>

      <!-- Center-aligned logo image -->
      <div style="display: flex; justify-content: center;">
        <img src="@/assets/Logo/Industry Internship System Logo_White.svg">
      </div>

    </div>
  </div>
</template>

<style scoped>
/* Styling for headings and link */
h1,
h2,
a {
  font-weight: bold;
}

h1 {
  color: rgb(215, 213, 228);
  font-size: 4rem;
}

h2 {
  color: rgb(162, 159, 181);
  font-size: 3rem;
}

/* Styling for homepage link */
a {
  text-decoration: none;
  color: white;
  font-size: 2rem;
}

/* Margin spacing classes */
.mt-3 {
  margin-top: 3rem !important;
}

.mt-6 {
  margin-top: 6rem !important;
}

/* Main container styling */
.container {
  /* Full screen height */
  height: 100vh;
  background-image: url('@/assets/Mockup_App 404 Pag_without_Content.svg');
  /* Ensures the image covers the container */
  background-size: cover;
  /* Centers the image */
  background-position: center;
  /* Prevents image repetition */
  background-repeat: no-repeat;
  display: flex;
  /* Vertically centers content */
  align-items: center;
}

/* Section styling */
.section {
  /* Fixed width for the content section */
  width: 1333px;
  /* Centers section horizontally */
  margin: 0 auto;
}
</style>