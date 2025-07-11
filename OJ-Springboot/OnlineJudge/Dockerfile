# -------------------------------
# 1. 构建阶段：用本地已有的 Amazon Corretto 21 + Maven 镜像
# -------------------------------
FROM maven:3.9.9-amazoncorretto-21 AS build
WORKDIR /workspace

# 只复制 pom.xml，先离线下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package \
    -DskipTests \
    -Dspring-boot.repackage.skip=false

# -------------------------------
# 2. 运行阶段：直接继承 build 阶段，不再拉任何新的 JDK 镜像
# -------------------------------
FROM build AS runtime
WORKDIR /app

# 拷贝打好的 fat-jar
COPY --from=build /workspace/target/OnlineJudge-0.0.1-SNAPSHOT.jar app.jar

# 暴露 9090（与你 application.yml 中 server.port 保持一致）
EXPOSE 9090

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

