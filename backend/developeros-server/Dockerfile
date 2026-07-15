# ============================================
# 第 1 阶段：编译（build）
# ============================================
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# 先复制 pom.xml（利用 Docker 缓存层）
COPY pom.xml .

# 预下载依赖（-B 静默模式，日志更干净）
RUN mvn dependency:go-offline -B

# 再复制源代码
COPY src ./src

# 编译打包，跳过测试
RUN mvn package -DskipTests -B


# ============================================
# 第 2 阶段：运行（runtime）
# ============================================
FROM eclipse-temurin:21-jre-alpine

# 镜像元数据
LABEL maintainer="keyx" \
      version="1.0.0" \
      description="DeveloperOS Spring Boot Backend"

# 设置时区为中国（避免日志时间差 8 小时）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# JVM 容器感知参数（关键！避免容器内 OOM）
# - XX:+UseContainerSupport        自动识别容器内存限制（JDK 11+ 默认开启）
# - XX:MaxRAMPercentage=75.0       最多使用容器内存的 75%
# - XX:+UseG1GC                    使用 G1 垃圾回收器（适合大内存）
# - Djava.security.egd=...         加速 Tomcat 启动（避免熵源阻塞）
# - Duser.timezone                 JVM 时区
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom \
               -Duser.timezone=Asia/Shanghai"

WORKDIR /app

# 从 build 阶段复制 jar
COPY --from=build /app/target/developeros-server-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 9090

# 启动命令（用 sh 展开 JAVA_OPTS 环境变量）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]