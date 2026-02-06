# MinIO 文件存储学习计划

## 📅 学习周期：第3周

---

## 🎯 学习目标

1. 理解对象存储的概念和应用场景
2. 掌握 Docker Compose 部署服务
3. 学会 MinIO 的基本操作
4. 实现 Spring Boot 集成 MinIO
5. 完成图片上传功能

---

## 📖 Day 15：环境搭建

### 知识点清单

- [ ] **Docker Compose 基础**
  - 什么是 Docker Compose？和 docker run 的区别
  - docker-compose.yml 文件结构
  - 常用命令：up, down, logs, ps

- [ ] **MinIO 核心概念**
  - 什么是对象存储（Object Storage）
  - Bucket（桶）的概念
  - S3 兼容 API 是什么意思
  - 端口 9000（API）vs 9001（Console）的区别

- [ ] **动手实践**
  - 使用 Docker Compose 启动 MinIO
  - 访问 MinIO Console 创建 Bucket
  - 手动上传一张图片测试

### 学习笔记区

> （学完后在这里记录你的理解）
> Compose启动会记录启动命令格式，而直接run需要记很长的参数，不如compose稳定和易迁移
> 文件结构就是service:minio:xxx，会记录账号密码端口路径等信息
> up:启动命令，docker-compose up -d后台启动，也可以-f指定文件名；其余命令都是以docker-compose为前缀...同理可-f
> 对象存储，感觉就是持久化除了代码之外的文件比如图片视频等
> Bucket就像是文件系统里的文件夹一样
> 不了解这个API和S3是什么意思
> 9000为api访问操作，9001为浏览器可视化操作
> 已成功启动上传

---

## 📝 导师点评区 (Day 15)

✅ **恭喜完成环境搭建！**
非常棒的总结！你的理解已经抓住了核心。补充一点关于 S3 的知识：
**S3 API (Simple Storage Service API)**：
就像 SQL 是数据库的通用语言一样，S3 API 是对象存储界的 "普通话"。Amazon 最早搞出了 S3，因为它太流行了，后来的厂商（MinIO、阿里云、腾讯云等）都照着它的接口标准做。
**好处**：如果我们的代码是按 S3 标准写的，以后想把 MinIO 换成阿里云 OSS，代码几乎不用改！这就是标准的力量。

---

## 📖 Day 16：Spring Boot 集成

### 知识点清单

- [ ] **MinIO Java SDK**
  - MinioClient 的作用
  - Builder 模式（建造者模式）
  - @Bean 注解的作用

- [ ] **配置管理**
  - @Value 注解读取配置
  - @ConfigurationProperties 批量绑定（进阶）
  - 敏感信息管理

- [ ] **动手实践**
  - 添加 MinIO Maven 依赖
  - 配置 application.yml
  - 创建 MinioConfig 配置类
  - 写一个简单的测试验证连接

### 学习笔记区

> （学完后在这里记录你的理解）

---

## 📘 核心知识补充 (重点补课)

### 1. 什么是对象存储 (Object Storage)？
**通俗比喻**：
- **文件存储（Windows/Linux文件系统）**：像**图书馆**。你要找一本书，必须知道它在“3楼-历史区-第4排-第2层”。你需要维护复杂的目录层级结构。
- **对象存储（MinIO/S3）**：像**代客泊车**。你把车（文件）给服务员，换回一张票（Key/URL）。下次你只管拿票取车，车停在哪、第几层、怎么停的，你不用管，系统自己管理。

**三大特征**：
1.  **扁平化**：没有真正的“文件夹”概念，所谓 `2026/01/13/` 只是文件名（Key）的一部分前缀，为了让人好理解而已。
2.  **不可修改**：对象一旦上传，通常只能覆盖或删除，不能修改文件里的某一行内容（不像 Word 文档可以直接编辑）。
3.  **HTTP 访问**：天生支持 Web，每个文件都有一个 URL。

### 2. MinIO 基本操作 (CRUD)
你现在的代码里已经实现全套了，对应关系如下：

| 动作 | MinIO 概念 | 对应 Java 代码 |
|------|------------|----------------|
| 建桶 | Make Bucket | `minioClient.makeBucket()` |
| 上传 | Put Object | `minioClient.putObject()` |
| 下载 | Get Object | (前端直接访问 URL，后端 `minioClient.getObject` 用于流式下载) |
| 查询 | Stat Object | (查文件是否存在/大小等元数据) |
| 删除 | Remove Object | `minioClient.removeObject()` |
| 权限 | Set Policy | `minioClient.setBucketPolicy()` |

### 3. MinIO vs 阿里云OSS/腾讯云COS

| 特性 | MinIO (自建) | 云厂商 (阿里云OSS / 腾讯云COS) |
|------|-------------|-------------------------------|
| **控制权** | **完全掌握** (数据在自己硬盘里) | 掌握在云厂商手里 (隐私顾虑) |
| **成本** | **低** (只需服务器/硬盘钱) | **高** (存储费 + **流量费** + 请求费) |
| **维护** | **难** (要自己管 Docker、扩容、备份) | **易** (花钱买服务，几乎不用管) |
| **功能** | 核心功能都有，兼容 S3 | 功能极其丰富 (自带图片处理、CDN 加速、鉴黄等) |
| **场景** | 内网应用、保密数据、学习项目、缺钱的初创公司 | 互联网产品、大流量 App、不差钱的企业 |

**总结**：我们学习用 MinIO，如果以后去大公司用阿里云 OSS，**代码改动极小**（只需换 endpoint、accessKey、secretKey，甚至 SDK 都不用换，因为都兼容 S3 协议）。

---

## 📖 Day 17：文件上传 Service

### 知识点清单

- [ ] **文件上传安全**
  - 为什么要校验文件类型？
  - 为什么要限制文件大小？
  - 为什么不能用原始文件名？
  - MIME Type 是什么？

- [ ] **UUID 与文件命名**
  - UUID 的作用和原理
  - 按日期分目录的好处

- [ ] **MinIO API**
  - putObject：上传文件
  - removeObject：删除文件
  - getPresignedObjectUrl：预签名 URL

- [ ] **动手实践**
  - 创建 FileService
  - 实现文件校验逻辑
  - 实现上传方法

### 学习笔记区

> （学完后在这里记录你的理解）

---

## 📖 Day 18-19：完整功能实现

### 知识点清单

- [ ] **Spring MVC 文件上传**
  - MultipartFile 接口
  - @RequestParam 绑定文件
  - multipart/form-data 编码类型

- [ ] **动手实践**
  - 创建 FileController
  - 实现头像上传接口
  - 实现帖子图片上传接口
  - Apifox/Postman 测试

### 学习笔记区

> （学完后在这里记录你的理解）

---

## 🔑 核心概念速查

| 术语 | 英文 | 含义 |
|------|------|------|
| MinIO | Mini + IO | 迷你输入输出，轻量级对象存储服务 |
| Bucket | 桶 | 存储对象的容器，类似于最顶层的文件夹 |
| Object | 对象 | 存储的文件，用唯一的 Key 标识 |
| S3 | Simple Storage Service | Amazon 的简单存储服务，MinIO 兼容其 API |
| Endpoint | 端点 | 服务的访问地址 |
| Access Key | 访问密钥 | 身份认证用，类似用户名 |
| Secret Key | 秘密密钥 | 身份认证用，类似密码 |

---

## ✅ 完成检查清单

- [ ] MinIO 环境运行正常
- [ ] 能通过 Console 上传/查看文件
- [ ] Spring Boot 能连接 MinIO
- [ ] 头像上传接口可用
- [ ] 帖子图片上传接口可用
- [ ] 前端能正常显示上传的图片

---

## 📝 导师点评区

> （每个阶段完成后我会在这里给你点评和建议）

