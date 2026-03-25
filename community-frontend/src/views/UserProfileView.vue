<template>
  <div class="user-profile">
    <!-- Profile card -->
    <div class="card profile-card mb-20">
      <div class="flex-center gap-16">
        <el-avatar :size="80" :src="profileAvatar" icon="UserFilled" />
        <div>
          <h2>{{ profileName }}</h2>
          <p class="text-secondary">用户公开资料接口开发中 (GET /api/users/:id/public)</p>
        </div>
        <div style="margin-left: auto">
          <el-button disabled>关注 (开发中)</el-button>
        </div>
      </div>
    </div>

    <!-- User posts -->
    <h3 class="mb-16">TA 的帖子</h3>
    <div v-if="loading" class="flex-center" style="padding: 60px; justify-content: center">
      <el-icon class="is-loading" :size="24"><Loading /></el-icon>
    </div>
    <div v-else-if="posts.length === 0" class="placeholder-section">
      暂无帖子
    </div>
    <div v-else class="post-list">
      <PostCard v-for="p in posts" :key="p.id" :post="p" />
    </div>

    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadPosts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getUserPostsApi } from '@/api/posts'
import PostCard from '@/components/PostCard.vue'
import { Loading } from '@element-plus/icons-vue'

const route = useRoute()

const posts = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const loading = ref(false)

// 从帖子列表中提取作者信息（因为 /api/users/:id/public 未实现）
const profileName = computed(() => posts.value[0]?.username || `用户 #${route.params.id}`)
const profileAvatar = computed(() => posts.value[0]?.userAvatarUrl || '')

async function loadPosts() {
  loading.value = true
  try {
    const res = await getUserPostsApi(route.params.id, { page: currentPage.value - 1, size: pageSize })
    const data = res.data
    posts.value = data?.results || data || []
    total.value = data?.total || posts.value.length
  } catch (e) {
    console.error('加载用户帖子失败:', e)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.id, () => {
  currentPage.value = 1
  loadPosts()
})

onMounted(() => loadPosts())
</script>

<style scoped>
.user-profile {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card h2 {
  font-size: 20px;
  margin-bottom: 4px;
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
