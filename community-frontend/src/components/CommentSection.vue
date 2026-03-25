<template>
  <div class="comment-section">
    <h4 class="section-title">评论 ({{ comments.length }})</h4>

    <!-- 评论输入 -->
    <div v-if="isAuthenticated" class="comment-input mb-16">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="1000"
        show-word-limit
      />
      <div style="margin-top: 8px; text-align: right">
        <el-button type="primary" :loading="submitting" :disabled="!newComment.trim()" @click="submitComment">
          发表评论
        </el-button>
      </div>
    </div>
    <el-alert v-else title="请先登录后再发表评论" type="info" :closable="false" show-icon class="mb-16" />

    <!-- 评论列表 -->
    <div v-if="loading" class="flex-center" style="padding: 40px; justify-content: center">
      <el-icon class="is-loading" :size="24"><Loading /></el-icon>
    </div>
    <div v-else-if="comments.length === 0" class="placeholder-section">
      暂无评论，来抢沙发吧
    </div>
    <div v-else class="comment-list">
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <el-avatar :size="32" icon="UserFilled" />
        <div class="comment-body">
          <div class="comment-header flex-between">
            <span class="comment-author">用户 #{{ c.userId }}</span>
            <div class="flex-center gap-8">
              <span class="text-secondary">{{ formatTime(c.createTime) }}</span>
              <el-button
                v-if="currentUserId && currentUserId === c.userId"
                text
                size="small"
                type="danger"
                @click="handleDelete(c.id)"
              >
                删除
              </el-button>
            </div>
          </div>
          <p class="comment-content">{{ c.content }}</p>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" style="text-align: center; margin-top: 16px">
      <el-button @click="loadMore" :loading="loading">加载更多</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { getCommentsApi, createCommentApi, deleteCommentApi } from '@/api/comments'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  postId: { type: [Number, String], required: true },
})

const authStore = useAuthStore()
const { isAuthenticated } = storeToRefs(authStore)
const currentUserId = ref(authStore.user?.id || null)

const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const submitting = ref(false)
const page = ref(1)
const hasMore = ref(false)

async function loadComments() {
  loading.value = true
  try {
    const res = await getCommentsApi(props.postId, { page: page.value, size: 10 })
    const list = res.data || []
    if (page.value === 1) {
      comments.value = list
    } else {
      comments.value.push(...list)
    }
    hasMore.value = list.length >= 10
  } catch (e) {
    console.error('加载评论失败:', e)
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value++
  loadComments()
}

async function submitComment() {
  if (!newComment.value.trim()) return
  submitting.value = true
  try {
    await createCommentApi(props.postId, { content: newComment.value })
    newComment.value = ''
    page.value = 1
    await loadComments()
    ElMessage.success('评论发表成功')
  } catch (e) {
    console.error('评论失败:', e)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(commentId) {
  await ElMessageBox.confirm('确定删除这条评论吗？', '确认', { type: 'warning' })
  try {
    await deleteCommentApi(commentId)
    comments.value = comments.value.filter((c) => c.id !== commentId)
    ElMessage.success('评论已删除')
  } catch (e) {
    console.error('删除评论失败:', e)
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => loadComments())
</script>

<style scoped>
.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-author {
  font-weight: 600;
  font-size: 13px;
}

.comment-content {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
</style>
