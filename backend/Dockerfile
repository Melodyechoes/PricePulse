FROM amazoncorretto:21-alpine

WORKDIR /app

# 安装 Maven
RUN apk add --no-cache maven git

# 复制父 POM 和 backend POM
COPY ../pom.xml ./pom.xml
COPY ../backend/pom.xml ./backend/pom.xml

# 进入 backend 目录
WORKDIR /app/backend

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY ../backend/src ./src

# 构建项目
RUN mvn clean package -DskipTests -B

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/backend-1.0.0.jar"]
