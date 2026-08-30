# IRM 前端设计指南

本文件定义 `apps/web` 的视觉语言、页面范式和交互验收要求。工程模块、HTTP
interface 与安全不变量见 `docs/architecture.md`；自动化协作规则见 `AGENTS.md`。
具体数值和组件行为以当前样式与源码为准。

## 1. 设计目标

IRM 服务于学生实习申请与候选人协作。界面应传达可信、清晰和可执行，优先帮助用户
理解当前状态与下一步，而不是追求装饰密度。

| 体验面 | 主要用户 | 首要目标 | 当前页面外壳 |
| --- | --- | --- | --- |
| 公共页面 | 未登录访客 | 理解流程、进入注册或登录 | Site Header/Footer、白色到暖橙背景 |
| 认证流程 | Student、Industry | 完成登录、注册、验证和密码重置 | 蓝白渐变、居中表单 |
| Student | 学生 | 查看流程、填写 application、查看 profile | 公共 Header/Footer、暖色内容背景 |
| Admin | 管理员 | 浏览学生列表与详情 | 橙色侧栏、顶部标题、桌面工作区 |
| Industry | 行业合作方 | 浏览候选人列表与详情 | 与 Admin 一致的桌面工作区 |

不同体验面保持同一品牌语言，但不应混用页面密度：认证页不做成工作台，Admin/Industry
列表不使用营销页面布局，Student 表单不隐藏业务进度来换取视觉留白。

## 2. 设计 Token 的权威来源

页面实现前先查以下文件：

- Element Plus 主色：`apps/web/src/assets/theme.css`
- 全局语义色、字体与基础重置：`apps/web/src/assets/base.css`
- 全局断点与应用容器：`apps/web/src/assets/main.css`
- 入口加载顺序：`apps/web/src/main.ts`

当前核心色义：

| 语义 | 当前值 | 用途 |
| --- | --- | --- |
| IRM Orange | `#FE6601` / `--el-color-primary` | 主操作、选中态、侧栏、流程强调 |
| IRM Blue | `#1E5192` | 品牌边框、认证背景、公共页面结构强调 |
| Warm Canvas | `#FFEDD7` | 公共页与 Student 页柔和背景 |
| Work Surface | `#FFFFFF` | 表单、详情和工作台内容面 |
| Neutral Surface | `#F1F1F1`、`#ECECEC` | Header、分组和次级区域 |
| Primary Text | `#2C3E50`、`#3A3541` | 标题和正文 |

规则：

- Element Plus 主题以 `theme.css` 中的 CSS 变量为准。
- 新增跨页面色义时先在 `theme.css` 或 `base.css` 建立语义变量，再由页面使用；
  不要继续扩散相近的橙色、蓝色和灰色硬编码。
- `styles/element/index.scss` 中仍有旧主色值，但当前入口加载的是 Element Plus 默认 CSS
  加 `theme.css`；不要把未接入的 SCSS 当成第二套 token 来源。
- 状态颜色同时配合文字或图标，不能只靠绿色、红色、橙色区分。
- 现有 `base.css` 会响应系统暗色，但多数业务页面仍使用固定浅色背景。暗色模式目前不是
  完整支持的体验；不要只给单个页面增加孤立暗色样式。

## 3. 字体、层级与密度

- 全局字体栈由 `base.css` 定义，基础字号为 15px；新增页面沿用，不单独引入字体。
- 页面必须有一个清晰主标题；辅助说明应解释任务或状态，不重复标题。
- 正文和表单标签保持可读，不使用小于 12px 的关键内容。
- 长姓名、邮箱、公司名和 URL 必须允许换行、截断加 tooltip，或在详情区完整展示。
- 列表中的编号、日期和状态保持稳定对齐；不要用大字号或全大写制造所有层级。
- 工作台以扫描效率为先，公共页和认证页可以更舒展，但都避免卡片层层嵌套。

## 4. 组件选型顺序

1. 复用现有业务组件和相同体验面的页面范式。
2. 使用 Element Plus 的表单、按钮、表格、反馈和弹层组件。
3. 当相同行为出现于多个页面时，提取小 interface、深 implementation 的共享模块。
4. 只有现有实现无法表达需求时才新增组件或全局 token。

代表性复用点：

- 公共 Header/Footer：`SiteHeaderComponent.vue`、`SiteFooterComponent.vue`
- Admin/Industry Header：`PanelHeaderComponent.vue`
- 左侧导航：`AdminPanelLeftNavComponent.vue`、`ClientPanelLeftNavComponent.vue`
- 候选人摘要：`StudentCardComponent.vue`
- 学生详情：`StudentDetailComponent.vue` 及角色专用详情组件
- 请求、路由与安全外链：`utils/http.ts`、`routeAccess.ts`、`navigation.ts`

Element Plus 图标已在应用入口全局注册。已有通用图标可表达动作时，不新增手写 SVG；
业务插图和品牌资产继续放在 `src/assets` 的语义目录中。

## 5. 页面外壳

### 公共页面与 Student

- 公共 Header 使用蓝色品牌结构与橙色主操作；Footer 保持蓝色收尾。
- 主内容使用白色到暖橙的浅色背景，信息区保持足够对比度。
- Student 页面复用公共 Header/Footer，使 application、profile 与公开内容保持连续。
- 页面主操作应与流程顺序一致；不要让次要链接比“继续”“保存”“提交”更醒目。

### 认证流程

- 登录、注册、邮箱验证和密码重置共用 `views/login/LayoutView.vue` 的蓝白渐变背景。
- 表单容器居中，标题、说明、输入、主操作、返回入口从上到下形成单一路径。
- OTP 输入必须显示剩余/重发状态、错误原因和下一步；不能仅改变输入框颜色。
- loading 覆盖实际请求周期，失败后保留用户可安全重试的输入。
- 密码要求在提交前可见，错误文案说明具体缺失条件。

### Admin 与 Industry 工作台

- 当前布局是桌面优先的两列 Grid：240px 左侧导航、100px Header、右侧独立滚动内容区。
- 列表页顺序保持为标题/说明、筛选或动作、数据列表、空/错误态。
- 详情页优先呈现身份、联系方式、application 状态和关键资料，再显示次级字段。
- Admin 与 Industry 可共享布局语言，但导航名称和允许动作必须符合各自角色。
- 若增加窄屏支持，应将侧栏改为可访问的 drawer/折叠导航；不要把 240px 侧栏和表格
  直接压缩到手机宽度。

## 6. 表单、列表与详情

### 表单

- 每个输入有可见 label；placeholder 只能举例，不能替代 label。
- 必填、格式、服务端冲突和权限错误靠近触发位置显示，并保留恢复路径。
- 多步骤 application 明确当前位置、已完成状态和下一步；返回上一步不丢失输入。
- 主按钮防止重复提交；请求进行中禁用并显示进行状态。
- 保存成功必须来自服务端成功响应，不以本地状态变化伪装成功。

### 列表

- 姓名、状态和主要操作应在不打开详情时可识别。
- loading 使用稳定占位或明确加载状态；空状态说明为什么为空以及用户能做什么。
- 大数据量不能无限一次加载。新增列表能力时评估分页、筛选和最小字段投影。
- 整行可点击时仍需键盘访问和清晰 focus；链接与按钮不能只绑定 mouse 事件。

### 详情

- 相同 student 字段在 Student、Admin、Industry 体验中使用一致标签和格式。
- 邮箱、LinkedIn、GitHub、portfolio 等外链必须使用安全导航 helper，并明确新窗口行为。
- 缺失值显示一致的空值文案，不展示 `null`、`undefined` 或原始 JSON。
- `internship_options`、`preferred_companies` 当前来自 JSON 字符串；解析失败时显示可恢复
  错误，不让整个详情页崩溃。

## 7. 状态与反馈

每个数据页面都要设计以下适用状态：

- 首次 loading；
- 有数据；
- 空数据；
- 输入或校验错误；
- 网络/服务端失败；
- 未登录或 token 过期；
- 已登录但角色不匹配；
- 重复提交或请求仍在处理中。

401 应引导重新登录，403 应解释当前角色无权访问，404 应提供返回公共首页的动作。
错误页面和通知不得展示堆栈、内部路径、数据库字段、token 或完整请求对象。

## 8. 响应式

当前样式使用三个主要范围：

- Mobile：小于或等于 768px；
- Tablet：768px–992px；
- Desktop：大于或等于 992px。

交付前至少检查 1440、1024、768 和 375px。重点检查：

- Header 导航、品牌图和主操作是否拥挤；
- 认证表单是否超出视口，键盘弹出后能否滚动到错误与按钮；
- application 长表单是否保留输入与步骤上下文；
- 表格、详情长文本和外链是否溢出；
- fixed/backtop 控件是否遮挡内容。

Admin/Industry 当前是桌面优先体验。没有完成导航折叠、表格响应式和触摸验收前，
不要宣称其已完整支持移动端。

## 9. 可访问性与内容

- 键盘 focus 必须清楚；Dialog 打开后焦点进入合理位置，关闭后返回触发点。
- 主要点击目标建议不小于 44×44px，图标按钮提供可访问名称。
- 图片根据用途提供 alt；纯装饰图片使用空 alt，不能把业务信息只放在图片中。
- 文本、边框和禁用态保持可辨对比度；颜色不是唯一状态线索。
- 尊重 `prefers-reduced-motion`；关键反馈不能依赖动画才能理解。
- 当前产品文案以英文为主。新增文案保持同一语言、角色名和字段术语；若引入本地化，
  应集中管理词条，不在单个页面混入另一套语言。
- 面向用户使用 `Industry`，不要显示遗留数据库术语 `Client`。

## 10. 安全交互

- 前端隐藏入口不是授权；越权响应不得转换成普通空状态。
- 不在 DOM、通知、URL、截图示例或 console 中显示 JWT、OTP、reset reference 或密码。
- 外部 URL 只接受 `http`/`https`，新窗口使用 `noopener,noreferrer`。
- 密码重置公开响应不得泄露邮箱是否存在，也不要通过不同 loading 时间或文案恢复枚举。
- 高风险动作在执行前说明影响，并在失败后保留恢复路径。

## 11. Do / Don't

### Do

- 先找同一体验面的现有页面，再设计新结构。
- 复用 token、Element Plus 和共享请求/路由模块。
- 让用户随时知道当前状态、下一步和失败后的恢复方式。
- 为真实长文本、空值、慢请求和权限错误设计，而不只验证理想数据。
- 修改共享视觉规则时同步更新本文件和代表性页面。

### Don't

- 不新增相近但无语义区别的橙色、蓝色、阴影和圆角。
- 不用 placeholder 代替 label，不用颜色代替状态文案。
- 不在页面内创建独立 Axios、认证或外链逻辑。
- 不把桌面侧栏和表格简单缩小后称为移动适配。
- 不把内部异常、原始对象或安全凭据展示给用户。

## 12. 页面交付检查清单

### 信息与业务

- [ ] 页面目标、当前状态和主操作在首屏可识别。
- [ ] 角色、字段标签、流程顺序和操作后果与现有业务一致。
- [ ] loading、空、错误、无权限和重复提交状态已覆盖。

### 视觉与组件

- [ ] 使用现有 token、Element Plus 和正确体验面的页面范式。
- [ ] 没有无理由新增的硬编码色、字号、阴影或 z-index。
- [ ] 长文本、空值、状态和真实数据不会破坏布局。

### 响应式与可访问性

- [ ] 已检查适用的 1440、1024、768 和 375px。
- [ ] 键盘 focus、表单 label、Dialog 焦点与点击目标可用。
- [ ] 颜色不是唯一状态线索，动效遵守 reduced-motion。

### 安全与验证

- [ ] 无 token、OTP、reset reference、内部错误或对象内容泄漏。
- [ ] 外链安全，越权状态没有伪装为空数据。
- [ ] 已运行相关 lint、typecheck、测试和构建，并记录未运行项。

## 13. 参考实现

- 应用入口与全局样式：`apps/web/src/main.ts`、`src/assets/{base,main,theme}.css`
- 公共外壳：`views/HomeView.vue`、`views/student/LayoutView.vue`
- 认证外壳：`views/login/LayoutView.vue`
- Student application：`views/student/ApplicationView.vue`
- Admin 工作台：`views/admin/LayoutView.vue`、`views/admin/StudentsListView.vue`
- Industry 工作台：`views/client/LayoutView.vue`、`views/client/CandidatesListView.vue`
- 错误页面：`views/401View.vue`、`views/404View.vue`
- 共享交互：`src/utils/http.ts`、`routeAccess.ts`、`navigation.ts`

当本文件与实现不一致时，先确定应更新规范还是修复实现，并在同一个变更中消除漂移。
