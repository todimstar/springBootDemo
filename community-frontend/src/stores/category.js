import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategoriesApi } from '@/api/categories'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const loaded = ref(false)

  async function fetchCategories() {
    if (loaded.value) return
    try {
      const res = await getCategoriesApi()
      categories.value = res.data || []
      loaded.value = true
    } catch (e) {
      console.error('获取分区列表失败:', e)
    }
  }

  function getCategoryById(id) {
    return categories.value.find((c) => c.id === id)
  }

  return { categories, loaded, fetchCategories, getCategoryById }
})
