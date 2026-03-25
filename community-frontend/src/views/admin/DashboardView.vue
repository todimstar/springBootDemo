<template>
  <div class="admin-dashboard">
    <h2 class="page-title">管理后台</h2>

    <!-- Stats cards -->
    <div class="stats-row mb-20">
      <div class="card stat-card">
        <div class="stat-value">--</div>
        <div class="text-secondary">帖子总数</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value">--</div>
        <div class="text-secondary">用户总数</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value">--</div>
        <div class="text-secondary">待审核</div>
      </div>
      <div class="card stat-card">
        <div class="stat-value">--</div>
        <div class="text-secondary">今日新帖</div>
      </div>
    </div>

    <!-- Pending review -->
    <div class="card mb-20">
      <h3 class="mb-16">待审核帖子</h3>
      <div class="placeholder-section">
        GET /api/admin/posts?status=PENDING_REVIEW 接口开发中
      </div>
    </div>

    <!-- Feature requests -->
    <div class="card">
      <h3 class="mb-16">精选申请</h3>
      <div class="placeholder-section">
        精选申请管理接口开发中
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  text-align: center;
  padding: 24px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}
</style>
