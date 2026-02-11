# 项目环境配置说明

本项目后端需要以下服务才能正常运行。请确保在开始开发前已启动所有必需服务。

## 快速启动

```bash
# 在 community-backgroundend 目录执行
cd community-backgroundend
docker-compose up -d

# 等待服务启动完成后，创建 MinIO bucket
# 访问 http://localhost:9001 登录 MinIO 控制台
# 账号/密码见 .env: MINIO_ACCESS_KEY / MINIO_SECRET_KEY
# 创建名为 "forum-images" 的 bucket
```

如果你在项目根目录（`e:\Java\springBootDemo`）执行，请使用：

```bash
docker-compose -f community-backgroundend/docker-compose.yml --env-file community-backgroundend/.env up -d
```

## 服务依赖

### 1. MySQL 8.0
- **用途**: 主数据库
- **端口**: 3307（由 `.env` 的 `DB_PORT` 控制）
- **数据库名**: 由 `.env` 的 `DB_NAME` 控制
- **用户名**: 由 `.env` 的 `DB_USERNAME` 控制
- **密码**: 由 `.env` 的 `DB_PASSWORD` 控制
- **环境变量**:
  - `DB_HOST=localhost`
  - `DB_PORT=3307`
  - `DB_NAME=springboot_db`
  - `DB_USERNAME=springboot_dev`
  - `DB_PASSWORD=Dev@springboot_114514`

### 2. Redis 7
- **用途**: 缓存、验证码存储、Session管理
- **端口**: 6379
- **数据库**: 0
- **配置位置**: `application-local.yml`

### 3. MinIO (对象存储)
- **用途**: 图片/文件上传存储
- **API端口**: 由 `.env` 的 `MINIO_API_PORT` 控制（默认 9000）
- **控制台端口**: 由 `.env` 的 `MINIO_CONSOLE_PORT` 控制（默认 9001）
- **账号**: `.env` 中 `MINIO_ACCESS_KEY`
- **密码**: `.env` 中 `MINIO_SECRET_KEY`
- **Bucket**: `.env` 中 `MINIO_BUCKET`

## 验证服务状态

```bash
# 检查所有容器状态
docker-compose ps

# 检查 Redis 连接
docker exec community-redis redis-cli ping
# 应返回 PONG

# 检查 MySQL 连接
docker exec community-mysql mysql -u$DB_USERNAME -p$DB_PASSWORD -e "SELECT 1"

# 检查 MinIO 健康状态
curl http://localhost:9000/minio/health/live
```

在项目根目录执行时，对应命令为：

```bash
docker-compose -f community-backgroundend/docker-compose.yml --env-file community-backgroundend/.env ps
```

## 常见问题

### MySQL 连接失败
1. 确认容器已启动: `docker-compose ps`
2. 检查端口占用: `netstat -an | findstr 3307`
3. 确认环境变量已设置

## 单一配置源说明

- `community-backgroundend/.env` 是本项目 Docker 与 Spring Boot 的统一配置源
- 生产/正式凭据请只维护在 `.env`，不要在 `application-cloud.yml` 和文档中硬编码

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
