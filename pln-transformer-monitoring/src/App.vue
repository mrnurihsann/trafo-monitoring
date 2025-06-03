<template>
  <div id="app" :class="{ 'dark-theme': isDarkMode }">
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const isDarkMode = ref(false)

onMounted(() => {
  // Load theme preference from localStorage
  const savedTheme = localStorage.getItem('theme')
  isDarkMode.value = savedTheme === 'dark'
  
  // Apply theme class to body
  document.body.classList.toggle('dark-theme', isDarkMode.value)
})
</script>

<style>
/* Global styles will be applied here */
</style>
