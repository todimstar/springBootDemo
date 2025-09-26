
<template>
  <div class="create-post-view">
    <h1>创建新帖子</h1>
    <div class="form-group">
      <label for="title">标题</label>
      <input ref="titleInput" v-model="title" type="text" id="title" placeholder="请输入帖子标题">
    </div>
    <div class="form-group">
      <label for="content">内容</label>
      <textarea ref="contentInput" v-model="content" id="content" rows="10" placeholder="请输入帖子内容"></textarea>
    </div>
    <button @click="handleSubmit">发布帖子</button>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import router from '@/router';

const authStore = useAuthStore();

const title = ref('');
const content = ref('');

const titleInput = ref(null);
const contentInput = ref(null);

const handleSubmit = async () => {
  //查空
  //鉴权
  //发送token和内容
  //后续清理和跳转

  if(!title.value){
    //focus到title输入框，然后提示不能为空
    titleInput.value.focus();
    alert('标题不能为空');
    return;
  }
  if(!content.value){
    contentInput.value.focus();
    alert('内容不能为空');
    return;
  }
  //鉴权
  if(!authStore.isAuthenticated){
    alert('请先登录');
    router.push('/login');
    return;
  }
  //发送请求
  try{
    const response = await axios.post('/api/posts',{
      title:title.value,
      content:content.value
    },{
      headers:{
        'Authorization':`Bearer ${authStore.token}`
      }
    });

    //不清理了，万一网络错误啥的，保留一下内容还在，但是要跳转
    // title.value = '';
    // content.value = '';
    alert('发布成功');
    router.push({name:'/posts/:id',params:{id:response.data.data.id}}); //跳转到新发的帖子详情页，原来动态路由是这样写的吗？这里会等异步的response吗？
  } catch (error) {
    console.error('发布失败:', error);
    alert('发布失败，请稍后再试');
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
input, textarea {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
}
input:focus, textarea:focus {
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
