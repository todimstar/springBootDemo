# 项目环境配置说明

本项目需要以下服务才能正常运行。请确保在开始开发前已启动所有必需服务。

## 快速启动

```bash
# 在项目根目录执行
docker-compose up -d

# 等待服务启动完成后，创建 MinIO bucket
# 访问 http://localhost:9001 登录 MinIO 控制台
# 账号: minio-root  密码: 123456789
# 创建名为 "forum-images" 的 bucket
```

## 服务依赖

### 1. MySQL 8.0
- **用途**: 主数据库
- **端口**: 3306
- **数据库名**: community_forum
- **用户名**: community
- **密码**: community123
- **环境变量**:
  - `DB_HOST=localhost`
  - `DB_PORT=3306`
  - `DB_NAME=community_forum`
  - `DB_USERNAME=community`
  - `DB_PASSWORD=community123`

### 2. Redis 7
- **用途**: 缓存、验证码存储、Session管理
- **端口**: 6379
- **数据库**: 0
- **配置位置**: `application-local.yml`

### 3. MinIO (对象存储)
- **用途**: 图片/文件上传存储
- **API端口**: 9000
- **控制台端口**: 9001
- **账号**: minio-root
- **密码**: 123456789
- **Bucket**: forum-images

## 验证服务状态

```bash
# 检查所有容器状态
docker-compose ps

# 检查 Redis 连接
docker exec community-redis redis-cli ping
# 应返回 PONG

# 检查 MySQL 连接
docker exec community-mysql mysql -ucommunity -pcommunity123 -e "SELECT 1"

# 检查 MinIO 健康状态
curl http://localhost:9000/minio/health/live
```

## 常见问题

### MySQL 连接失败
1. 确认容器已启动: `docker-compose ps`
2. 检查端口占用: `netstat -an | findstr 3306`
3. 确认环境变量已设置

### Redis 连接失败
1. 确认 Redis 容器运行中
2. 检查端口 6379 未被占用

### MinIO 上传失败
1. 确认 bucket "forum-images" 已创建
2. 检查 MinIO 服务状态
3. 验证 access-key 和 secret-key 配置正确

## 数据持久化

Docker volumes 存储位置:
- `mysql_data`: MySQL 数据
- `redis_data`: Redis 持久化数据
- `minio_data`: MinIO 存储文件

清理所有数据:
```bash
docker-compose down -v
```
