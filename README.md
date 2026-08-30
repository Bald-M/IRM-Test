# IRM Monorepo

IRM（Industry Relationship Management）是一个面向学生实习流程的业务系统，
覆盖学生申请、Industry 候选人查看、Admin 管理，以及注册、邮箱验证和密码重置。
仓库已统一为由 Nx 管理的 Vue + Java Monorepo。

## 项目组成

| 项目 | 路径 | 技术 | 主要职责 |
| --- | --- | --- | --- |
| Web | `apps/web` | Vue 3、Vite、TypeScript、Element Plus、Pinia | 公共页面、认证流程、学生端、Admin/Industry 工作台 |
| API | `apps/api` | Java 21、Spring Boot、Spring Security、JPA、Flyway | HTTP 接口、认证授权、业务规则、MySQL 持久化、邮件 |
| 工作区 | 根目录 | Nx 23、npm、Docker Compose | 任务编排、质量门禁、本地基础设施和容器集成 |

Nx 是统一任务入口；Vite 负责前端开发与构建，Maven Wrapper 负责 Java
依赖、测试和打包。

## 环境要求

- Node.js 22.22.2（见 `.node-version` 和 `.nvmrc`）
- npm 10.9 或更高版本
- Java 21
- Docker Desktop/Engine 与 Docker Compose v2

## 快速开始

在仓库根目录执行：

```bash
./scripts/dev.sh
# 等价入口：npm run dev
```

首次运行时，脚本会：

1. 按 `.nvmrc` 检查或切换 Node.js；
2. 在需要时执行 `npm ci`；
3. 从 `.env.example` 创建本地 `.env`，并生成随机数据库口令和 JWT 密钥；
4. 启动并等待 MySQL、Mailpit 就绪；
5. 通过 Nx 同时启动 Web 和 API，并启用前后端热更新。

已有 `.env` 和 MySQL 命名卷不会被覆盖或删除。如果默认基础设施端口被占用，
脚本会选择可用的本地端口，并在启动摘要中打印实际地址。

交互式终端会显示紧凑的 Nx 双任务面板：方向键选择任务，`?` 查看帮助，
`q` 或 Ctrl+C 退出。非交互式环境会自动回退为对齐的实时日志。

| 服务 | 默认地址 |
| --- | --- |
| Web | <http://localhost:4200> |
| API | <http://localhost:7001> |
| API 健康检查 | <http://localhost:7001/actuator/health> |
| Mailpit | <http://localhost:8025> |

Vite 将 `/api` 代理到本地 API。Ctrl+C 只停止 Web/API；MySQL 与 Mailpit
默认保留，以便下次快速启动。需要时可显式停止：

```bash
docker compose stop mysql mailpit
```

## 目录结构

```text
.
├── apps/
│   ├── api/                 Spring Boot API、Flyway 迁移和后端测试
│   └── web/                 Vue 应用、Vite 配置和前端测试
├── docs/
│   ├── architecture.md      运行时模块、接口与安全不变量
│   └── migration-runbook.md Egg.js → Spring Boot 数据迁移手册
├── scripts/dev.sh           一键本地开发入口
├── AGENTS.md                自动化协作规则
├── DESIGN.md                前端设计与交互规范
├── compose.yaml             本地集成基础设施与容器栈
├── nx.json                  Nx 工作区默认行为
└── package.json             工作区命令与 JavaScript 依赖
```

## 常用命令

| 命令 | 用途 |
| --- | --- |
| `npm run dev` | 准备本地环境并启动 Web/API 热更新 |
| `npm run dev:apps` | 只启动两个应用，不准备依赖、`.env` 或基础设施 |
| `npm run dev:web` | 只启动 Vite |
| `npm run dev:api` | 只启动 Spring Boot 开发进程 |
| `npm run lint` | 运行只读 lint |
| `npm run typecheck` | 运行支持该 target 的类型检查 |
| `npm test` | 运行前后端测试 |
| `npm run build` | 构建全部项目 |
| `npm run check` | 依次运行 lint、类型检查、测试和构建 |
| `npm run affected:check` | 只检查受影响的 Nx 项目 |
| `npm run graph` | 查看 Nx 项目与任务图 |

单项目命令使用标准 Nx 语法：

```bash
npx nx test web
npx nx typecheck web
npx nx test api
npx nx run api:verify
```

`dev:apps`、`dev:web` 和 `dev:api` 假设依赖、`.env` 及 MySQL/SMTP 已经准备好；
首次开发应优先使用 `./scripts/dev.sh`。

## 配置与容器

- 根 `.env.example`：完整本地开发和 Compose 的模板；`dev.sh` 会据此生成
  不提交的根 `.env`。
- `apps/api/.env.example`：单独运行 API 时的配置说明，默认关闭邮件发送。
- `apps/web/.env.development` 与 `.env.production`：只包含可公开的 Vite 配置。

API 启动必须获得 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 和 `JWT_SECRET`。
不要提交真实 `.env`，也不要把密码、OTP、JWT 或 reset reference 写入日志。

完整容器栈仅用于本地集成。先确保根 `.env` 已由 `dev.sh` 生成或按模板安全填写，
再执行：

```bash
npm run docker:up
# 停止并保留 MySQL 命名卷
npm run docker:down
```

容器 Web 默认监听 8080；API、Web、MySQL 和 Mailpit 的实际宿主端口以
`.env` 为准。Mailpit 是本地邮件捕获工具，不是生产邮件系统。未经明确授权，
不要执行 `docker compose down -v` 或删除数据库卷。

## 质量门禁与 CI

提交跨项目改动前运行：

```bash
npm run check
```

GitHub Actions 会在 Pull Request 和推送到 `main` 时执行同一命令。API 自动化
测试使用 H2；真实 MySQL 数据与 Flyway 兼容性仍必须按迁移手册在恢复的数据库上验证。

## 安全与迁移提醒

Java API 保留当前 Web 依赖的路径、snake_case JSON 字段、公开角色名和响应结构，
同时修复旧实现中的密码、OTP、JWT 和对象所有权问题。旧数据库值 `Client` 在
公开接口中映射为 `Industry`。

旧 Egg.js 仓库历史中曾包含数据库、Gmail 和 JWT 凭据。删除源码中的默认值并不
会撤销这些凭据，必须在对应系统中轮换。对现有数据库切换 Java API 前，务必执行
[迁移手册](docs/migration-runbook.md)，不要让旧 API 与新 API 同时写入生产数据库。

## 文档索引

- [AGENTS.md](AGENTS.md)：协作边界、变更联动与验证规则
- [DESIGN.md](DESIGN.md)：前端视觉、页面范式、响应式与可访问性规范
- [架构说明](docs/architecture.md)：模块 interface、运行时职责和安全不变量
- [迁移手册](docs/migration-runbook.md)：旧数据库验证、切换与回滚检查
