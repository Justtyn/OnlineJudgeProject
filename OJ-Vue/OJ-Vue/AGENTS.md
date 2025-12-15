# 仓库指南

## 项目结构
- `src/`：前端源码目录。
  - `src/main.js`：应用入口（Vue 3）与全局插件初始化。
  - `src/router/`：Vue Router 路由配置与守卫（页面多为懒加载）。
  - `src/stores/`：Pinia 状态管理（例如 `pageState.ts`）。
  - `src/views/`：页面级视图（主要在 `src/views/page/` 与 `src/views/admin/`）。
  - `src/components/`：可复用组件。
  - `src/assets/`：全局样式与静态资源（会被代码引用）。
- `public/`：原样复制到构建产物中的静态文件。
- `dist/`：生产构建输出（生成目录，请勿手改）。

## 构建、测试与开发命令
- `npm install`：安装依赖。
- `npm run dev`：启动 Vite 开发服务器。
- `npm run build`：构建生产包到 `dist/`（建议作为提交/发 PR 前的基础自检）。
- `npm run preview`：本地预览已构建的产物（Vite preview）。

## 配置与环境变量
- 环境文件：`.env.development`、`.env.production`（Vite 读取 `VITE_*` 变量）。
- 后端 API 基地址：通过 `VITE_BASE_URL` 配置；本地代理规则见 `vite.config.js`。
- 不要提交任何密钥（API Key/Token 等）。本地个人配置请使用 `.env.local`，并确保不进入 Git。

## 代码风格与命名规范
- 以“就近一致”为准；本仓库常见风格为单引号、无分号。
- 组件/页面：`PascalCase.vue`（如 `UserProfilePage.vue`）；工具/组合式/通用函数：`camelCase`。
- 优先使用 `@/…`（指向 `src/`）导入，避免过深的相对路径。

## 测试指南
- 当前 `package.json` 未配置自动化测试命令/框架。
- 如需引入测试：建议就近放置并命名为 `*.spec.ts`/`*.spec.js`，同时补充 `npm run test` 脚本。

## 提交与 Pull Request 规范
- 历史提交多为简短的祈使句摘要（如“修复…/优化…”）；请保持信息量，避免使用 `1` 这类占位提交信息。
- PR 请包含：改动内容与原因、UI 改动截图、以及任何配置/环境变量变更说明（如 `VITE_BASE_URL`）。

## Additional Notes
- Always respond in Chinese.
