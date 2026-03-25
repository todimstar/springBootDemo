<template>
  <el-upload
    :auto-upload="false"
    :show-file-list="false"
    :on-change="handleChange"
    accept="image/*"
  >
    <slot>
      <el-button type="primary" plain>选择图片</el-button>
    </slot>
  </el-upload>
</template>

<script setup>
/**
 * 纯文件选择器组件 —— 只负责选文件 + 校验大小/类型
 * 实际上传 API 调用由父组件处理，保证调用链路可追溯
 *
 * 用法示例:
 *   <ImageUploader @pick="handleUpload">
 *     <el-button>上传</el-button>
 *   </ImageUploader>
 *
 *   async function handleUpload(file) {
 *     const formData = new FormData()
 *     formData.append('file', file)
 *     const res = await uploadFileApi(formData)  // ← 这里能直接看到调了哪个接口
 *   }
 */
import { ElMessage } from 'element-plus'

const props = defineProps({
  maxSize: { type: Number, default: 5 }, // MB
})

const emit = defineEmits(['pick'])

function handleChange(uploadFile) {
  const file = uploadFile.raw
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (file.size / 1024 / 1024 > props.maxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return
  }

  emit('pick', file)
}
</script>
