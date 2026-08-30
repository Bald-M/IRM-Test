# IRM Agent Guide

本文件适用于整个仓库，面向在 IRM 中工作的自动化 Agent。它记录稳定的协作规则、
业务不变量和变更联动；容易变化的路由、字段和依赖版本以可执行源码为准。

## 1. 开始工作前

1. 阅读根 `README.md`、本文件，以及与任务相关的 `DESIGN.md` 或 `docs/*`。
2. 检查 `git status`、当前分支和已有差异；现有改动可能属于用户或其他 Agent。
3. 先确认目标模块及其 interface，再决定需要联动的 Web、API、数据库和文档文件。
4. 优先使用仓库已有 Nx target、Maven Wrapper、测试工具和实现范式。
5. 不覆盖、不回滚、不顺手格式化与当前任务无关的改动。

## 2. 文档与权威来源

| 内容 | 权威来源 |
| --- | --- |
| 安装、启动、命令与文档导航 | `README.md` |
| 自动化协作、联动规则与交付要求 | `AGENTS.md` |
| 前端视觉、页面范式与可访问性 | `DESIGN.md` |
| 模块 interface、运行时职责与安全不变量 | `docs/architecture.md` |
| 旧数据库验证、生产切换与回滚 | `docs/migration-runbook.md` |
| 工作区任务 | `package.json`、`nx.json`、各应用 `project.json` |
| HTTP 行为 | Controller、DTO、Service 与 Web 调用代码 |
| 数据库结构 | 已按顺序执行的 Flyway migration |

实现、配置和文档不一致时，先判断是实现偏离还是文档过时；同一个变更应修正两者，
不要建立第二套重复的真相源。

## 3. 不可违反的规则

- 不得部署、发布、上传或托管到 ChatGPT/OpenAI 运营的服务，包括 OpenAI Sites、
  `chatgpt.site` 或任何同类平台。
- 未经用户明确授权，不执行生产部署、外部发布、push 或其他影响仓库外部状态的操作。
- 不输出、提交或记录 `.env`、数据库口令、SMTP 凭据、JWT、OTP、reset reference、
  token、真实个人资料或生产数据。只允许提交已脱敏的 `.env.example`。
- 不删除或重建 MySQL 命名卷，不执行破坏性 SQL，不运行 `docker compose down -v`，
  除非用户明确授权且已核实目标与备份。
- 不修改已经执行过的 Flyway migration；使用新的、更高版本 migration 修复已有数据库。
- 不绕过服务端认证、角色授权、账户状态、对象所有权、OTP 状态机或 URL 校验。
- 不让旧 Egg.js API 与 Java API 同时作为生产数据库写入者。
- 不编辑 `node_modules`、`.nx`、`dist`、`target`、`coverage` 等生成目录。

## 4. 项目地图

| 模块 | 路径 | 职责 |
| --- | --- | --- |
| Web | `apps/web` | Vue 页面、路由、Pinia 状态、浏览器侧 HTTP adapter、前端测试 |
| API | `apps/api` | Java 领域规则、HTTP interface、认证授权、持久化、邮件 adapter、后端测试 |
| 开发入口 | `scripts/dev.sh` | 版本检查、本地配置、MySQL/Mailpit 和 Nx 开发进程 |
| 集成配置 | `package*.json`、`nx.json`、`compose.yaml` | 跨模块任务、依赖、缓存和容器拓扑 |
| 文档 | `README.md`、`DESIGN.md`、`docs` | 上手、设计、架构和迁移约束 |

并行工作必须分配不重叠的文件所有权。根级集成文件一次只由一个 Agent 编辑；其他
Agent 应读取最新内容并适配，而不是覆盖共享改动。

## 5. 业务与权限边界

IRM 的核心流程是学生注册与验证、登录、实习申请、Industry 候选人查看和 Admin
管理查看。

- 可公开注册的角色只有 `Student` 和 `Industry`。
- 旧数据库角色 `Client` 对外映射为 `Industry`；不要在 Web 或新 HTTP 响应中重新
  暴露 `Client`。
- `IRM User` 是兼容旧数据的角色，目前没有独立前端体验面；不要把它误当成 Admin。
- 账户状态为 `Pending`、`Active`、`Blocked`、`Removed`。受保护请求只允许有效的
  `Active` 身份。
- Student 只能读取和更新自己的 application。
- Admin 与 Industry 可以读取候选人列表和详情，但不能依赖前端隐藏入口完成授权。
- 路由 guard 是用户体验层；Spring Security、方法授权和对象所有权检查才是安全层。

认证、OTP、密码与 token 的完整不变量见 `docs/architecture.md`。修改这些流程时，
必须覆盖成功、过期、重复、冷却、未知邮箱、错误角色和越权路径。

## 6. 模块与 interface 约束

- Nx 是仓库级任务 interface；Vite 负责 Web，Maven Wrapper 负责 Java。不要增加绕过
  Nx 的平行根脚本，除非它解决 Nx 无法表达的启动准备工作。
- Web 与 API 的外部 seam 是 `/api` JSON/HTTP。除非同步修改双方、测试和文档，
  必须保持 endpoint、snake_case 字段、公开角色名和既有响应结构。
- 受保护请求统一使用 `Authorization: Bearer <token>`；浏览器请求复用
  `apps/web/src/utils/http.ts`，不要在页面内各建 Axios 实例。
- Controller 只处理协议转换、Bean Validation 与授权声明；事务、OTP、密码、token
  和所有权规则放在 Service 模块的 implementation 中。
- Repository 是 API implementation 的内部 seam，不跨 HTTP 暴露实体操作。
- 邮件是可替换的真实 seam；新增邮件行为通过 `MailPort` 及 adapter 实现，不在
  Controller 或 Service 中直接创建 SMTP 客户端。
- 运行时配置来自环境变量。不要添加可工作的默认凭据，也不要让测试配置进入生产。

## 7. 前端实现约定

- 使用 Vue 3、TypeScript、Vue Router、Pinia 和 Element Plus；新增代码遵循所在文件
  已有 Composition API/`script setup` 范式。
- 全局认证请求复用 `src/utils/http.ts`，路由访问判断复用 `routeAccess.ts`，外部链接
  复用 `navigation.ts`。修复共享行为应集中在这些模块，而不是散落到页面。
- 新增受保护页面时同时更新 Router、导航入口、角色 meta、route-access 测试和服务端
  授权。前端角色判断不能替代后端检查。
- 认证和 application 页面变更需要挂载页面测试，至少断言请求体、失败提示、成功跳转
  和重复提交保护；只测试工具函数不足以覆盖用户流程。
- 对 loading、空数据、错误、无权限和超时提供明确状态，不把 console 日志当作反馈。
- 视觉、响应式、内容和可访问性要求见 `DESIGN.md`。

## 8. Java API 实现约定

- 使用 Java 21、Spring Boot、Spring Security、Spring Data JPA、Flyway 和 Maven Wrapper。
- DTO 在进入业务模块前完成结构校验；统一错误结构由全局异常处理器输出，不向客户端
  暴露堆栈、SQL、实体或内部路径。
- 密码只保存安全摘要；JWT 与 OTP 只保存不可逆摘要。不要降低散列、过期、限流、
  单次使用或撤销策略来简化实现。
- JWT 既要验证签名和过期时间，也要与 token 表及当前用户身份、角色、状态一致。
- 外部资料 URL 只允许绝对 `http`/`https`；新增 URL 字段必须经过相同校验。
- 数据库 schema 通过 Flyway 演进，Hibernate 保持 `ddl-auto: validate`。Entity、
  Repository、migration 和集成测试必须在同一变更中更新。
- API 测试默认使用 H2 MySQL compatibility mode；涉及 migration 或 MySQL 方言的变更
  还必须在真实 MySQL 测试库验证。

## 9. 本地数据与基础设施

- 根 `.env.example` 面向完整本地环境；`apps/api/.env.example` 面向独立 API。
- `scripts/dev.sh` 可以创建新的根 `.env`，但已有文件永不覆盖。不要在 shell 中
  `source .env`；JDBC URL 中的字符并不保证符合 shell 赋值语义。
- `compose.yaml` 的 MySQL 与 Mailpit 端口只绑定 `127.0.0.1`。不要为方便调试改为所有
  宿主接口。
- Mailpit 只用于本地捕获测试邮件，不得用于生产或暴露到公共网络。
- 已初始化 MySQL 卷不会因 `.env` 密码变化自动更新用户密码；遇到不匹配时先恢复旧
  配置或制定迁移方案，不得自动删卷。

## 10. 变更联动清单

| 变更 | 必查位置 |
| --- | --- |
| 新页面或路由 | Router、布局/导航、角色 meta、route guard、页面状态、页面测试、`DESIGN.md` |
| 新 HTTP endpoint | Controller、DTO、Service、SecurityConfig、Web 调用、契约测试、架构文档 |
| 认证或密码流程 | 前后端状态、公开响应防枚举、OTP/JWT 摘要、过期/撤销、失败与重复请求测试 |
| 新角色或权限 | `Role` 映射、Spring Security、对象所有权、Router meta、导航、401/403 体验、测试 |
| 数据库字段或表 | Entity、Repository、Service、新 Flyway migration、H2/MySQL 验证、迁移手册 |
| 新邮件行为 | `MailPort`、SMTP/no-op adapter、模板/locale、禁用模式、Mailpit 测试 |
| 新环境变量 | `application.yml`、两个 `.env.example`、Compose、CI、README（仅当用户需要知道） |
| Nx target 或依赖 | `project.json`、`nx.json`、`package*.json`、CI、README 命令说明 |

## 11. 验证与交付

使用 Node 22.22.2、npm 10.9+、Java 21 和 Docker Compose v2。安装依赖使用
`npm ci`；只有明确更新依赖时才使用 `npm install`，不要手工编辑 lockfile。

```bash
# 全工作区门禁
npm run check

# Web
npx nx lint web
npx nx typecheck web
npx nx test web
npx nx build web

# API
npx nx test api
npx nx build api
npx nx run api:verify

# 配置与启动脚本
docker compose --env-file .env.example config --quiet
bash -n scripts/dev.sh
node --check apps/api/tools/dev.mjs
node --check apps/api/tools/maven.mjs
```

按风险选择检查，但跨 Web/API、认证、数据库或根配置的变更应运行 `npm run check`。
UI 变更至少检查相关桌面/移动宽度、loading/空/错误/无权限状态和键盘操作。交付时
说明已运行的检查、未运行项及原因；不要仅凭编译成功宣称行为正确。

## 12. Git 与部署

- 编辑和暂存前先检查 `git status`；确认移动、删除和新文件都属于当前任务。
- 默认使用 `codex/<topic>` 分支。提交信息优先使用 Conventional Commit，并让一个
  commit 对应一个可解释的逻辑变更。
- 未经用户明确要求，不 commit、amend、rebase、push、建 PR 或改写历史。
- 即使用户授权部署，也必须是明确命名的非 ChatGPT/OpenAI 目标；本仓库默认只在本地
  工作。

## 13. 权威源码索引

- Web 路由：`apps/web/src/router/index.ts`
- Web HTTP adapter：`apps/web/src/utils/http.ts`
- Web 访问判断：`apps/web/src/utils/routeAccess.ts`
- Web 设计 token：`apps/web/src/assets/theme.css`、`base.css`
- API 配置：`apps/api/src/main/resources/application.yml`
- HTTP interface：`apps/api/src/main/java/nz/ac/wintec/irm/web`
- 认证与授权：`apps/api/src/main/java/nz/ac/wintec/irm/security`
- 业务规则：`apps/api/src/main/java/nz/ac/wintec/irm/service`
- 数据库 migration：`apps/api/src/main/resources/db/migration`
- 工作区任务：`package.json`、`nx.json`、`apps/*/project.json`
- 本地拓扑：`compose.yaml`、`scripts/dev.sh`

当本文件与实现不一致时，以经过测试的可执行源码为准，并在同一变更中修正文档。
