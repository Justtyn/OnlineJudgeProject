# 仓库贡献指南

## 项目结构与模块组织

- 后端（本目录）为 Spring Boot 服务。
- 代码：`src/main/java/com/example/onlinejudge/`（`controller/`、`service/`、`entity/`、`mapper/`、`task/`、`utils/` 等）。
- 配置/资源：`src/main/resources/`（`application.yml`、`application.properties`、MyBatis XML：`mapper/`、静态资源：`static/`）。
- 测试：`src/test/java/`（JUnit 5 + Spring Boot Test）。
- 构建产物：`target/`（不要提交到 Git）。
- 上传文件可能位于 `uploads/` 和/或 `src/main/resources/static/uploads/`（按运行时数据处理，避免当作源码维护）。

## 构建、测试与本地开发命令

- 环境：Maven 3.x + JDK 8+（项目按 Java `1.8` 编译，见 `pom.xml`）。
- `mvn clean package`：编译并生成可执行 JAR（输出到 `target/`）。
- `mvn test`：运行单元/集成测试。
- `mvn spring-boot:run`：本地启动（默认端口 `9090`，见 `src/main/resources/application.yml`）。
- `java -jar target/OnlineJudge-0.0.1-SNAPSHOT.jar`：运行已打包的应用。
- `docker build -t onlinejudge .`：构建镜像（见 `Dockerfile`）。
- `docker run --rm -p 9090:9090 onlinejudge`：本地运行容器。

## 代码风格与命名约定

- Java：4 空格缩进，不使用 Tab；保持 import 整洁有序（IDE 的 “Optimize Imports”）。
- 包名遵循 `com.example.onlinejudge.<layer>`（如 `controller`、`service`、`mapper`、`entity`）。
- MyBatis：接口位于 `src/main/java/.../mapper/`，XML 位于 `src/main/resources/mapper/`，命名为 `*Mapper.xml`。
- 推荐使用 Lombok 减少样板代码（如 `@Data`、`@Builder`），DTO/实体命名清晰可辨。

## 测试指南

- 测试框架：`spring-boot-starter-test`（JUnit 5）。
- 测试代码放在 `src/test/java`，命名使用 `*Test.java`（需要 Spring 上下文时可用 `*Tests.java` + `@SpringBootTest`）。
- 优先编写快速测试；仅在确有必要时使用 `@SpringBootTest` 拉起完整容器。

## 提交与 PR 规范

- 提交信息保持简短且可读（历史中常见“修复…/优化…”）；避免使用 `1` 这类占位提交。
- PR 至少包含：变更摘要、验证方式（如运行了 `mvn test`/手动步骤）、配置或 API 变更说明（涉及 Swagger 时同步更新/说明）。

## 安全与配置建议

- 不要提交真实凭据：`src/main/resources/application.yml` 建议仅保留本地/开发默认值；优先使用环境变量或通过 `.gitignore` 忽略的 `application-local.yml`。
- 新增配置项时：提供合理默认值，并在 PR 中简要说明用途与示例。

## 代理使用说明

- 与本仓库协作时，优先使用中文沟通与输出。
