# 个人记账 Web App

这是一个个人使用的记账 Web App，第一版目标是完成流水记录、查询、统计和图表展示。

## 项目结构

- `backend`：Spring Boot 后端
- `frontend`：Vue 3 前端
- `docker-compose.yml`：可选的本地 MySQL 8 配置
- `.env.example`：环境变量示例
- `backend/src/main/resources/application-local.yml`：本机开发数据库配置

## 本地启动

1. 启动 MySQL：

```bash
docker start mymysql
```

如果你想使用项目自带的 MySQL Compose 配置，也可以执行：

```bash
docker-compose up -d
```

2. 启动后端：

```bash
cd "/Users/xuke/Documents/New project"
/Users/xuke/Documents/program/apache-maven-3.9.9/bin/mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

后端默认地址是 `http://localhost:8080`。

3. 启动前端：

```bash
cd "/Users/xuke/Documents/New project/frontend"
npm install
npm run dev
```

前端默认地址是 `http://localhost:5173`。

## 本地数据库配置

当前本机开发配置在：

```text
backend/src/main/resources/application-local.yml
```

默认连接你已有的 Docker MySQL：

- 容器名：`mymysql`
- 地址：`localhost:3306`
- 数据库：`personal_finance`
- 用户名：`root`
- 密码：`123456`

这个文件包含本地密码，已经加入 `.gitignore`，不要提交到 Git。

## 登录配置

当前第一版是单用户登录：

- 账号：`admin`
- 密码：`123456`

本地开发配置在：

```text
backend/src/main/resources/application-local.yml
```

通用配置支持通过环境变量覆盖：

```bash
APP_AUTH_USERNAME=admin
# 公网部署优先使用 BCrypt 哈希，不建议直接放明文密码
APP_AUTH_PASSWORD_HASH=你的_BCrypt_密码哈希
APP_AUTH_SESSION_HOURS=12
APP_AUTH_COOKIE_SECURE=true
APP_AUTH_MAX_FAILED_ATTEMPTS=5
APP_AUTH_LOCK_MINUTES=10
```

如果未来部署到公网，请务必使用强密码，并优先配置 `APP_AUTH_PASSWORD_HASH`。本地开发仍然可以使用 `APP_AUTH_PASSWORD` 明文配置。登录连续失败超过限制后，会临时锁定一段时间，降低暴力破解风险。

## 已实现功能

- 单用户登录保护
- 流水新增、编辑、删除、查询
- 日期范围、类型、分类、支付方式、备注关键词筛选
- 本月收入、本月支出、本月结余统计
- 月度收支趋势图
- 支出分类占比图
- 工资收入统计

## 后端接口

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/transactions`
- `GET /api/transactions/{id}`
- `POST /api/transactions`
- `PUT /api/transactions/{id}`
- `DELETE /api/transactions/{id}`
- `GET /api/statistics/summary`
- `GET /api/statistics/monthly`
- `GET /api/statistics/category`
- `GET /api/statistics/daily`
- `GET /api/statistics/salary`
