
<template>
  <div class="create-post-view">
    <h1>创建新帖子</h1>
    <div class="form-group">
      <label for="title">标题</label>
      <input ref="titleInput" v-model="title" type="text" id="title" placeholder="请输入帖子标题">
    </div>
    <div class="form-group">
      <label for="summary">摘要 <span class="char-count">{{ summary.length }}/300</span></label>
      <textarea v-model="summary" id="summary" rows="3" maxlength="300" placeholder="请输入帖子摘要（用于列表展示，最多300字）"></textarea>
    </div>
    <div class="form-group">
      <label for="category">分区</label>
      <select v-model="selectedCategoryId" id="category">
        <option :value="null" disabled>请选择分区</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
          {{ cat.name }}
        </option>
      </select>
    </div>
    <div class="form-group">
      <label for="content">内容</label>
      <textarea ref="contentInput" v-model="content" id="content" rows="10" placeholder="请输入帖子内容"></textarea>
    </div>
    <button @click="handleSubmit">发布帖子</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import router from '@/router';
import { ElMessage } from 'element-plus';

const authStore = useAuthStore();

const title = ref('');
const content = ref('');
const summary = ref('');
const selectedCategoryId = ref(null);
const categories = ref([]);

const titleInput = ref(null);
const contentInput = ref(null);

// 获取分区列表
onMounted(async () => {
  try {
    const response = await axios.get('/api/categories');
    categories.value = response.data.data || [];
  } catch (error) {
    console.error('获取分区列表失败:', error);
    ElMessage.error('获取分区列表失败');
  }
});

const handleSubmit = async () => {
  //查空
  //鉴权
  //发送token和内容
  //后续清理和跳转

  if(!title.value){
    //focus到title输入框，然后提示不能为空
    titleInput.value.focus();
    ElMessage.warning('标题不能为空');
    return;
  }
  if(!content.value){
    contentInput.value.focus();
    ElMessage.warning('内容不能为空');
    return;
  }
  if(!summary.value){
    ElMessage.warning('摘要不能为空');
    return;
  }
  if(!selectedCategoryId.value){
    ElMessage.warning('请选择分区');
    return;
  }
  //鉴权
  if(!authStore.isAuthenticated){
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  //发送请求
  try{
    const response = await axios.post('/api/posts',{
      title: title.value,
      content: content.value,
      summary: summary.value,
      categoryId: selectedCategoryId.value
    },{
      headers:{
        'Authorization':`Bearer ${authStore.token}`
      }
    });

    // 根据审核状态显示不同提示
    const postStatus = response.data.data.status;
    if (postStatus === 1) {
      // PENDING_REVIEW
      ElMessage.warning('帖子已提交，正在等待审核，通过后将自动发布');
    } else if (postStatus === 2) {
      // PUBLISHED
      ElMessage.success('帖子发布成功！');
    } else if (postStatus === 3) {
      // REJECTED
      ElMessage.error('帖子未通过审核，请修改后重新提交');
    } else {
      ElMessage.success('帖子已提交');
    }

    // 修复路由跳转 bug：name 应该是 'post-detail'，不是 '/posts/:id'
    router.push({name: 'post-detail', params: {id: response.data.data.id}});
  } catch (error) {
    console.error('发布失败:', error);
    ElMessage.error(error.response?.data?.message || '发布失败，请稍后再试');
  }

};
</script>

<style scoped>
.create-post-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 1.5rem;
}
label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
}
.char-count {
  float: right;
  font-size: 0.9rem;
  color: #999;
  font-weight: normal;
}
input, textarea, select {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
}
input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: hsla(160, 100%, 37%, 1);
  box-shadow: 0 0 0 3px hsla(160, 100%, 37%, 0.2);
}
button {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 8px;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}
button:hover {
  background-color: hsla(160, 100%, 30%, 1);
}
</style>
