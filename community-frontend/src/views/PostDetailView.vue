<template>
  <div class="post-detail-container">
    <div v-if="loading" class="loading">正在加载...</div>
    <div v-if="error" class="error-message">{{ error }}</div>
    <article v-if="post" class="post">
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-meta">
        <span class="author">作者：{{ post.author }}</span>
        <span class="created-at">发布于：{{ new Date(post.createdAt).toLocaleDateString() }}</span>
      </div>
      <div class="post-content" v-html="post.content"></div>
    </article>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
// 假设你有一个服务来获取帖子数据
// import { getPostById } from '@/services/postService'

export default {
  setup() {
    const route = useRoute()
    const post = ref(null)
    const loading = ref(true)
    const error = ref(null)

    const fetchPost = async () => {
      try {
        const postId = route.params.id    //从路由参数获取帖子ID

        // 模拟 API 调用
        const response = await new axios.get('/api/posts/' + postId);
        post.value = response
      } catch (err) {
        error.value = '加载帖子失败，请稍后再试。'
        console.error(err)
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      fetchPost()
    })

    return {
      post,
      loading,
      error
    }
  }
}
</script>

<style lang="scss" scoped>
.post-detail-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px 40px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.loading,
.error-message {
  text-align: center;
  padding: 50px 0;
  font-size: 1.2rem;
  color: #6c757d;
}

.error-message {
  color: #dc3545;
}

.post {
  .post-title {
    font-size: 2.8rem;
    font-weight: 700;
    color: #212529;
    margin-bottom: 1rem;
    line-height: 1.2;
  }

  .post-meta {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    margin-bottom: 2.5rem;
    font-size: 0.9rem;
    color: #6c757d;
    border-bottom: 1px solid #e9ecef;
    padding-bottom: 1.5rem;
  }

  .post-content {
    line-height: 1.8;
    font-size: 1.1rem;
    color: #343a40;

    // 样式穿透，用于 v-html 渲染的内容
    :deep(h2) {
      font-size: 1.8rem;
      font-weight: 600;
      margin-top: 2.5rem;
      margin-bottom: 1rem;
      padding-bottom: 0.5rem;
      border-bottom: 1px solid #e9ecef;
    }

    :deep(p) {
      margin-bottom: 1.2rem;
    }

    :deep(img) {
      max-width: 100%;
      height: auto;
      border-radius: 8px;
      margin: 1.5rem 0;
    }

    :deep(ul),
    :deep(ol) {
      padding-left: 2rem;
      margin-bottom: 1.2rem;
    }

    :deep(li) {
      margin-bottom: 0.5rem;
    }

    :deep(strong) {
      font-weight: 600;
    }

    :deep(em) {
      font-style: italic;
    }

    :deep(a) {
      color: #007bff;
      text-decoration: none;
      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
