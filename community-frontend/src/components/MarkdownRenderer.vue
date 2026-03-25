<template>
  <div class="markdown-body" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const props = defineProps({
  content: { type: String, default: '' },
})

const renderedHtml = computed(() => {
  if (!props.content) return ''
  const raw = marked(props.content)
  return DOMPurify.sanitize(raw)
})
</script>
