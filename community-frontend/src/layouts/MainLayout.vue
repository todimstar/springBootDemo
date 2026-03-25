<template>
  <div class="main-layout">
    <AppHeader />
    <div class="layout-body">
      <aside class="layout-left">
        <LeftSidebar />
      </aside>
      <main class="layout-main">
        <router-view />
      </main>
      <aside class="layout-right">
        <RightSidebar />
      </aside>
    </div>
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
import LeftSidebar from '@/components/layout/LeftSidebar.vue'
import RightSidebar from '@/components/layout/RightSidebar.vue'
import { useCategoryStore } from '@/stores/category'
import { onMounted } from 'vue'

const categoryStore = useCategoryStore()
onMounted(() => {
  categoryStore.fetchCategories()
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: var(--color-bg);
}

.layout-body {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 0;
  padding: 0 24px;
}

.layout-left {
  width: var(--sidebar-width);
  flex-shrink: 0;
  position: sticky;
  top: var(--header-height);
  height: calc(100vh - var(--header-height));
  overflow-y: auto;
}

.layout-main {
  flex: 1;
  min-width: 0;
  padding: 16px 20px;
}

.layout-right {
  width: var(--right-sidebar-width);
  flex-shrink: 0;
  position: sticky;
  top: var(--header-height);
  height: calc(100vh - var(--header-height));
  overflow-y: auto;
}

@media (max-width: 1200px) {
  .layout-right { display: none; }
}

@media (max-width: 900px) {
  .layout-left { display: none; }
}
</style>
