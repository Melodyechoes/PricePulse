@echo off
chcp 65001 >nul
echo ========================================
echo   Price Pulse Docker 启动脚本
echo ========================================
echo.

docker info >nul 2>&1
if errorlevel 1 (
    echo [错误] Docker 未运行，请先启动 Docker Desktop
    pause
    exit /b 1
)

echo [1/3] 停止已有容器...
docker-compose down

echo.
echo [2/3] 构建并启动（首次需要 5-10 分钟）...
docker-compose up -d --build

echo.
echo [3/3] 启动完成！
echo.
echo ========================================
echo   访问地址:
echo   前端：http://localhost
echo   后端：http://localhost:8080
echo ========================================
echo.
pause
