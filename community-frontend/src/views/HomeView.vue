<template>
  <div class="home-page">
    <!-- Hero Card -->
    <div v-if="heroPost" class="hero-card card" @click="$router.push(`/posts/${heroPost.id}`)">
      <div class="hero-content">
        <el-tag size="small" effect="dark" type="warning">精选推荐</el-tag>
        <h2 class="hero-title">{{ heroPost.title }}</h2>
        <p class="hero-summary">{{ heroPost.summary || '暂无摘要' }}</p>
        <div class="hero-meta flex-center gap-12 text-secondary">
          <span>{{ heroPost.username }}</span>
          <span>{{ heroPost.categoryName }}</span>
          <span><el-icon><View /></el-icon> {{ heroPost.viewCount }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ heroPost.commentCount }}</span>
        </div>
      </div>
      <div v-if="heroPost.coverImage" class="hero-cover">
        <img :src="heroPost.coverImage" alt="" />
      </div>
    </div>

    <!-- Tab bar -->
    <div class="flex-between mb-16">
      <h3 style="font-size: 16px">
        {{ activeCategoryName ? `${activeCategoryName} 的帖子` : '最新帖子' }}
      </h3>
    </div>

    <!-- Post list -->
    <div v-if="loading" class="flex-center" style="padding: 60px; justify-content: center">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>
    <template v-else>
      <div v-if="filteredPosts.length === 0" class="placeholder-section">
        暂无帖子
      </div>
      <div v-else class="post-list">
        <PostCard v-for="post in filteredPosts" :key="post.id" :post="post" />
      </div>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadPosts"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPostsApi } from '@/api/posts'
import { useCategoryStore } from '@/stores/category'
import PostCard from '@/components/PostCard.vue'
import { View, ChatDotRound, Loading } from '@element-plus/icons-vue'

const route = useRoute()
const categoryStore = useCategoryStore()

const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

const heroPost = computed(() => posts.value[0] || null)

const activeCategoryId = computed(() => {
  const id = route.query.categoryId
  return id ? Number(id) : null
})

const activeCategoryName = computed(() => {
  if (!activeCategoryId.value) return null
  const cat = categoryStore.getCategoryById(activeCategoryId.value)
  return cat?.name || null
})

const filteredPosts = computed(() => {
  const list = heroPost.value ? posts.value.slice(1) : posts.value
  if (!activeCategoryId.value) return list
  return list.filter((p) => p.categoryName === activeCategoryName.value)
})

async function loadPosts() {
  loading.value = true
  try {
    const res = await getPostsApi({ page: currentPage.value - 1, size: pageSize })
    const data = res.data
    posts.value = data?.results || data || []
    total.value = data?.total || posts.value.length
  } catch (e) {
    console.error('加载帖子失败:', e)
  } finally {
    loading.value = false
  }
}

watch(() => route.query.categoryId, () => {
  currentPage.value = 1
})

onMounted(() => loadPosts())
</script>

<style scoped>
.hero-card {
  display: flex;
  gap: 20px;
  cursor: pointer;
  margin-bottom: 20px;
  transition: box-shadow 0.2s;
}

.hero-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.hero-content {
  flex: 1;
}

.hero-title {
  font-size: 22px;
  font-weight: 700;
  margin: 12px 0 8px;
  color: #303133;
}

.hero-summary {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-meta span {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.hero-cover {
  width: 240px;
  height: 160px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.hero-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
