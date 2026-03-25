<template>
  <div class="category-manage">
    <div class="flex-between mb-20">
      <h2 style="font-size: 20px; font-weight: 600">分区管理</h2>
      <el-button type="primary" @click="openDialog()">新建分区</el-button>
    </div>

    <el-table :data="categories" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="postCount" label="帖子数" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="isActive" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
            {{ row.isActive ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button
            size="small"
            :type="row.isActive ? 'warning' : 'success'"
            @click="toggleActive(row)"
          >
            {{ row.isActive ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEditMode ? '编辑分区' : '新建分区'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" maxlength="200" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getCategoriesApi,
  createCategoryApi,
  updateCategoryApi,
  enableCategoryApi,
  disableCategoryApi,
  deleteCategoryApi,
} from '@/api/categories'
import { ElMessage, ElMessageBox } from 'element-plus'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEditMode = ref(false)
const submitting = ref(false)

const form = reactive({ id: null, name: '', description: '', sortOrder: 0 })

async function loadCategories() {
  loading.value = true
  try {
    const res = await getCategoriesApi()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    isEditMode.value = true
    Object.assign(form, { id: row.id, name: row.name, description: row.description || '', sortOrder: row.sortOrder || 0 })
  } else {
    isEditMode.value = false
    Object.assign(form, { id: null, name: '', description: '', sortOrder: 0 })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分区名称')
    return
  }
  submitting.value = true
  try {
    if (isEditMode.value) {
      await updateCategoryApi(form)
      ElMessage.success('分区已更新')
    } else {
      await createCategoryApi(form)
      ElMessage.success('分区已创建')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch (e) {
    // handled
  } finally {
    submitting.value = false
  }
}

async function toggleActive(row) {
  try {
    if (row.isActive) {
      await disableCategoryApi(row.id)
      ElMessage.success('分区已禁用')
    } else {
      await enableCategoryApi(row.id)
      ElMessage.success('分区已启用')
    }
    await loadCategories()
  } catch (e) {
    // handled
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除分区「${row.name}」吗？`, '确认', { type: 'warning' })
  try {
    await deleteCategoryApi(row.id)
    ElMessage.success('分区已删除')
    await loadCategories()
  } catch (e) {
    // handled
  }
}

onMounted(() => loadCategories())
</script>
