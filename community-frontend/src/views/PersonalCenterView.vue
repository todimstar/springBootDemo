<template>
  <div class="personal-center">
    <h2 class="page-title">个人中心</h2>

    <!-- Profile card -->
    <div class="card profile-card mb-20">
      <div class="profile-header flex-center gap-16">
        <div class="avatar-wrapper">
          <el-avatar :size="80" :src="user?.avatarUrl" icon="UserFilled" />
          <!-- @pick → useFileUpload composable 处理头像上传 -->
          <ImageUploader @pick="uploadAvatar">
            <el-button size="small" circle class="avatar-edit-btn" :loading="avatarUploading">
              <el-icon><Edit /></el-icon>
            </el-button>
          </ImageUploader>
        </div>
        <div class="profile-info">
          <h3>{{ user?.username || '用户' }}</h3>
          <p class="text-secondary">{{ user?.bio || '这个人很懒，什么也没留下' }}</p>
          <p class="text-secondary">{{ user?.location || '未设置地区' }} · {{ user?.email || '' }}</p>
        </div>
      </div>

      <!-- Edit form -->
      <el-divider />
      <el-form :model="editForm" label-width="80px" size="default">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="2" placeholder="介绍一下自己" maxlength="500" />
        </el-form-item>
        <el-form-item label="地区">
          <el-input v-model="editForm.location" placeholder="所在城市" maxlength="100" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-tooltip content="PATCH /api/users/me 接口开发中" placement="top">
            <el-button type="primary" disabled>保存修改 (接口开发中)</el-button>
          </el-tooltip>
        </el-form-item>
      </el-form>
    </div>

    <!-- My posts -->
    <div class="card">
      <h3 class="mb-16">我的帖子</h3>
      <el-tabs v-model="activeStatus" @tab-change="loadMyPosts">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="已发布" name="2" />
        <el-tab-pane label="草稿" name="0" />
        <el-tab-pane label="待审核" name="1" />
        <el-tab-pane label="已拒绝" name="3" />
      </el-tabs>

      <div v-if="postsLoading" class="flex-center" style="padding: 40px; justify-content: center">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
      </div>
      <div v-else-if="filteredPosts.length === 0" class="placeholder-section">
        暂无帖子
      </div>
      <div v-else>
        <div v-for="p in filteredPosts" :key="p.id" class="my-post-item flex-between">
          <div>
            <router-link :to="`/posts/${p.id}`" class="post-link">{{ p.title }}</router-link>
            <div class="text-secondary mt-8">
              <el-tag :type="statusTagType(p.status)" size="small">{{ statusLabel(p.status) }}</el-tag>
              · {{ p.categoryName }} · 浏览 {{ p.viewCount }} · 评论 {{ p.commentCount }}
            </div>
          </div>
          <div class="flex-center gap-8">
            <el-button v-if="p.status === 0 || p.status === 2 || p.status === 3" size="small" @click="$router.push(`/edit-post/${p.id}`)">编辑</el-button>
            <el-tooltip v-if="p.status === 2" content="精选申请接口开发中" placement="top">
              <el-button size="small" disabled>申请精选</el-button>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { getUserPostsApi } from '@/api/posts'
import { useFileUpload } from '@/composables/useFileUpload'
import ImageUploader from '@/components/ImageUploader.vue'
import { Edit, Loading } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const { user } = storeToRefs(authStore)

// ── Composable: 头像上传 ──
// 后端 uploadUserAvatar 全包: 删旧→传新→更新DB→返回URL
const { uploading: avatarUploading, upload: uploadAvatar } = useFileUpload({
  type: 'avatar',
  onSuccess(url) {
    authStore.updateUserInfo({ avatarUrl: url })
  },
})

const editForm = reactive({
  username: user.value?.username || '',
  bio: user.value?.bio || '',
  location: user.value?.location || '',
  gender: user.value?.gender || 0,
})

const allPosts = ref([])
const postsLoading = ref(false)
const activeStatus = ref('all')

const filteredPosts = computed(() => {
  if (activeStatus.value === 'all') return allPosts.value
  return allPosts.value.filter((p) => p.status === Number(activeStatus.value))
})

function statusLabel(s) {
  return { 0: '草稿', 1: '待审核', 2: '已发布', 3: '已拒绝', 4: '已删除' }[s] || '未知'
}

function statusTagType(s) {
  return { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: 'danger' }[s] || 'info'
}

async function loadMyPosts() {
  if (!user.value?.id) return
  postsLoading.value = true
  try {
    const res = await getUserPostsApi(user.value.id, { page: 0, size: 100 })
    allPosts.value = res.data?.results || res.data || []
  } catch (e) {
    console.error('加载我的帖子失败:', e)
  } finally {
    postsLoading.value = false
  }
}

onMounted(() => loadMyPosts())
</script>

<style scoped>
.personal-center {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
}

.profile-info h3 {
  font-size: 18px;
  margin-bottom: 4px;
}

.my-post-item {
  padding: 14px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.my-post-item:last-child {
  border-bottom: none;
}

.post-link {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.post-link:hover {
  color: #409eff;
}
</style>
