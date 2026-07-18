[根目录](../CLAUDE.md) > **backend**

# backend 后端总目录

> 导航：[⇧ 返回根目录](../CLAUDE.md) » **backend**

---

## 一、目录职责

`backend/` 是 DeveloperOS 后端代码总目录。

当前只包含一个 Spring Boot 主服务：`developeros-server`。后续若需要拆分（auth-server、chat-server、rag-server 等），也会放在该目录下作为并列模块。

## 二、当前结构

```text
backend/
└── developeros-server/    # Spring Boot 主服务（V1 MVP）
    ├── src/main/java/com/keyx/
    │   ├── common/                # R、异常
    │   ├── config/                # MyBatis-Plus 配置
    │   ├── security/              # JWT、过滤器、SecurityConfig
    │   ├── module/
    │   │   ├── auth/              # 已完成
    │   │   ├── user/              # 已完成
    │   │   ├── chat/              # 待开发
    │   │   ├── memory/            # 待开发
    │   │   ├── knowledge/         # 待开发（RAG）
    │   │   └── growth/            # 待开发
    │   └── DeveloperosServerApplication.java
    ├── src/main/resources/
    │   ├── application.yml
    │   ├── application-local.yml
    │   └── db/migration/V1__init_schema.sql
    ├── src/test/java/com/keyx/
    ├── Dockerfile
    └── pom.xml
```

## 三、跳转

- 主服务文档：[developeros-server/CLAUDE.md](./developeros-server/CLAUDE.md)

## 四、变更记录 (Changelog)

| 日期       | 变更内容                                                       |
| ---------- | -------------------------------------------------------------- |
| 2026-07-18 | 新建 `backend/CLAUDE.md`，说明后端总目录职责与未来拆分方向。   |