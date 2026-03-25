<template>
  <div class="right-sidebar">
    <!-- 热门标签 (占位) -->
    <div class="card sidebar-card">
      <h4 class="section-title flex-between">
        热门标签
        <el-tag size="small" type="info">开发中</el-tag>
      </h4>
      <div class="placeholder-section" style="margin-top: 12px">
        标签功能开发中，敬请期待
      </div>
    </div>

    <!-- 社区统计 (占位) -->
    <div class="card sidebar-card">
      <h4 class="section-title">社区统计</h4>
      <div class="stat-list">
        <div class="stat-item">
          <span class="stat-label">帖子总数</span>
          <span class="stat-value">--</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">用户总数</span>
          <span class="stat-value">--</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">今日新帖</span>
          <span class="stat-value">--</span>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="card sidebar-card">
      <h4 class="section-title">快捷入口</h4>
      <el-button type="primary" style="width: 100%" @click="$router.push('/create-post')">
        发布帖子
      </el-button>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.right-sidebar {
  padding: 16px 0;
}

.sidebar-card {
  padding: 16px;
}

.sidebar-card + .sidebar-card {
  margin-top: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.stat-list {
  margin-top: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 13px;
}

.stat-label {
  color: var(--color-text-secondary);
}

.stat-value {
  font-weight: 600;
  color: #303133;
}
</style>
