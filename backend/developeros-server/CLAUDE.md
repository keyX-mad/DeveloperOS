[根目录](../../CLAUDE.md) > [backend](../) > **developeros-server**

# developeros-server 主服务

> 导航：[⇧ 返回根目录](../../CLAUDE.md) » [backend](../) » **developeros-server**

---

## 一、模块职责

`developeros-server` 是 DeveloperOS 的 V1 MVP 主后端服务，承载所有业务模块：

- **Auth**：注册、登录、JWT 颁发与校验（**已完成**）
- **User**：用户实体、查询（**已完成**）
- **Chat**：会话、消息、SSE 流式输出（**已完成，待端到端验证**）
- **Memory**：长期记忆提取与检索（**待开发**）
- **Knowledge（RAG）**：文档上传、切片、Embedding、检索增强问答（**待开发**）
- **Growth**：学习目标、任务、成长日志（**待开发**）

## 二、技术栈

| 组件              | 选型                                          | 备注 |
| ----------------- | --------------------------------------------- | ---- |
| 基础框架          | Spring Boot 3.5.0                             | 锁定（升级 4.x 工作量大，OGNL 问题不会消失） |
| 编程语言          | Java 21                                       | |
| ORM               | MyBatis-Plus 3.5.5（**锁定**）+ mybatis-spring 3.0.5 | 详见 §9.1（3.5.6+ 有 OGNL bug） |
| MyBatis           | 3.5.16                                        | Spring Boot 3.5 默认 |
| 安全框架          | Spring Security 6 + JJWT 0.12.6               | |
| 数据库迁移        | Flyway core + flyway-database-postgresql      | |
| 校验              | spring-boot-starter-validation（JSR-380）     | |
| AI 集成           | Spring AI 1.0.3（spring-ai-starter-model-openai） | |
| 构建工具          | Maven（使用 `./mvnw` 包装器）                 | |
| 数据库            | PostgreSQL 17 + pgvector                      | |
| 缓存              | Redis 7                                       | |
| 部署              | Docker（两阶段 Dockerfile，JDK 21 + JRE Alpine） | |

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
│   │   │       ├── chat/                           # 已完成（待端到端验证）
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
- `JwtAuthenticationFilter`：从 `Authorization: Bearer <token>` 抠 token，校验后写入 `SecurityContextHolder`（principal 为 `Long userId`，由 `BaseController.currentUserId()` 统一读取）。

### 模块 chat（已完成，待端到端验证）

**核心能力**：AI 多轮对话 + SSE 流式输出 + 会话/消息管理 + Spring AI 接入。

#### 包结构

```text
module/chat/
├── controller/
│   ├── ChatController.java              # 流式对话 + abort
│   └── ConversationController.java      # 会话 CRUD
├── service/
│   ├── ConversationService.java         # 会话 Service 接口
│   ├── MessageService.java              # 消息 Service 接口
│   └── impl/
│       ├── ConversationServiceImpl.java
│       └── MessageServiceImpl.java
├── stream/
│   ├── ChatStreamChunk.java             # SSE chunk DTO（type/content/done/error）
│   ├── ChatStreamService.java           # 流式服务接口
│   └── impl/
│       └── ChatStreamServiceImpl.java   # 流式核心（Spring AI ChatClient + abort 机制）
├── prompt/
│   ├── PromptBuilder.java               # 组装 system + history + user Message
│   └── SystemPromptTemplate.java        # @ConfigurationProperties 读 YAML
├── entity/
│   ├── Conversation.java                # @TableName("conversation")
│   └── Message.java                     # @TableName("message")
├── mapper/
│   ├── ConversationMapper.java
│   └── MessageMapper.java
├── enums/
│   ├── ConversationStatus.java          # ACTIVE / ARCHIVED
│   ├── MessageRole.java                 # SYSTEM / USER / ASSISTANT / TOOL
│   └── MessageStatus.java               # STREAMING / COMPLETED / FAILED / STOPPED
├── dto/
│   ├── request/
│   │   ├── CreateConversationRequest.java
│   │   ├── UpdateConversationRequest.java
│   │   └── SendMessageRequest.java      # @NotBlank + @Size(max=32000)
│   └── response/
│       ├── ConversationVO.java
│       ├── MessageVO.java
│       └── PageVO.java                  # 泛型分页 VO
└── exception/
    └── ChatException.java               # 错误码 1500+ 段
```

#### 对外接口

| Method | Path                                                | 鉴权 | 说明                                |
| ------ | --------------------------------------------------- | ---- | ----------------------------------- |
| POST   | `/api/chat/conversations`                           | 是   | 创建会话                            |
| GET    | `/api/chat/conversations`                           | 是   | 分页查会话列表                      |
| GET    | `/api/chat/conversations/{id}`                      | 是   | 查单个会话                          |
| PUT    | `/api/chat/conversations/{id}`                      | 是   | 改会话标题                          |
| DELETE | `/api/chat/conversations/{id}`                      | 是   | 删除会话（级联删 messages）         |
| GET    | `/api/chat/conversations/{id}/messages`             | 是   | 查消息列表（分页）                  |
| POST   | `/api/chat/conversations/{conversationId}/messages` | 是   | **SSE 流式发送消息**                |
| POST   | `/api/chat/abort/{messageId}`                       | 是   | 用户主动停止生成                    |

#### 关键流程（流式对话）

```
1. POST /conversations/{id}/messages {content, modelName}
2. JwtAuthenticationFilter 解析 token → SecurityContext principal = userId
3. ChatController.stream 接收 @Valid SendMessageRequest
4. ChatStreamServiceImpl.stream(userId, conversationId, content, modelName)
   ├─ 4.1 conversationService.getById() 校验归属
   ├─ 4.2 messageService.saveUserAndAssistantStart()  # 原子事务，两条 message + lastMessageAt
   ├─ 4.3 promptBuilder.build() 组装 system + 最近 20 条 COMPLETED 历史 + 当前 user
   └─ 4.4 chatClient.prompt().stream().content()  # Flux<String>
       ├─ doOnNext: 累积 token + 检查 abortFlags（抛 AbortSignal 中断）
       ├─ doOnComplete: completeAssistant(content)
       ├─ doOnError(AbortSignal): stopAssistant(partialContent)
       └─ doOnError(other): failAssistant(errorMessage)
5. Flux<ChatStreamChunk> 推送给前端（SSE）
```

#### 关键设计

- **状态机**：`STREAMING → COMPLETED / FAILED / STOPPED`，`failAssistant` / `stopAssistant` 保留已生成的部分 content
- **原子事务**：`saveUserAndAssistantStart` 把 user insert + assistant insert 包到同一事务（#4 修复）
- **abort 机制**：用 `ConcurrentHashMap<Long, Boolean>` 存 abort 标记；`AbortSignal` 内部异常中断流式回调（V1 简单，V2 改 Redis）
- **Prompt 组装**：只取 `status=COMPLETED` 的历史（过滤中间态），V2/V3 预留 `injectMemory()` / `injectRagContext()` 钩子
- **用户隔离**：所有 Service 方法首个参数都是 `userId`，校验失败统一抛 404（不是 403，防止信息泄露）
- **派生字段维护**：`MessageService` 通过 `ConversationService.touchLastMessage()` 更新 `conversation.last_message_at`（不再直接用 mapper，符合分层）

#### Spring AI 集成

- 依赖：`spring-ai-starter-model-openai`（BOM 统一在 pom.xml）
- 配置：`application.yml` 的 `spring.ai.openai.*`（api-key / base-url / model）
- 用法：`ChatClient.Builder` 注入到 `ChatStreamServiceImpl`，**构造时 build 一次单例**

#### 关键设计：MyBatis-Plus + MyBatis OGNL 兼容性

**重要约束**（影响 list / count / 列表查询的写法）：

| 写法 | 状态 |
|---|---|
| `insert(entity)` / `updateById(entity)` / `deleteById(id)` / `selectById(id)` | ✅ MyBatis-Plus 原生可用 |
| `lambdaQuery().eq().one()` | ✅ 链式调用能绕开 OGNL，**正常使用** |
| `lambdaQuery().eq().count()` | ✅ 链式调用能绕开 OGNL，**正常使用** |
| `lambdaQuery().list()` / `mapper.selectList(wrapper)` | ❌ 触发 OGNL 报错 |
| `mapper.selectCount(wrapper)` / `mapper.selectOne(wrapper)` | ❌ 直接传 wrapper 触发 OGNL |
| `mapper.selectPage(page, wrapper)` | ❌ 触发 OGNL 报错 |

**最佳实践**（list 查询）：

```java
// ✅ 正确写法：records 手写 SQL + total 用链式 .count()
List<Message> records = messageMapper.selectByConversationPaged(...);  // @Select 手写
long total = lambdaQuery().eq(...).count();                              // 链式，OK
```

详见 §9.1（FAQ 解释 MyBatis-Plus 3.5.5 锁定原因）

#### 关键设计：created_at 自动填充

**当前状态**：Service 层手动 set `createdAt` / `updatedAt`（V1 临时方案）。

**原因**：`MyMetaObjectHandler` 暂未生效（详见 §9.4）。直接 INSERT 时不填这两个字段会触发数据库 `NOT NULL` 约束。

**未来优化**（V2）：让 `MyMetaObjectHandler` 生效后，可以从 Service 层去掉手写 set。

#### 关键设计：abort 流式中断

**V1 简单方案**：用 `ConcurrentHashMap<Long, Boolean>` 存 abort 标记 + `AbortSignal` 内部异常中断流式回调。

**未来优化**（V2）：改 Redis（`SET abort:{messageId} 1 EX 60`），解决多实例部署 + 内存泄漏问题。

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

| Method | Path                                                | 鉴权 | 说明                                |
| ------ | --------------------------------------------------- | ---- | ----------------------------------- |
| POST   | `/api/auth/register`                                | 否   | 注册；请求体见 `RegisterRequest`    |
| POST   | `/api/auth/login`                                   | 否   | 登录；返回 `LoginResponse` 含 token |
| POST   | `/api/chat/conversations`                           | 是   | 创建会话                            |
| GET    | `/api/chat/conversations`                           | 是   | 分页查会话列表                      |
| GET    | `/api/chat/conversations/{id}`                      | 是   | 查单个会话                          |
| PUT    | `/api/chat/conversations/{id}`                      | 是   | 改会话标题                          |
| DELETE | `/api/chat/conversations/{id}`                      | 是   | 删除会话                            |
| GET    | `/api/chat/conversations/{id}/messages`             | 是   | 查消息列表                          |
| POST   | `/api/chat/conversations/{conversationId}/messages` | 是   | **SSE 流式发送消息**                |
| POST   | `/api/chat/abort/{messageId}`                       | 是   | 用户主动停止生成                    |

> Memory / Knowledge / Growth 模块接口将在后续 V1.x 迭代中上线。

## 九、常见问题 (FAQ)

### 9.1 为什么 MyBatis-Plus 锁定 3.5.5？🔒

**核心原因**：MyBatis-Plus 3.5.5 + MyBatis 3.5.16 + Spring Boot 3.5.0 组合**已通过端到端验证**，3.5.6+ 有 OGNL 兼容 bug（详见下）。

**MyBatis-Plus 3.5.6+ 已知问题**：

| 现象 | 原因 |
|---|---|
| `mapper.selectList(wrapper)` 抛 `MybatisPlusException: can not use this method for "getSqlFirst"` | MyBatis 内部 XML 模板 `<if test="ew.sqlFirst != null">` 调 `ew.getSqlFirst()`，被 OGNL sandbox 拦截 |
| `mapper.selectCount(wrapper)` / `mapper.selectOne(wrapper)` 同样问题 | 直接传 wrapper 时触发 |
| `lambdaQuery().list()` / `selectPage` 同样问题 | 链式 .list() 走 executeForMany 路径 |
| `lambdaQuery().one()` / `.count()` 正常 | 链式不走 MyBatis 内部 XML 模板，绕开 OGNL |

**已尝试的方案**（全部失败）：

| 方案 | 结果 |
|---|---|
| 升级 MyBatis-Plus 3.5.6 / 3.5.9 / 3.5.10 / 3.5.17 | ❌ 同样的 OGNL 报错 |
| 降级 MyBatis 到 3.5.15 + 升 mybatis-spring 3.0.3 | ❌ MyBatis-Plus 3.5.6+ 假设 MyBatis 3.5.16 API |
| 降级 MyBatis-Plus 到 3.5.1 | ❌ 与 Spring 6 不兼容（`NestedIOException` 找不到） |
| 降级到 3.5.3.2 | ❌ 同样 OGNL 报错 |
| 升 Spring Boot 4.0 | ❌ 默认带 MyBatis 3.5.19+，OGNL sandbox 仍然存在 |

**当前最佳实践**（`chat` 模块已应用）：

```java
// 列表查询：records 手写 SQL + total 用链式 .count()
@Select("SELECT * FROM message WHERE conversation_id = #{convId} ORDER BY created_at LIMIT #{size} OFFSET #{offset}")
List<Message> selectByConversationPaged(...);

long total = lambdaQuery()
        .eq(Message::getConversationId, conversationId)
        .count();
```

未来 RAG / Memory 模块沿用此模式。

### 9.2 为什么手动声明 `SqlSessionFactory`？

Spring Boot 3.5 + MyBatis-Plus 3.5.5 自动配置可能丢失 `SqlSessionFactory` Bean，启动报 `Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required`，显式声明是最稳的方案。

### 9.3 为什么 `JwtAuthenticationFilter` 没注册到 Security 链？

（已过时）当前 `SecurityConfig` 已 `addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`，filter 接入过滤器链，principal 存 `Long userId`，由 `BaseController.currentUserId()` 统一读取。

### 9.4 `preferences` 字段为什么用 `String` + `@TableField(updateStrategy = NEVER)`？

数据库是 JSONB，Java 当前用 String 占位。直接 `updateById` 会触发 JSONB 类型不匹配；用 `NEVER` 让 update 时跳过该字段，V2 再补 TypeHandler。

### 9.5 时区策略

- 数据库：`TIMESTAMPTZ`，绝对时间
- 后端：统一 `Instant`
- 前端展示：按用户 `app_user.timezone`（默认 `Asia/Shanghai`）
- Docker：`TZ=Asia/Shanghai`、`-Duser.timezone=Asia/Shanghai`

### 9.6 Chat 模块的流式 abort 为什么用 `ConcurrentHashMap` 而不是 Redis？

V1 单实例部署，`ConcurrentHashMap` 性能最优、零依赖。**V2 改 Redis** 解决多实例部署问题（主从实例间共享状态），同时加 `EX 60` 过期防止内存泄漏。

### 9.7 为什么 `createdAt` / `updatedAt` 在 Service 手动 set？

**当前 V1 状态**：`MyMetaObjectHandler` 暂未生效，INSERT 时这两个字段为 `null`，触发数据库 `NOT NULL` 约束。

**临时方案**：在 Service 层 insert 前手动 `setCreatedAt(Instant.now())` + `setUpdatedAt(Instant.now())`。

**影响范围**（已修复）：
- `ConversationServiceImpl.create()` 手动 set
- `MessageServiceImpl.createUserMessageInternal()` 手动 set
- `MessageServiceImpl.createAssistantStartInternal()` 手动 set

**未来优化**（V2）：让 `MyMetaObjectHandler` 的 `insertFill` / `updateFill` 真正生效后，可去掉所有手写 set。

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
| 2026-07-20 | **Chat 模块完成 + 端到端验证通过**：AI 对话 + SSE 流式 + 会话/消息管理 + Spring AI 集成（26 个文件）。含 15 项 Review 修复：JWT userId 类型、流式接口签名 + RESTful 路径、双事务合并、状态机事务、abort 完整修复（越权 + 真正中断）、`ConversationStatus` 枚举、移除直接 mapper 注入、`ChatClient` 单例化、`BaseController` 提取、Prompt 加载 SQL 优化（Desc+LIMIT+status 过滤 + TOOL 跳过）、`@Size` 放大、`JWT` 失败返回 401、注释补全。**端到端测试 20/20 通过**（基础 6 + 用户隔离 4 + 异常 5 + 自身 5）。修复 2 个 P0 bug：`created_at` NOT NULL 约束（Service 手动 set 时间）+ MyBatis-Plus OGNL 兼容（链式 `.count()` + `@Select` 手写 records）。锁定版本：MyBatis-Plus 3.5.5 + MyBatis 3.5.16 + Spring Boot 3.5.0 + Java 21。|
| 2026-07-18 | 新建 `developeros-server/CLAUDE.md`，记录包结构、已有模块入口、迁移目录、运行命令、FAQ。                               |
| 2026-07-17 | Auth + User 模块完成：注册、登录、JWT、统一响应、全局异常、MyBatis-Plus 集成（21 个文件，979 行）。                      |
| 2026-07-15 | 完成 Spring Boot 后端容器化、Docker Compose 编排、配置文件分层。                                                         |