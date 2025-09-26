<script setup>
// 这里是写你的 Vue 组件JS逻辑
import {ref,onMounted} from 'vue';// 1.导入Vue的核心功能 ref 和onMounted
import axios from 'axios';  //2.导入axios

// 用ref()创建应该响应式变量 posts,初始值是一个空数组
const posts = ref([]);

onMounted(async () =>{
    fetchPosts();
})

async function fetchPosts(){
    try{
        const response = await axios.get("/api/posts")

        var responseData = response.data;
        console.log('成功获取数据：', responseData);
        posts.value = responseData.data;    //因为返回格式Result是Result{code:0,message:'...',data:[...]}格式
        
    }catch(error){
        console.error('获取文章列表失败：', error);
        
    }
}

function addTestPost() {
  // 手动向 posts 数组的开头添加一个测试帖子
  // .value 是必须的！
  posts.value.unshift({
    id: Date.now(), // 用当前时间戳作为唯一的 key
    title: '这是一个测试帖子'
  });
}
</script>

<template>
    <!-- 这里写HTML结构 -->
    <div class="posts-list">
        <h1>帖子列表</h1>

        <!-- 添加一个测试按钮 -->
        <button @click="addTestPost">添加一个测试帖子</button>

        <!-- 用v-for 循环posts数组 -->
        <!-- 每个post对象，都创建应该 <dir> -->
        <!-- :key="post.id"是必须的用来帮助Vue高效更新列表 -->
        <div v-for="post in posts" :key="post.id" class="post-item">
            <h2>{{ post.title }}</h2>
        </div>

        <div v-if="posts.length === 0">
            正在加载中...
        </div>
    </div>
</template>

<style scoped>
/* 这里写你的CSS样式 */
.posts-list {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}
.post-item{
    border-bottom: 1px solid #eee;
    padding: 15px 0;
}
.post-item h2{
    margin: 0;
    font-size: 1.2em;
}
</style>