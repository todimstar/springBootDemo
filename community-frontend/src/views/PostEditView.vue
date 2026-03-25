<template>
  <PostCreateInner />
</template>

<script setup>
// 编辑帖子复用 PostCreateView 的逻辑（通过 route.params.id 判断）
import PostCreateInner from '@/views/PostCreateView.vue'
</script>
