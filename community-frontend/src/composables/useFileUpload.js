/**
 * useFileUpload —— 文件上传 Composable
 *
 * 类比后端的 Service 层：
 *   后端: Controller → Service → Mapper
 *   前端: View → Composable → API
 *
 * 用法:
 *   const { uploading, upload } = useFileUpload({
 *     type: 'cover',                          // 'avatar' | 'cover'
 *     onSuccess(url) { form.coverImage = url },
 *     getPreviousObjectName() { return coverObjectName.value },
 *   })
 *
 *   // template 里: <ImageUploader @pick="upload" />
 *
 * 调试: 在这个文件里打断点就能看到完整上传流程
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadAvatarApi } from '@/api/auth'
import { uploadFileApi, getFileUrlApi, deleteFileApi } from '@/api/upload'

export function useFileUpload(options = {}) {
  const { type = 'cover', onSuccess, getPreviousObjectName } = options
  const uploading = ref(false)

  async function upload(file) {
    uploading.value = true
    try {
      const formData = new FormData()
      formData.append('file', file)

      let newUrl = ''
      let newObjectName = ''

      if (type === 'avatar') {
        // ── 头像上传 ──────────────────────────────────
        // POST /api/auth/upload/userAvatar
        // 后端全包: 删旧文件 → 上传新文件 → 更新 DB → 返回完整 URL
        const res = await uploadAvatarApi(formData)
        newUrl = res.data

      } else {
        // ── 通用文件上传(封面图等) ───────────────────
        // 步骤①: POST /upload/upload → 返回 objectName (MinIO 内部路径)
        const uploadRes = await uploadFileApi(formData)
        newObjectName = uploadRes.data

        // 步骤②: GET /upload/getUrl?objectName=xxx → objectName 转完整可访问 URL
        const urlRes = await getFileUrlApi(newObjectName)
        newUrl = urlRes.data

        // 步骤③: DELETE /upload/delete → 清理旧文件(非阻塞)
        const oldName = getPreviousObjectName?.()
        if (oldName) {
          deleteFileApi(oldName).catch(() => {})
        }
      }

      onSuccess?.(newUrl, newObjectName)
      ElMessage.success('上传成功')
    } catch (e) {
      ElMessage.error('上传失败')
    } finally {
      uploading.value = false
    }
  }

  return { uploading, upload }
}
