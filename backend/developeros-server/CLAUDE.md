[根目录](../../CLAUDE.md) > [backend](../) > **developeros-server**

# developeros-server 主服务

> 导航：[⇧ 返回根目录](../../CLAUDE.md) » [backend](../) » **developeros-server**

---

## 一、模块职责

`developeros-server` 是 DeveloperOS 的 V1 MVP 主后端服务，承载所有业务模块：

- **Auth**：注册、登录、JWT 颁发与校验（**已完成**）
- **User**：用户实体、查询（**已完成**）
- **Chat**：会话、消息、SSE 流式输出（**待开发**）
- **Memory**：长期记忆提取与检索（**待开发**）
- **Knowledge（RAG）**：文档上传、切片、Embedding、检索增强问答（**待开发**）
- **Growth**：学习目标、任务、成长日志（**待开发**）

## 二、技术栈

| 组件              | 选型                                          |
| ----------------- | --------------------------------------------- |
| 基础框架          | Spring Boot 3.5                               |
| 编程语言          | Java 21                                       |
| ORM               | MyBatis-Plus 3.5.5 + mybatis-spring 3.0.5     |
| 安全框架          | Spring Security 6 + JJWT 0.12.6               |
| 数据库迁移        | Flyway core + flyway-database-postgresql      |
| 校验              | spring-boot-starter-validation（JSR-380）     |
| 构建工具          | Maven（使用 `./mvnw` 包装器）                 |
| 数据库            | PostgreSQL 17 + pgvector                      |
| 缓存              | Redis 7                                       |
| 部署              | Docker（两阶段 Dockerfile，JDK 21 + JRE Alpine） |

> 关键依赖决策见 `pom.xml` 注释（包含 MyBatis-Plus 3.5.5 选择理由、SqlSessionFactory 手动声明原因等）。

## 三、目录与包结构

```text
backend/developeros-server/
├── pom.xml
├── Dockerfile
├── .dockerignore
├── .gitignore
├── src/
│   ├── main/
│   │   ├── java/com/keyx/
│   │   │   ├── DeveloperosServerApplication.java   # 启动入口
│   │   │   ├── common/                             # 通用层
│   │   │   │   ├── R.java                          # 统一响应
│   │   │   │   └── exception/
│   │   │   │       ├── BusinessException.java
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── config/                             # 配置层
│   │   │   │   ├── MybatisPlusConfig.java
│   │   │   │   └── MyMetaObjectHandler.java
│   │   │   ├── security/                           # 安全层
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   └── module/                             # 业务模块
│   │   │       ├── auth/                           # 已完成
│   │   │       │   ├── controller/AuthController.java
│   │   │       │   ├── dto/  (LoginRequest/LoginResponse/RegisterRequest)
│   │   │       │   └── service/  (AuthService + impl)
│   │   │       ├── user/                           # 已完成
│   │   │       │   ├── entity/User.java
│   │   │       │   ├── mapper/UserMapper.java
│   │   │       │   └── service/  (UserService + impl)
│   │   │       ├── chat/                           # 待开发
│   │   │       ├── memory/                         # 待开发
│   │   │       ├── knowledge/                      # 待开发
│   │   │       └── growth/                         # 待开发
│   │   └── resources/
│   │       ├── application.yml                     # 公共配置 + profile
│   │       ├── application-local.yml               # 本地开发（不入 git）
│   │       └── db/migration/
│   │           └── V1__init_schema.sql             # Flyway 初始迁移（9 张表）
│   └── test/java/com/keyx/
│       └── DeveloperosServerApplicationTests.java  # Spring Boot 上下文加载测试
└── target/                                         # 构建产物（不入 git）
```

## 四、已有模块入口

### 模块 auth（已完成）

- Controller：`com.keyx.module.auth.controller.AuthController`
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- DTO：`LoginRequest`、`LoginResponse`、`RegisterRequest`
- Service：`AuthService` + `AuthServiceImpl`
- 流程：
  1. DTO 校验（`@Valid`）
  2. `UserService.existsByUsername/existsByEmail` 判重
  3. `PasswordEncoder.encode` 加密
  4. `userService.save(user)`
  5. 登录后 `JwtUtil.generate(userId, username)` 颁发 token
  6. 返回 `LoginResponse(token, userId, username)`

### 模块 user（已完成）

- Entity：`com.keyx.module.user.entity.User`（对应 `app_user` 表）
- Mapper：`com.keyx.module.user.mapper.UserMapper`（`@Mapper`，继承 `BaseMapper<User>`）
- Service：`UserService` + `UserServiceImpl`（继承 `ServiceImpl<UserMapper, User>`）
- 提供方法：
  - `findByUsername(String)` / `findByEmail(String)`
  - `existsByUsername(String)` / `existsByEmail(String)`
  - MyBatis-Plus 自带 CRUD（`save`、`updateById`、`getById`、`list`、`removeById`）

### 模块 common（已完成）

- `R<T>`：统一响应（`code` / `message` / `data`），所有 Controller 必须返回该类型。
- `BusinessException`：业务异常（自定义 `code`，默认 400）。
- `GlobalExceptionHandler`：三段式处理
  1. `BusinessException` → `R.fail(code, message)`
  2. `MethodArgumentNotValidException` → 取第一条字段错误 → `R.fail(400, msg)`
  3. `Exception` 兜底 → `R.fail(500, "服务器内部错误")`，记录完整堆栈

### 模块 config（已完成）

- `MybatisPlusConfig`：显式声明 `SqlSessionFactory`（解决 Spring Boot 3.5 + MyBatis-Plus 3.5.5 自动配置丢失问题）；注册 `PaginationInnerInterceptor`（PostgreSQL 分页）。
- `MyMetaObjectHandler`：自动填充 `createdAt` / `updatedAt`（V1 暂未生效，Service 手动 set 更稳）。

### 模块 security（已完成）

- `JwtUtil`：基于 JJWT 0.12.6 的生成、解析、验证。
- `SecurityConfig`：
  - 禁用 CSRF、表单登录、HTTP Basic
  - 无状态 Session（`STATELESS`）
  - `/api/auth/**` 放行，其他接口需要认证
  - 提供 `BCryptPasswordEncoder` Bean
- `JwtAuthenticationFilter`：从 `Authorization: Bearer <token>` 抠 token，校验后写入 `SecurityContextHolder`。

## 五、迁移目录与 Flyway

- 路径：`src/main/resources/db/migration/`
- 当前：`V1__init_schema.sql`（Flyway 初始迁移，9 张表 + 触发器）
- 规范：
  1. V1 执行后不再修改
  2. 后续字段、索引、约束变化新增迁移文件（`V2__...`）
  3. PostgreSQL 支持事务 DDL，无需手工 `BEGIN/COMMIT`
  4. 重要迁移前先备份，先在测试库验证

### 9 张核心表

| 表                  | 模块      | 作用                           |
| ------------------- | --------- | ------------------------------ |
| `app_user`          | User      | 账号、资料、偏好、状态         |
| `conversation`      | Chat      | AI 会话                        |
| `message`           | Chat      | 会话消息                       |
| `memory`            | Memory    | 长期记忆                       |
| `knowledge_document`| Knowledge | 文档元数据                     |
| `knowledge_chunk`   | Knowledge | 文档切片 + Embedding           |
| `learning_goal`     | Growth    | 学习目标                       |
| `learning_task`     | Growth    | 学习任务                       |
| `growth_log`        | Growth    | 成长日志                       |

详细字段、外键、级联、索引、Service 维护边界详见 `docs/database/Database Design.md`。

## 六、配置与运行

### 6.1 配置文件分层

- `application.yml`：公共配置 + profile 切换 + 环境变量占位符（入 git）
- `application-local.yml`：IDE 本地运行配置（不入 git）
- `docker/.env`：Docker 容器启动时注入（不入 git）

### 6.2 环境变量

| 变量                | 默认值                  | 说明                              |
| ------------------- | ----------------------- | --------------------------------- |
| `POSTGRES_HOST`     | `localhost`             | PostgreSQL 主机                   |
| `POSTGRES_DB`       | `developeros`           | 数据库名                          |
| `POSTGRES_USER`     | 必填                    | 数据库用户                        |
| `POSTGRES_PASSWORD` | 必填                    | 数据库密码                        |
| `REDIS_HOST`        | `localhost`             | Redis 主机                        |
| `REDIS_PORT`        | `6379`                  | Redis 端口                        |
| `REDIS_PASSWORD`    | 必填                    | Redis 密码                        |
| `SERVER_PORT`       | `9090`                  | Spring Boot 端口（避开 8080 占用） |
| `JWT_SECRET`        | 必填（≥32 字节）        | JWT 签名密钥                      |
| `JWT_EXPIRATION`    | `604800000`（7 天）     | Token 过期毫秒数                  |

### 6.3 启动方式

#### A. Docker Compose（推荐）

```bash
docker compose -f docker/docker-compose.yml up -d
```

backend 容器自动连接 `postgres` / `redis` 服务，端口 9090。

#### B. 本地 IDE 运行

1. 启动本地 PostgreSQL + Redis
2. 创建 `application-local.yml`（或使用默认占位）
3. IDEA 直接运行 `DeveloperosServerApplication`

#### C. 本地 Maven

```bash
./mvnw clean package -DskipTests
java -jar target/developeros-server-0.0.1-SNAPSHOT.jar
```

## 七、测试入口

- `src/test/java/com/keyx/DeveloperosServerApplicationTests.java`：Spring Boot 上下文加载测试（`contextLoads`）
- 后续模块需要补：
  - AuthService 单元测试 / 集成测试
  - JWT Filter 测试
  - 各模块 Controller MockMvc 测试

## 八、对外接口（当前）

| Method | Path                    | 鉴权 | 说明                                |
| ------ | ----------------------- | ---- | ----------------------------------- |
| POST   | `/api/auth/register`    | 否   | 注册；请求体见 `RegisterRequest`    |
| POST   | `/api/auth/login`       | 否   | 登录；返回 `LoginResponse` 含 token |

> 其他接口暂未实现，将在 chat / memory / knowledge / growth 模块逐步上线。

## 九、常见问题 (FAQ)

### 9.1 为什么 MyBatis-Plus 用 3.5.5？

3.5.9 起分页插件从 `mybatis-plus-extension` 拆到 `mybatis-plus-jsqlparser`，3.5.5 是最后一个 extension 内含 `PaginationInnerInterceptor` 的稳定版本（详见 2026-07-17 开发日志）。

### 9.2 为什么手动声明 `SqlSessionFactory`？

Spring Boot 3.5 + MyBatis-Plus 3.5.5 自动配置可能丢失 `SqlSessionFactory` Bean，启动报 `Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required`，显式声明是最稳的方案。

### 9.3 为什么 `JwtAuthenticationFilter` 没注册到 Security 链？

V1 暂未在 `SecurityConfig` 中 `addFilterBefore`。当前实现仍依赖 `SecurityFilterChain` 放行 `/api/auth/**` 与 `.anyRequest().authenticated()`。后续需把过滤器接入过滤器链，并在 Controller 中通过 `SecurityContextHolder` 读取 userId。

### 9.4 `preferences` 字段为什么用 `String` + `@TableField(updateStrategy = NEVER)`？

数据库是 JSONB，Java 当前用 String 占位。直接 `updateById` 会触发 JSONB 类型不匹配；用 `NEVER` 让 update 时跳过该字段，V2 再补 TypeHandler。

### 9.5 时区策略

- 数据库：`TIMESTAMPTZ`，绝对时间
- 后端：统一 `Instant`
- 前端展示：按用户 `app_user.timezone`（默认 `Asia/Shanghai`）
- Docker：`TZ=Asia/Shanghai`、`-Duser.timezone=Asia/Shanghai`

## 十、相关文件清单

- `pom.xml`：依赖与构建
- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`（不入 git）
- `src/main/resources/db/migration/V1__init_schema.sql`
- `Dockerfile`
- `../../docker/docker-compose.yml`

## 十一、变更记录 (Changelog)

| 日期       | 变更内容                                                                                                                |
| ---------- | ----------------------------------------------------------------------------------------------------------------------- |
| 2026-07-18 | 新建 `developeros-server/CLAUDE.md`，记录包结构、已有模块入口、迁移目录、运行命令、FAQ。                               |
| 2026-07-17 | Auth + User 模块完成：注册、登录、JWT、统一响应、全局异常、MyBatis-Plus 集成（21 个文件，979 行）。                      |
| 2026-07-15 | 完成 Spring Boot 后端容器化、Docker Compose 编排、配置文件分层。                                                         |