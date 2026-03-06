<template>
  <div class="main-layout">
    <AppHeader />
    <main class="main-content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  padding: 20px;
}
</style>
