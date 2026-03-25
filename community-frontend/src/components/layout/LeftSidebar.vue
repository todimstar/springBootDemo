<template>
  <div class="left-sidebar">
    <div class="sidebar-section">
      <h4 class="section-title">分区导航</h4>
      <div class="category-list">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="category-item"
          :class="{ active: activeCategoryId === cat.id }"
          @click="selectCategory(cat.id)"
        >
          <span class="cat-name">{{ cat.name }}</span>
          <el-badge :value="cat.postCount" :max="999" type="info" />
        </div>
        <div
          class="category-item"
          :class="{ active: !activeCategoryId }"
          @click="selectCategory(null)"
        >
          <span class="cat-name">全部</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCategoryStore } from '@/stores/category'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const { categories } = storeToRefs(categoryStore)

const activeCategoryId = computed(() => {
  const id = route.query.categoryId
  return id ? Number(id) : null
})

function selectCategory(id) {
  if (id) {
    router.push({ path: '/', query: { categoryId: id } })
  } else {
    router.push({ path: '/' })
  }
}
</script>

<style scoped>
.left-sidebar {
  padding: 16px 0;
}

.section-title {
  padding: 0 16px;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  cursor: pointer;
  border-radius: 6px;
  margin: 2px 8px;
  transition: all 0.2s;
  font-size: 14px;
}

.category-item:hover {
  background: #f5f7fa;
}

.category-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 600;
}
</style>
