<template>
  <div class="create-post-page">
    <h2 class="page-title">{{ isEdit ? '编辑帖子' : '创建帖子' }}</h2>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入帖子标题" maxlength="50" show-word-limit />
      </el-form-item>

      <div class="form-row">
        <el-form-item label="分区" prop="categoryId" style="flex: 1">
          <el-select v-model="form.categoryId" placeholder="选择分区" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="标签" style="flex: 1">
          <el-select multiple disabled placeholder="标签功能开发中..." style="width: 100%">
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="可选，不填将自动截取正文前300字" maxlength="300" show-word-limit />
      </el-form-item>

      <el-form-item label="封面图">
        <div class="cover-upload flex-center gap-12">
          <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
          <!-- @pick → useFileUpload composable 处理上传全流程 -->
          <ImageUploader @pick="uploadCover">
            <el-button :loading="coverUploading">{{ form.coverImage ? '更换封面' : '上传封面' }}</el-button>
          </ImageUploader>
          <el-button v-if="form.coverImage" text type="danger" @click="handleRemoveCover">移除</el-button>
        </div>
      </el-form-item>

      <el-form-item label="正文" prop="content">
        <div class="editor-wrapper">
          <div class="editor-toolbar flex-between">
            <span class="text-secondary">支持 Markdown 语法</span>
            <el-switch v-model="showPreview" active-text="预览" inactive-text="编辑" />
          </div>
          <div class="editor-body">
            <el-input
              v-if="!showPreview"
              v-model="form.content"
              type="textarea"
              :rows="18"
              placeholder="请输入帖子正文..."
              maxlength="15000"
              show-word-limit
            />
            <div v-else class="preview-area card">
              <MarkdownRenderer :content="form.content" />
            </div>
          </div>
        </div>
      </el-form-item>

      <div class="form-actions flex-center gap-12">
        <el-button @click="handleSave(0)" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="handleSave(2)" :loading="publishing">发布</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useCategoryStore } from '@/stores/category'
import { createPostApi, updatePostApi, getPostApi } from '@/api/posts'
import { deleteFileApi } from '@/api/upload'
import { useFileUpload } from '@/composables/useFileUpload'
import ImageUploader from '@/components/ImageUploader.vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const categoryStore = useCategoryStore()
const { categories } = storeToRefs(categoryStore)

const isEdit = !!route.params.id
const formRef = ref()
const showPreview = ref(false)
const saving = ref(false)
const publishing = ref(false)

// 记录当前封面在 MinIO 中的 objectName，用于替换/移除时清理旧文件
const coverObjectName = ref('')

// ── Composable: 封面图上传 ──
// 类比后端: View 调用 Service，Service 调用 Mapper
// 这里: View 调用 useFileUpload，它内部调用 uploadFileApi → getFileUrlApi → deleteFileApi
const { uploading: coverUploading, upload: uploadCover } = useFileUpload({
  type: 'cover',
  onSuccess(url, objectName) {
    form.coverImage = url
    coverObjectName.value = objectName
  },
  getPreviousObjectName: () => coverObjectName.value,
})

/** 移除封面：同时清理 MinIO 中的文件 */
function handleRemoveCover() {
  if (coverObjectName.value) {
    deleteFileApi(coverObjectName.value).catch(() => {})
  }
  form.coverImage = ''
  coverObjectName.value = ''
}

const form = reactive({
  title: '',
  content: '',
  summary: '',
  categoryId: null,
  coverImage: '',
  status: 0,
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分区', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }],
}

async function handleSave(status) {
  await formRef.value.validate()
  const data = { ...form, status }
  if (!data.summary && data.content) {
    data.summary = data.content.replace(/[#*`>\-\[\]()!]/g, '').substring(0, 300)
  }

  const loadingRef = status === 0 ? saving : publishing
  loadingRef.value = true
  try {
    if (isEdit) {
      await updatePostApi(route.params.id, data)
      ElMessage.success('帖子已更新')
    } else {
      const res = await createPostApi(data)
      ElMessage.success(status === 0 ? '草稿已保存' : '帖子已发布')
      router.push(`/posts/${res.data.id}`)
      return
    }
    router.push(`/posts/${route.params.id}`)
  } catch (e) {
    // handled in interceptor
  } finally {
    loadingRef.value = false
  }
}

onMounted(async () => {
  await categoryStore.fetchCategories()
  if (isEdit) {
    try {
      const res = await getPostApi(route.params.id)
      const post = res.data
      Object.assign(form, {
        title: post.title,
        content: post.content,
        summary: post.summary || '',
        categoryId: post.categoryId,
        coverImage: post.coverImage || '',
        status: post.status,
      })
    } catch (e) {
      ElMessage.error('帖子不存在')
      router.push('/')
    }
  }
})
</script>

<style scoped>
.create-post-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.cover-upload {
  flex-wrap: wrap;
}

.cover-preview {
  width: 160px;
  height: 90px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--color-border-light);
}

.editor-wrapper {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  overflow: hidden;
}

.editor-toolbar {
  padding: 8px 12px;
  background: #fafafa;
  border-bottom: 1px solid var(--color-border-light);
}

.preview-area {
  min-height: 400px;
  padding: 16px;
  border: none;
  border-radius: 0;
}

.form-actions {
  margin-top: 24px;
  justify-content: center;
}
</style>
