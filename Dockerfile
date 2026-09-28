# ============================================================
# excel-import-service 运行镜像
#
# 前提：jar 已在本地用 gradlew bootJar 打好，与 Dockerfile 一起上传。
#   因此这里不装 Gradle/JDK，只装 JRE —— 镜像小、构建快、
#   不依赖服务器网络拉依赖，几乎不会因网络失败。
#
# 构建目录：Dockerfile 与 excel-import-service.jar 必须在同一目录
#   （COPY 是相对构建目录的路径），推荐 /opt/excel-import-service
#
# ⚠ 本文件用 CRLF 换行，因此所有 RUN 一律写成单行、不用反斜杠续行：
#   续行遇到 CRLF 有把 \r 带进命令的风险，单行写法对换行符免疫。
# ============================================================
FROM eclipse-temurin:21-jre

ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8

# JVM 参数：可在 1Panel 环境变量里覆盖（改这个不用重新构建镜像）
# 解析大 Excel 若出现 OOM，优先调大 -Xmx
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=192m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

WORKDIR /app

# 产物名由 build.gradle 的 bootJar.archiveFileName 固定，不随版本号变化
COPY excel-import-service.jar /app/excel-import-service.jar

# 挂载点：应用日志 / 堆转储（compose 里把 /app/logs 映射到宿主机磁盘）
RUN mkdir -p /app/logs

# 非 root 运行，降低容器逃逸风险。
#   · 不用 UID/GID 1000：基础镜像 eclipse-temurin:21-jre 里 1000 已被占用，
#     groupadd -g 1000 会报 "GID '1000' already exists" 直接构建失败。
#     （img-service 用 useradd -r，-r 从系统号段 100-999 里挑空闲号，所以躲过了）
#     改用高位 10001：常规用户从 1000 起、系统用户到 999 止，几乎不可能被占，
#     同时保持固定不变 —— 宿主机 bind mount 的 ./logs 才能用确定的 chown 授权，
#     而不必 chmod 777（世界可写，不安全）。
#   · 单行不续行，见文件头说明。
RUN groupadd -g 10001 app && useradd -u 10001 -g 10001 -M -d /app -s /bin/sh app && chown -R app:app /app
USER app

# 容器内外统一 8083（宿主机 8082 已被占用）
EXPOSE 8083

# 用 exec 让 java 直接成为 PID 1：
#   1) docker stop 的 SIGTERM 才能被 JVM 收到，走 Spring 优雅停机
#   2) 不带 exec 时 SIGTERM 只发给 sh，JVM 收不到，容器要等 10s 被 SIGKILL 强杀
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/excel-import-service.jar --spring.profiles.active=prod"]
