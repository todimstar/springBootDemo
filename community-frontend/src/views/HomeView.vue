<script setup>
// 1. <script setup>
// 这是 Vue 3 的组合式 API (Composition API) 的推荐语法。
// "setup" 是一个特殊的属性，它告诉 Vue 这个 <script> 块中的代码是组件的设置和逻辑部分。

import { onMounted, ref } from 'vue'

const count = ref(1)
const change = ref(1)

// 全部帖子（从后端获取或本地模拟）
const allPosts = ref([])

// 当前随机抽取结果
let posts = ref([])

function add() {
  if (allPosts.value.length === 0) return
  count.value = Math.min(count.value + change.value, allPosts.value.length)
  draw()
}
function sub() {
  count.value = Math.max(count.value - change.value, 1)
  draw()
}

function sampleWithoutReplacement(arr, n) {
  const len = arr.length
  if (n <= 0) return []
  if (n >= len) return arr.slice()

  const copy = arr.slice()

  // 只把后面 n 个位置“洗出来”
  for (let i = len - 1; i >= len - n; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[copy[i], copy[j]] = [copy[j], copy[i]]
  }
  return copy.slice(len - n)
}

function draw() {
  posts.value = sampleWithoutReplacement(allPosts.value, count.value)
}

// 模拟获取帖子（替换为你的真实接口）
async function fetchPosts() {
  // 例：const res = await fetch('/api/posts')
  // const data = await res.json()
  // allPosts.value = data
  try {
    const res = await fetch('/api/posts/allTitles')
    const { data } = await res.json()
    allPosts.value = data
    console.log(allPosts.value)

    // 修正 count 不超过总量
    count.value = Math.min(count.value, allPosts.value.length || 1)
    draw()
  } catch (err) {
    console.error('获取帖子失败:', err)
  }
}

onMounted(fetchPosts)
</script>

<template>
  <div class="page-container">
    <div class="home-view">
      <h1>欢迎!来到我的社区！</h1>
      <p>
        这是一个使用 Vue.js 构建的简单社区应用首页。 你可以点击页面顶部的导航链接
        <router-link to="/posts">帖子列表</router-link>
        来查看所有帖子。
      </p>
    </div>

    <div class="main-content">
      <!-- 控制区域 -->
      <div class="control-panel card">
        <h2>抽取设置</h2>
        <div class="control-group">
          <label for="draw-count">抽取数量：</label>
          <span id="draw-count" class="count-display">{{ count }}</span>
        </div>
        <div class="control-group">
          <label for="change-step">调整步长：</label>
          <select id="change-step" v-model.number="change">
            <option value="1">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
          </select>
        </div>
        <div class="button-group">
          <button @click="add">加 {{ change }}</button>
          <button @click="sub">减 {{ change }}</button>
          <button @click="draw" class="primary">重新抽取</button>
        </div>
      </div>

      <!-- 结果展示区域 -->
      <div class="post-list card">
        <h2>随机帖子列表</h2>
        <div v-if="posts.length > 0" class="posts-container">
          <div v-for="post in posts" :key="post.id" class="post-item">
            <span class="post-id">#{{ post.id }}</span>
            <p class="post-title">{{ post.title }}</p>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无帖子，请先抽取。</p>
        </div>
        <p class="footer-text">~ 顶到底啦 ~</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 1. 页面整体布局和背景 */
.page-container {
  max-width: 1200px; /* max-width (最大宽度): 限制内容最大宽度，在大屏幕上不会过宽 */
  margin: 0 auto; /* margin (外边距): 0 auto -> 上下为0，左右自动，实现水平居中 */
  padding: 20px; /* padding (内边距): 在容器内部增加一些空间 */
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; /* 使用更现代的系统字体 */
  color: #333;
}

/* 2. 顶部欢迎区域 */
.home-view {
  text-align: center;
  margin-bottom: 40px;
}

.home-view h1 {
  font-size: 2.5rem;
  font-weight: 600; /* font-weight (字体粗细): 600 是半粗体 */
  color: #2c3e50;
}

.home-view p {
  font-size: 1.1rem;
  color: #555;
  line-height: 1.6; /* line-height (行高): 增加行高提升可读性 */
}

.home-view a {
  color: #3498db;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.home-view a:hover {
  color: #2980b9;
  text-decoration: underline;
}

/* 3. 主内容区网格布局 */
.main-content {
  display: grid; /* display (显示): grid (网格) -> 启用网格布局，非常适合二维布局 */
  grid-template-columns: 300px 1fr; /* grid-template-columns (网格模板列): 定义列宽。第一列300px，第二列占据剩余空间(1fr) */
  gap: 20px; /* gap (间隙): 定义网格项之间的距离 */
  align-items: start; /* align-items (对齐项目): start -> 让网格项从其单元格的起始位置对齐 */
}

/* 4. 卡片统一样式 (Card Style) */
.card {
  background-color: #ffffff;
  border-radius: 8px; /* border-radius (边框圆角): 让卡片边角变圆滑 */
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); /* box-shadow (盒子阴影): 制造悬浮感和层次感 */
  transition: box-shadow 0.3s ease;
}
.card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.card h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 1.5rem;
  color: #2c3e50;
  border-bottom: 1px solid #eaecef; /* 在标题下方添加一条细线作为分隔 */
  padding-bottom: 10px;
}

/* 5. 左侧控制面板样式 */
.control-panel .control-group {
  display: flex;
  justify-content: space-between; /* justify-content (主轴内容对齐): space-between (两端对齐) */
  align-items: center;
  margin-bottom: 15px;
}

.control-panel label {
  font-weight: 500;
  color: #555;
}

.count-display {
  font-size: 1.2rem;
  font-weight: bold;
  color: #3498db;
  background-color: #f0f8ff;
  padding: 4px 10px;
  border-radius: 4px;
}

.control-panel select {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

.button-group {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.button-group button {
  flex-grow: 1; /* flex-grow (弹性增长因子): 让按钮平分剩余空间 */
  padding: 10px;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  background-color: #f0f0f0;
  color: #333;
}

.button-group button:hover {
  transform: translateY(
    -2px
  ); /* transform (变换): translateY (Y轴平移) -> 鼠标悬停时向上移动2像素，产生轻微浮动效果 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.button-group button.primary {
  background-color: #3498db;
  color: white;
}
.button-group button.primary:hover {
  background-color: #2980b9;
}

/* 6. 右侧帖子列表样式 */
.post-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}
.post-item:last-child {
  border-bottom: none; /* 最后一个帖子项去掉底部分隔线 */
}
.post-item:hover {
  background-color: #fafafa;
}

.post-id {
  background-color: #e9ecef;
  color: #495057;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: bold;
  margin-right: 15px;
}

.post-title {
  margin: 0;
  color: #333;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #888;
}

.footer-text {
  text-align: center;
  margin-top: 20px;
  color: #aaa;
  font-size: 0.9rem;
}

/* 7. 响应式设计：当屏幕宽度小于768px时 */
@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr; /* 在小屏幕上，从两列布局变为单列布局 */
  }
}
</style>
