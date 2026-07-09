# DeveloperOS 数据库设计（Database Design）

> 文档版本：V1.0
> 文档状态：Draft
> 创建日期：2026-07-09
> 最后更新：2026-07-09

------

# 一、设计目标

DeveloperOS V1 采用模块化数据库设计，遵循以下原则：

- 面向 MVP，避免过度设计；
- 高内聚、低耦合；
- 支持后续功能扩展；
- 优先保证开发效率；
- 满足 AI、RAG 和长期记忆场景需求。

------

# 二、数据库技术选型

| 技术       | 用途                 |
| ---------- | -------------------- |
| PostgreSQL | 主数据库             |
| Redis      | 缓存、会话、临时记忆 |
| pgvector   | 向量检索             |

------

# 三、数据库模块划分

## 用户模块

- user

------

## 聊天模块

- conversation
- message

------

## 长期记忆模块

- memory

------

## 知识库模块

- knowledge_document
- knowledge_chunk

------

## 成长模块

- learning_goal
- learning_task
- growth_log

------

# 四、核心表清单

| 模块         | 表名               | 说明         |
| ------------ | ------------------ | ------------ |
| 用户模块     | user               | 用户信息     |
| 聊天模块     | conversation       | 会话信息     |
| 聊天模块     | message            | 消息记录     |
| 长期记忆模块 | memory             | 用户长期记忆 |
| 知识库模块   | knowledge_document | 知识文档     |
| 知识库模块   | knowledge_chunk    | 文档切片     |
| 成长模块     | learning_goal      | 学习目标     |
| 成长模块     | learning_task      | 学习任务     |
| 成长模块     | growth_log         | 成长日志     |

------

# 五、ER 关系图

```text
user
 ├── conversation
 │      └── message
 │
 ├── memory
 │
 ├── knowledge_document
 │      └── knowledge_chunk
 │
 ├── learning_goal
 │      └── learning_task
 │
 └── growth_log
```

------

# 六、表关系说明

| 主表               | 子表               | 关系   |
| ------------------ | ------------------ | ------ |
| user               | conversation       | 一对多 |
| conversation       | message            | 一对多 |
| user               | memory             | 一对多 |
| user               | knowledge_document | 一对多 |
| knowledge_document | knowledge_chunk    | 一对多 |
| user               | learning_goal      | 一对多 |
| learning_goal      | learning_task      | 一对多 |
| user               | growth_log         | 一对多 |

------

# 七、模块职责

## 用户模块

负责：

- 用户信息管理
- 用户偏好管理
- 用户设置管理

------

## 聊天模块

负责：

- AI 对话
- 会话管理
- 消息记录
- 多轮上下文

------

## 长期记忆模块

负责：

- 用户目标记忆
- 用户偏好记忆
- 项目经验记忆
- 重要信息沉淀

------

## 知识库模块

负责：

- 文档上传
- 文档切片
- 向量化处理
- RAG 检索增强

------

## 成长模块

负责：

- 学习目标管理
- 学习任务管理
- 成长日志记录
- 成长轨迹沉淀

------

# 八、后续扩展原则

后续新增功能遵循以下原则：

## 优先新增模块表

例如：

- reminder
- github_account
- repository
- agent
- post

------

## 优先新增关联表

例如：

- user_tag
- document_tag
- memory_tag

------

## 谨慎修改核心表结构

尽量避免频繁修改：

- user
- conversation
- message

等核心表。

------

# 九、后续规划

下一阶段将继续完善：

- 表字段设计
- 索引设计
- Redis Key 设计
- 数据字典
- 数据库初始化脚本

------

# 十、数据库演进路线

```text
V1
├── user
├── conversation
├── message
├── memory
├── knowledge_document
├── knowledge_chunk
├── learning_goal
├── learning_task
└── growth_log

V2
├── reminder
├── github_account
└── repository

V3
├── agent
├── agent_memory
└── agent_task

V4
├── post
├── comment
└── like
```