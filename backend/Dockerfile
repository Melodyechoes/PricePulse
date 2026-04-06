FROM amazoncorretto:21-alpine

WORKDIR /app

# 安装 Maven
RUN apk add --no-cache maven git

# 复制父 POM 和 backend POM
COPY ../pom.xml ./pom.xml
COPY ../backend/pom.xml ./backend/pom.xml

# 进入 backend 目录
WORKDIR /app/backend

# 复制源代码（先复制，这样 Maven 构建时才能找到）
COPY ../backend/src ./src

# 直接构建项目（Maven 会自动下载需要的依赖）
# 不使用 dependency:go-offline，因为某些依赖可能在中央仓库不存在
RUN mvn clean package -DskipTests -B

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/backend-1.0.0.jar"]
