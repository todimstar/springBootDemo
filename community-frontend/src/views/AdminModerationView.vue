<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const authStore = useAuthStore()

// Tab 控制
const activeTab = ref('queue')

// ==================== 审核队列相关 ====================
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

// 详情抽屉
const detailDrawerVisible = ref(false)
const detailItem = ref(null)
const auditLogs = ref([])
const detailLoading = ref(false)

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

// 查看详情（并行获取详情和审计日志）
async function viewDetail(row) {
  detailDrawerVisible.value = true
  detailLoading.value = true
  try {
    // 并行请求详情和审计日志
    const [detailRes, logsRes] = await Promise.all([
      axios.get(`/api/admin/moderation/${row.id}`, { headers: getAuthHeaders() }),
      axios.get(`/api/admin/moderation/${row.id}/logs`, { headers: getAuthHeaders() })
    ])
    detailItem.value = detailRes.data.data
    auditLogs.value = logsRes.data.data || []
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ==================== 规则管理相关 ====================
const rulesLoading = ref(false)
const rulesData = ref([])
const rulesTotal = ref(0)
const rulesPage = ref(1)
const rulesPageSize = ref(10)
const ruleTypeFilter = ref('')
const ruleEnabledFilter = ref(null)

// 规则表单对话框
const ruleDialogVisible = ref(false)
const isEditMode = ref(false)
const editingRuleId = ref(null)
const ruleForm = ref({
  ruleType: 'keyword',
  pattern: '',
  weightScore: 10,
  enabled: true,
  description: ''
})

const ruleTypeOptions = [
  { value: '', label: '全部' },
  { value: 'keyword', label: '关键词' },
  { value: 'regex', label: '正则表达式' },
  { value: 'blacklist', label: '黑名单' },
]

async function fetchRules() {
  rulesLoading.value = true
  try {
    const params = { page: rulesPage.value, pageSize: rulesPageSize.value }
    if (ruleTypeFilter.value) params.ruleType = ruleTypeFilter.value
    if (ruleEnabledFilter.value !== null) params.enabled = ruleEnabledFilter.value
    const res = await axios.get('/api/admin/moderation/rules', {
      params, headers: getAuthHeaders()
    })
    rulesData.value = res.data.data.results || []
    rulesTotal.value = res.data.data.total || 0
  } catch (error) {
    console.error('获取规则列表失败:', error)
    ElMessage.error('获取规则列表失败')
  } finally {
    rulesLoading.value = false
  }
}

function openCreateRule() {
  isEditMode.value = false
  ruleForm.value = { ruleType: 'keyword', pattern: '', weightScore: 10, enabled: true, description: '' }
  ruleDialogVisible.value = true
}

function openEditRule(row) {
  isEditMode.value = true
  editingRuleId.value = row.id
  ruleForm.value = { ...row }
  ruleDialogVisible.value = true
}

async function submitRule() {
  if (!ruleForm.value.pattern.trim()) {
    ElMessage.warning('规则模式不能为空')
    return
  }
  try {
    if (isEditMode.value) {
      await axios.put(`/api/admin/moderation/rules/${editingRuleId.value}`,
        ruleForm.value, { headers: getAuthHeaders() })
      ElMessage.success('规则已更新')
    } else {
      await axios.post('/api/admin/moderation/rules',
        ruleForm.value, { headers: getAuthHeaders() })
      ElMessage.success('规则已创建')
    }
    ruleDialogVisible.value = false
    fetchRules()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

async function deleteRule(row) {
  try {
    await ElMessageBox.confirm(`确定要删除规则 "${row.pattern}" 吗？`, '确认删除', {
      type: 'warning'
    })
    await axios.delete(`/api/admin/moderation/rules/${row.id}`, { headers: getAuthHeaders() })
    ElMessage.success('规则已删除')
    fetchRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

function handleRulesPageChange(page) {
  rulesPage.value = page
  fetchRules()
}

function handleRulesSizeChange(size) {
  rulesPageSize.value = size
  rulesPage.value = 1
  fetchRules()
}

function handleRulesFilterChange() {
  rulesPage.value = 1
  fetchRules()
}

function getRuleTypeTag(type) {
  const map = {
    keyword: 'primary',
    regex: 'success',
    blacklist: 'danger'
  }
  return map[type] || 'info'
}

// ==================== 统计面板相关 ====================
const statsLoading = ref(false)
const statsData = ref([])

async function fetchStats() {
  statsLoading.value = true
  try {
    const res = await axios.get('/api/admin/moderation/rules/stats', { headers: getAuthHeaders() })
    statsData.value = (res.data.data || []).sort((a, b) => b.hitCount - a.hitCount)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// Tab 切换时的懒加载
function handleTabChange(tabName) {
  if (tabName === 'queue' && tableData.value.length === 0) {
    fetchQueue()
  }
  if (tabName === 'rules' && rulesData.value.length === 0) {
    fetchRules()
  }
  if (tabName === 'stats') {
    fetchStats() // 统计每次切换都刷新
  }
}

onMounted(() => {
  fetchQueue()
})
</script>

<template>
  <div class="moderation-container">
    <h2>审核管理</h2>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 审核队列 Tab -->
      <el-tab-pane label="审核队列" name="queue">
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
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="info" @click="viewDetail(row)">
            详情
          </el-button>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="审核详情" size="600px">
      <div v-loading="detailLoading">
        <!-- 内容摘要区 -->
        <el-descriptions :column="1" border v-if="detailItem">
          <el-descriptions-item label="ID">{{ detailItem.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ formatTargetType(detailItem.targetType) }}</el-descriptions-item>
          <el-descriptions-item label="作者">{{ detailItem.authorName }}</el-descriptions-item>
          <el-descriptions-item label="内容摘要">{{ detailItem.contentSummary }}</el-descriptions-item>
          <el-descriptions-item label="风险分">
            <el-tag :type="getRiskScoreType(detailItem.riskScore)" size="small">
              {{ detailItem.riskScore }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="命中规则">{{ detailItem.hitRules }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detailItem.status }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailItem.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 审计轨迹 -->
        <h4 style="margin: 24px 0 12px">审计轨迹</h4>
        <el-empty v-if="auditLogs.length === 0" description="暂无审计记录" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="log in auditLogs" :key="log.id"
            :timestamp="log.createdAt" placement="top">
            <el-card>
              <p><strong>{{ log.actionType }}</strong> by {{ log.operatorName }}</p>
              <p v-if="log.reason">理由：{{ log.reason }}</p>
              <p v-if="log.hitRules">命中规则：{{ log.hitRules }}</p>
              <p v-if="log.riskScore !== undefined">风险分：{{ log.riskScore }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
      </el-tab-pane>

      <!-- 规则管理 Tab -->
      <el-tab-pane label="规则管理" name="rules">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <el-select
            v-model="ruleTypeFilter"
            placeholder="规则类型"
            @change="handleRulesFilterChange"
            style="width: 150px"
          >
            <el-option
              v-for="opt in ruleTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-select
            v-model="ruleEnabledFilter"
            placeholder="启用状态"
            @change="handleRulesFilterChange"
            style="width: 150px"
            clearable
          >
            <el-option label="已启用" :value="true" />
            <el-option label="已禁用" :value="false" />
          </el-select>
          <el-button type="primary" @click="openCreateRule">新增规则</el-button>
        </div>

        <!-- 规则表格 -->
        <el-table :data="rulesData" v-loading="rulesLoading" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getRuleTypeTag(row.ruleType)" size="small">
                {{ row.ruleType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pattern" label="规则模式" min-width="200" show-overflow-tooltip />
          <el-table-column prop="weightScore" label="权重分" width="90" sortable />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="创建时间" width="170" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openEditRule(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="deleteRule(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="rulesPage"
            v-model:page-size="rulesPageSize"
            :page-sizes="[10, 20, 50]"
            :total="rulesTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="handleRulesPageChange"
            @size-change="handleRulesSizeChange"
          />
        </div>

        <!-- 规则表单对话框 -->
        <el-dialog
          v-model="ruleDialogVisible"
          :title="isEditMode ? '编辑规则' : '新增规则'"
          width="600px"
        >
          <el-form :model="ruleForm" label-width="100px">
            <el-form-item label="规则类型">
              <el-select v-model="ruleForm.ruleType" style="width: 100%">
                <el-option label="关键词" value="keyword" />
                <el-option label="正则表达式" value="regex" />
                <el-option label="黑名单" value="blacklist" />
              </el-select>
            </el-form-item>
            <el-form-item label="规则模式">
              <el-input v-model="ruleForm.pattern" placeholder="请输入规则模式" />
            </el-form-item>
            <el-form-item label="权重分">
              <el-input-number
                v-model="ruleForm.weightScore"
                :min="0"
                :max="100"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="启用状态">
              <el-switch v-model="ruleForm.enabled" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input
                v-model="ruleForm.description"
                type="textarea"
                :rows="3"
                placeholder="请输入规则描述（可选）"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="ruleDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitRule">确认</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 统计面板 Tab -->
      <el-tab-pane label="统计面板" name="stats">
        <div style="margin-bottom: 16px">
          <el-button type="primary" @click="fetchStats" :loading="statsLoading">
            刷新统计
          </el-button>
        </div>

        <!-- 统计表格 -->
        <el-table :data="statsData" v-loading="statsLoading" border stripe style="width: 100%">
          <el-table-column prop="ruleId" label="规则ID" width="80" />
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getRuleTypeTag(row.ruleType)" size="small">
                {{ row.ruleType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="pattern" label="规则模式" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="hitCount" label="命中次数" width="120" sortable>
            <template #default="{ row }">
              <el-tag :type="row.hitCount > 0 ? 'danger' : 'info'" size="small">
                {{ row.hitCount }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastHitAt" label="最后命中时间" width="170">
            <template #default="{ row }">
              {{ row.lastHitAt || '从未命中' }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        </el-table>

        <el-empty v-if="!statsLoading && statsData.length === 0" description="暂无统计数据" />
      </el-tab-pane>
    </el-tabs>
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
