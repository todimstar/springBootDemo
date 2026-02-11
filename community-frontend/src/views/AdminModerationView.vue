<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const targetTypeFilter = ref('')

// 审核操作对话框
const actionDialogVisible = ref(false)
const currentItem = ref(null)
const actionForm = ref({ action: '', reason: '' })

const actionOptions = [
  { value: 'approve', label: '通过' },
  { value: 'reject', label: '驳回' },
  { value: 'takedown', label: '下架' },
  { value: 'shadow_ban', label: '影子发布' },
]

const targetTypeOptions = [
  { value: '', label: '全部' },
  { value: 'post', label: '帖子' },
  { value: 'comment', label: '评论' },
]

function getAuthHeaders() {
  return { Authorization: `Bearer ${authStore.token}` }
}

async function fetchQueue() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
    }
    if (targetTypeFilter.value) {
      params.targetType = targetTypeFilter.value
    }
    const response = await axios.get('/api/admin/moderation/queue', {
      params,
      headers: getAuthHeaders(),
    })
    const data = response.data.data
    tableData.value = data.results || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取审核队列失败:', error)
    ElMessage.error('获取审核队列失败')
  } finally {
    loading.value = false
  }
}

function handlePageChange(page) {
  currentPage.value = page
  fetchQueue()
}

function handleSizeChange(size) {
  pageSize.value = size
  currentPage.value = 1
  fetchQueue()
}

function handleFilterChange() {
  currentPage.value = 1
  fetchQueue()
}

function openActionDialog(row, action) {
  currentItem.value = row
  actionForm.value = { action, reason: '' }
  actionDialogVisible.value = true
}

async function submitAction() {
  if (!actionForm.value.reason.trim()) {
    ElMessage.warning('请填写审核理由')
    return
  }
  try {
    await axios.post(
      `/api/admin/moderation/${currentItem.value.id}/action`,
      {
        action: actionForm.value.action,
        reason: actionForm.value.reason,
      },
      { headers: getAuthHeaders() },
    )
    ElMessage.success('操作成功')
    actionDialogVisible.value = false
    fetchQueue()
  } catch (error) {
    console.error('审核操作失败:', error)
    ElMessage.error(error.response?.data?.message || '审核操作失败')
  }
}

function getActionLabel(code) {
  const opt = actionOptions.find((o) => o.value === code)
  return opt ? opt.label : code
}

function getRiskScoreType(score) {
  if (score >= 70) return 'danger'
  if (score >= 30) return 'warning'
  return 'success'
}

function formatTargetType(type) {
  return type === 'post' ? '帖子' : type === 'comment' ? '评论' : type
}

onMounted(() => {
  fetchQueue()
})
</script>

<template>
  <div class="moderation-container">
    <h2>审核队列</h2>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="targetTypeFilter"
        placeholder="内容类型"
        @change="handleFilterChange"
        style="width: 150px"
      >
        <el-option
          v-for="opt in targetTypeOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="contentSummary" label="内容摘要" min-width="200" show-overflow-tooltip />
      <el-table-column prop="authorName" label="作者" width="120" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ formatTargetType(row.targetType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="风险分" width="90" sortable>
        <template #default="{ row }">
          <el-tag :type="getRiskScoreType(row.riskScore)" size="small">
            {{ row.riskScore }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="hitRules" label="命中规则" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="提交时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="openActionDialog(row, 'approve')">
            通过
          </el-button>
          <el-button size="small" type="danger" @click="openActionDialog(row, 'reject')">
            驳回
          </el-button>
          <el-button size="small" type="warning" @click="openActionDialog(row, 'takedown')">
            下架
          </el-button>
          <el-button size="small" @click="openActionDialog(row, 'shadow_ban')">
            影子发布
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-bar">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 审核操作对话框 -->
    <el-dialog v-model="actionDialogVisible" title="审核操作" width="500px">
      <div v-if="currentItem" style="margin-bottom: 16px">
        <p><strong>内容：</strong>{{ currentItem.contentSummary }}</p>
        <p><strong>操作：</strong>{{ getActionLabel(actionForm.action) }}</p>
      </div>
      <el-input
        v-model="actionForm.reason"
        type="textarea"
        :rows="3"
        placeholder="请输入审核理由（必填）"
      />
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.moderation-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.filter-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
