<template>
    <!-- 1. <template> 区域 -->
    <!-- 这是 Vue 组件的模板部分，定义了组件的 HTML 结构。 -->
    <div class="about">
        <h1>关于我们</h1>
        <p>这是一个基于 Vue.js 和 Spring Boot 构建的全栈社区项目。</p>
        <p>前端使用 Vue 3、Vue Router 和 Vite，后端使用 Spring Boot。</p>
        <p>我们的目标是创建一个简单、快速且易于扩展的在线交流平台。</p>
        <p>感谢你的访问！</p>
      <el-button type="primary">一个验证element-plus安装成功的蓝色按钮</el-button>
    </div>
</template>

<script setup>
// 2. <script setup> 区域
// 这是 Vue 3 的组合式 API (Composition API) 的语法糖。
// 在这个区域内声明的变量和函数都可以直接在 <template> 中使用。
// 目前这个 "关于我们" 页面是静态的，所以这里不需要任何 JavaScript 逻辑。
</script>

<style scoped>
/* 3. <style scoped> 区域 */
/* "scoped" (作用域) 属性确保这里的 CSS 样式只应用于当前组件 (AboutView.vue)，不会影响到其他组件。 */
/* 这是一种非常好的实践，可以避免全局样式冲突。 */

.about {
    /* 使用 Flexbox 布局来轻松实现内容的垂直和水平居中 */
    display: flex; /* display (显示): flex (弹性盒子) -> 启用 Flexbox 布局 */
    flex-direction: column; /* flex-direction (弹性方向): column (列) -> 让子元素从上到下垂直排列 */
    align-items: center; /* align-items (对齐项目): center -> 在交叉轴（这里是水平方向）上居中 */
    justify-content: center; /* justify-content (主轴内容对齐): center -> 在主轴（这里是垂直方向）上居中 */

    min-height: 50vh; /* min-height (最小高度): 50vh (视口高度的50%) -> 确保内容区域至少有半个屏幕高，使其在页面上看起来更居中 */
    padding: 2rem; /* 在内容周围添加一些内边距，避免文字紧贴边缘 */
    text-align: center; /* text-align (文本对齐): center -> 让容器内的文本居中对齐 */
}

.about h1 {
    font-size: 2.2rem; /* rem 是一个相对单位，相对于根元素(<html>)的字体大小，方便进行响应式缩放 */
    color: #2c3e50; /* 一种深灰色，比纯黑更柔和 */
    margin-bottom: 1.5rem; /* 标题和下方段落之间的间距 */
}

.about p {
    font-size: 1.1rem;
    color: #34495e; /* 另一种深灰色 */
    line-height: 1.6; /* line-height (行高) -> 增加行高可以提高段落的可读性 */
    max-width: 600px; /* max-width (最大宽度) -> 限制段落的最大宽度，在大屏幕上防止文本行过长，提升阅读体验 */
}
</style>
