[根目录](../CLAUDE.md) > **frontend**

# frontend 前端项目

> 导航：[⇧ 返回根目录](../CLAUDE.md) » **frontend**

---

## 一、当前状态

**占位目录**。当前 `frontend/` 尚未初始化任何脚手架代码。规划使用 Vue3 + TypeScript + Element Plus，作为 DeveloperOS 的 Web 客户端。

## 二、规划技术栈

| 类别     | 选型                                          |
| -------- | --------------------------------------------- |
| 框架     | Vue 3 (`<script setup>`)                      |
| 语言     | TypeScript（严格模式）                         |
| 构建工具 | Vite                                          |
| 路由     | Vue Router 4                                  |
| 状态管理 | Pinia                                         |
| UI 组件库 | Element Plus                                  |
| HTTP     | Axios（统一拦截器 + JWT 注入）                |
| Markdown | Markdown-It                                   |

## 三、规划目录结构

> 待脚手架搭建完成后补充。

```text
frontend/
├── src/
│   ├── api/                 # Axios 封装 + 模块 API（auth / user / chat / knowledge / growth）
│   ├── assets/              # 静态资源
│   ├── components/          # 公共组件
│   ├── layouts/             # 布局（DefaultLayout / AuthLayout）
│   ├── router/              # 路由
│   ├── stores/              # Pinia store
│   ├── utils/               # 工具方法
│   ├── views/               # 页面
│   │   ├── auth/            # 登录、注册
│   │   ├── chat/            # AI 对话
│   │   ├── knowledge/       # 知识库
│   │   ├── growth/          # 学习计划与成长
│   │   └── profile/         # 个人中心
│   ├── App.vue
│   └── main.ts
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 四、规划页面（来自 PRD-V1）

1. 登录页 / 注册页
2. 首页 Dashboard
3. AI 聊天页（Markdown 渲染 + 代码高亮 + 流式输出）
4. 知识库页（上传 / 列表 / 检索）
5. 学习计划页（目标 / 任务）
6. 成长记录页（每日 / 每周 / 复盘）
7. 个人中心页（资料 / 偏好 / 密码）

## 五、与后端约定

- BaseURL：`http://localhost:9090`（Docker 中通过 Nginx 反代统一前缀）
- 鉴权：请求头 `Authorization: Bearer <token>`，由 Axios 拦截器自动注入
- 统一响应：`{ code: number, message: string, data: T }`
  - `code === 200` 成功，否则前端统一弹错
- 业务状态码：401 触发重新登录；403 触发禁用提示；423 触发锁定提示
- SSE 流式：用于 Chat 模块，前端用 `EventSource` 或 `fetch + ReadableStream`

## 六、跳转

- 后端接口文档：[../backend/developeros-server/CLAUDE.md](../backend/developeros-server/CLAUDE.md)
- V1 PRD：[../docs/prd/PRD-V1.md](../docs/prd/PRD-V1.md)

## 七、变更记录 (Changelog)

| 日期       | 变更内容                                                       |
| ---------- | -------------------------------------------------------------- |
| 2026-07-18 | 新建 `frontend/CLAUDE.md` 占位文档，描述规划技术栈与未来目录。 |