<template>
  <router-link :to="`/posts/${post.id}`" class="post-card card">
    <div class="post-card-body">
      <div class="post-content">
        <h3 class="post-title text-ellipsis">{{ post.title }}</h3>
        <p class="post-summary">{{ post.summary || '暂无摘要' }}</p>
        <div class="post-meta flex-center gap-12">
          <div class="author-info flex-center gap-8">
            <el-avatar :size="22" :src="post.userAvatarUrl" icon="UserFilled" />
            <span>{{ post.username || '未知用户' }}</span>
          </div>
          <el-tag size="small" effect="plain">{{ post.categoryName || '未分类' }}</el-tag>
          <span class="meta-item">
            <el-icon><View /></el-icon> {{ post.viewCount || 0 }}
          </span>
          <span class="meta-item">
            <el-icon><ChatDotRound /></el-icon> {{ post.commentCount || 0 }}
          </span>
          <span class="meta-item time">{{ formatTime(post.createTime) }}</span>
        </div>
      </div>
      <div v-if="post.coverImage" class="post-cover">
        <img :src="post.coverImage" alt="封面" />
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { View, ChatDotRound } from '@element-plus/icons-vue'

defineProps({
  post: { type: Object, required: true },
})

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = (now - d) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)} 小时前`
  if (diff < 604800) return `${Math.floor(diff / 86400)} 天前`
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.post-card {
  display: block;
  text-decoration: none;
  color: inherit;
  transition: box-shadow 0.2s;
  cursor: pointer;
}

.post-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.post-card-body {
  display: flex;
  gap: 16px;
}

.post-content {
  flex: 1;
  min-width: 0;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  white-space: normal;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-summary {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

.author-info {
  font-size: 13px;
  color: #606266;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.post-cover {
  width: 160px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
