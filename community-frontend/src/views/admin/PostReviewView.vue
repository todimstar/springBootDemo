<template>
  <div class="post-review">
    <h2 style="font-size: 20px; font-weight: 600; margin-bottom: 24px">帖子审核</h2>

    <el-alert
      title="审核查询接口开发中"
      description="GET /api/admin/posts?status=PENDING_REVIEW 接口尚未实现，此页面为预留 UI。完成后端接口后即可联调。"
      type="info"
      :closable="false"
      show-icon
      class="mb-20"
    />

    <!-- Preview UI structure -->
    <div class="card">
      <div class="flex-between mb-16">
        <el-select disabled placeholder="按状态筛选" style="width: 200px">
          <el-option label="待审核" value="1" />
          <el-option label="已发布" value="2" />
          <el-option label="已拒绝" value="3" />
        </el-select>
        <el-input disabled placeholder="搜索帖子..." style="width: 250px" prefix-icon="Search" />
      </div>

      <el-table :data="[]" empty-text="接口开发中，暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="username" label="作者" width="120" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button size="small" type="success" disabled>通过</el-button>
            <el-button size="small" type="danger" disabled>拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
