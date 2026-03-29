# CommunityExpress Agent Memory

## Purpose
Quick reference for project structure, API conventions, database schema, and open work items.

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- MyBatis-Plus 3.5.5
- Knife4j OpenAPI (starter)
- MySQL 8

## Project Structure (key paths)
- `src/main/java/com/express/system/CommunityExpressApplication.java`: Spring Boot entry point, `@MapperScan("com.express.system.mapper")`.
- `src/main/java/com/express/system/controller`: REST controllers.
- `src/main/java/com/express/system/service` + `impl`: Service interfaces and implementations.
- `src/main/java/com/express/system/entity`: MyBatis-Plus entities.
- `src/main/java/com/express/system/mapper`: Mapper interfaces.
- `src/main/resources/mapper`: Mapper XMLs (currently empty).
- `src/main/java/com/express/system/common`: `ApiResponse`, `GlobalExceptionHandler`.
- `src/main/resources/application.properties`: datasource config.
- `src/main/resources/sql/test_data_mysql8.sql`: test data script.
- `CommunityExpress.postman_collection.json`: Postman collection with variables and auto-id scripts.
- `web/`: Vue 3 前端（Vite + Naive UI + Pinia + Vue Router + Axios）。

## API Conventions
- Base module path: `/system`.
- Response wrapper: `ApiResponse<T>` with fields `code`, `message`, `data`.
  - Success: `code=200`, `message=success` or custom.
- Error handling: `RuntimeException` => `400`, other `Exception` => `500`.

## Implemented APIs
### ExpressInfoController (`/system/expresses`)
- `GET /system/expresses`
  - Query params: `trackingNumber`, `receiverPhone`, `status`, `shelfCode`, `shelfLayer`, `sizeType` (all optional).
  - Query param: `overdueOnly`（员工/管理员可用；`true` 时筛选 48h+ 滞留且按入库时间升序）。
  - Returns `ApiResponse<List<ExpressInfo>>`.
  - USER 角色默认按“本人手机号 + 已绑定包裹”查询。
- `GET /system/expresses/{id}`
  - Returns `ApiResponse<ExpressInfo>`.
- `POST /system/expresses`
  - Body: `ExpressCheckinRequest` (trackingNumber, logisticsCompany, sizeType, receiverName, receiverPhone, shelfCode, shelfLayer, remark, useRecommendShelf).
  - Returns `ApiResponse<ExpressInfo>`.
- `POST /system/expresses/{trackingNumber}/checkout`
  - Uses service `checkOut(trackingNumber, operatorUsernameAsPhone, role, userId)` and writes `pickup_phone`.
  - USER 仅允许出库“本人手机号包裹或本人已绑定包裹”。
- `POST /system/expresses/claim`
  - Body: `trackingNumber`, `receiverPhone`。
  - USER 通过“单号 + 收件手机号”认领包裹并绑定到当前账号可见范围。
- `PUT /system/expresses/{id}`
  - Body: `ExpressInfo`.
  - Updates non-shelf fields; `sizeType` must be changed via `relocate`.
- `DELETE /system/expresses/{id}`
  - Deletes express; if status=1, releases shelf usage.
- `POST /system/expresses/{id}/relocate`
  - Body: `ExpressRelocateRequest` (shelfCode, shelfLayer, sizeType).
  - If `shelfCode` or `shelfLayer` is missing, auto-assigns via recommend shelf.

### ShelfInfoController (`/system/shelves`)
- `GET /system/shelves`
  - Query params: `shelfType`, `status`, `shelfCode`, `shelfLayer` (all optional).
  - Returns `ApiResponse<List<ShelfInfo>>`.
- `GET /system/shelves/{id}`
  - Returns `ApiResponse<ShelfInfo>`.
- `GET /system/shelves/lookup`
  - Query params: `shelfCode`, `shelfLayer`.
  - Returns `ApiResponse<ShelfInfo>`.
- `GET /system/shelves/recommend`
  - Query param: `sizeType`.
  - Returns `ApiResponse<ShelfInfo>`.
- `GET /system/shelves/load`
  - Query params: `shelfType`, `status`, `shelfCode`, `shelfLayer`。
  - 返回 `ApiResponse<List<ShelfLoadVO>>`（`currentUsage/totalCapacity/loadRate`）。
- `POST /system/shelves`
  - Body: `ShelfInfo`.
  - Returns `ApiResponse<ShelfInfo>`.
- `PUT /system/shelves/{id}`
  - Body: `ShelfInfo`.
  - Returns `ApiResponse<ShelfInfo>`.
- `DELETE /system/shelves/{id}`
  - Returns `ApiResponse<Boolean>`.

### SysUserController (`/system/users`)
- `GET /system/users`
  - Query params: `username`, `role`, `status` (optional).
  - Returns `ApiResponse<List<SysUser>>` (password masked).
- `GET /system/users/{id}`
  - Returns `ApiResponse<SysUser>` (password masked).
- `POST /system/users`
  - Body: `SysUserCreateRequest` (username, password, role required).
  - Returns `ApiResponse<SysUser>`.
- `PUT /system/users/{id}`
  - Body: `SysUserUpdateRequest`.
  - Returns `ApiResponse<SysUser>`.
- `DELETE /system/users/{id}`
  - Returns `ApiResponse<Boolean>`.
- `POST /system/users/register`
  - Body: `SysUserRegisterRequest` (username=手机号, password, code required).
  - Returns `ApiResponse<SysUser>`.
- `POST /system/users/login`
  - Body: `SysUserLoginRequest` (account, password).
  - Returns `ApiResponse<SysUserLoginResponse>` (token + user).
- `POST /system/users/refresh`
  - Returns `ApiResponse<SysUserTokenResponse>` (token).
- `POST /system/users/password-reset/request`
  - Body: `phone` (手机号/username).
  - 仅手机号存在时发送验证码；验证码输出到日志。
- `POST /system/users/sms-code/request`
  - Body: `phone`, `bizType` (`REGISTER` / `PASSWORD_RESET`)。
  - 统一短信验证码发送入口（验证码仅日志打印，10 分钟有效）。
- `POST /system/users/password-reset/confirm`
  - Body: `phone`, `code`, `newPassword`.
  - 校验验证码后重置密码（BCrypt）。

### SendOrderController (`/system/send-orders`)
- `POST /system/send-orders`
  - USER 创建寄件申请（寄件手机号以后端登录账号为准）。
- `GET /system/send-orders`
  - USER 查询本人申请；STAFF/ADMIN 可按状态/寄件手机号筛选。
- `PUT /system/send-orders/{id}/status`
  - STAFF/ADMIN 更新状态，限制合法流转：`0->1/3`, `1->2/3`。

### DashboardController (`/system/dashboard`)
- `GET /system/dashboard/summary`
  - 核心指标：`totalExpress`, `pendingPickup`, `overdue48h`, `todayCheckin`, `todayCheckout`, `pendingSendOrders`。
- `GET /system/dashboard/trend?days=7`
  - 近 N 天趋势：`date`, `checkinCount`, `checkoutCount`。
- `GET /system/dashboard/ranks`
  - 榜单：`topLoadShelves`, `topOverdueExpresses`。
- 权限：仅 STAFF/ADMIN。

## Frontend (Vue 3)
- 目录：`web/`
- 技术栈：Vue 3 + Vite + Naive UI + Pinia + Vue Router + Axios。
- 角色路由：USER/STAFF/ADMIN，根据 JWT + 登录响应控制权限。
- 页面模块：
  - 登录 / 注册 / 忘记密码（短信验证码）
  - USER：快递查询、出库快捷入口、添加包裹（认领）
  - STAFF：快递入库/换柜/管理，货架管理
  - ADMIN：用户管理（含角色限制）
  - USER/STAFF/ADMIN：寄件管理（`/send-orders`）
  - STAFF/ADMIN：首页管理看板（指标卡 + 7天趋势图 + 榜单）
- 编辑弹窗：快递/货架/用户列表均提供编辑更新弹窗（NModal + NForm），提交后刷新列表并保留筛选条件。
- 表单校验：手机号、密码长度与后端规则一致；必填项前端拦截（手机号正则 `^1\\d{10}$`，密码 6-20）。
- 请求约定：统一 `ApiResponse`，`/system` 代理到后端；API 已对接到可直接演示。
- 交互优化：列表请求带 loading；错误提示优先展示后端 message。
- 用户端出库入口：快递查询页顶部提供单号出库输入框与按钮，仅 USER 可见。
- 看板图表：ECharts 按需引入（`echarts/core`），并修复刷新后角色恢复时图表不显示问题（监听角色变化后加载）。

## Business Logic Notes
### Express check-in
- Requires: `trackingNumber`, `receiverPhone`, `sizeType`.
- Rejects duplicate `trackingNumber`.
- Shelf selection:
  - If `useRecommendShelf=true`: `IShelfInfoService.getRecommendShelf(sizeType)`.
  - Else: `IShelfInfoService.getByCodeAndLayer(shelfCode, shelfLayer)` and `status==1`.
- Assigns: `shelfCode`, `shelfLayer`, `pickupCode` (format `shelfCode-shelfLayer-XXXX`), `status=1`.
- Sets `createTime`, `updateTime` to now.
- Persists `ExpressInfo` and increments shelf usage by `+1`.

### Express check-out
- Requires: `trackingNumber`, `pickupPhone`.
- Validates status `==1` (awaiting pickup) and has `shelfCode` + `shelfLayer`.
- Loads shelf by code+layer and decrements shelf usage by `-1`.
- Updates `status=2`, `pickupPhone`, `updateTime`.
- USER 出库增加归属校验；STAFF/ADMIN 按原权限可操作。

### Express update / relocate / delete
- `update` allows editing common fields (receiver/logistics/remark/phones) and `trackingNumber`/`status`.
- `update` does not change shelf fields or `sizeType`.
- `relocate` handles shelf movement and `sizeType` changes, and regenerates `pickupCode`.
- `delete` releases shelf usage if the express is still in status `1`.

### Shelf recommendation
- `IShelfInfoService.getRecommendShelf(sizeType)`:
  - Filters by `shelf_type=sizeType` and `status=1`.
  - Orders by:
    - Not full first: `current_usage < total_capacity`.
    - Then lowest usage ratio.

### Shelf management
- `IShelfInfoService.listByFilter(...)` supports multi-field filtering and orders by `shelf_code`, `shelf_layer`.
- `createShelf` validates required fields and enforces unique `(shelf_code, shelf_layer)`.
- `updateShelf` checks existence and duplicate `(shelf_code, shelf_layer)` conflicts.
- `deleteShelf` uses logical deletion via MyBatis-Plus (`@TableLogic` on `is_deleted`).

### Shelf usage update
- `updateUsage(shelfId, delta)` clamps `current_usage` to minimum `0`.
- Over-capacity allowed but logs a warning.

### SysUser module
- Role type uses enum `UserRole` (ADMIN/STAFF/USER), stored as string.
- Username is the phone number (regex `^1\\d{10}$`); `phone` field removed.
- Passwords are BCrypt-hashed; login uses username.
- `SysUserInitializer` auto-creates default admin (`13900000001`/`123456`) if no admin exists.
- JWT auth enabled: login returns token; token TTL 2h; Authorization `Bearer <token>`.
- Controller requests use validation annotations; validation errors return 400 with message.
- 管理员权限：允许修改自身信息，但不能改角色；不能删除管理员账号；不能修改其他管理员账号。
- 忘记密码：短信验证码重置（仅日志输出），验证码内存存储，10 分钟有效，最多 5 次尝试。
- 注册：接入短信验证码校验（`REGISTER` 场景）。

### Send order module
- 状态：`0-待处理, 1-已受理, 2-已寄出, 3-已取消`。
- 创建时寄件手机号以登录账号为准，避免伪造。
- 状态更新限制合法流转，禁止回退。

### Dashboard module
- Summary/Trend/Ranks 三个聚合接口，均为只读。
- 趋势按天统计近 N 天（默认 7，最大 30），空日期补 0。
- 滞留定义：`status=1 && create_time <= now-48h`。

## Database Schema (from entities)
### `express_info`
- `id` (PK, auto)
- `tracking_number`
- `logistics_company`
- `size_type` (0 standard, 1 large, 2 fragile, 3 cold chain)
- `receiver_name`
- `receiver_phone`
- `pickup_code` (generated: `shelfCode-shelfLayer-XXXX`)
- `shelf_code`
- `shelf_layer`
- `pickup_phone`
- `status` (0 pending check-in, 1 waiting pickup, 2 picked, 3 returned)
- `is_deleted` (logical delete)
- `create_time`, `update_time`, `remark`

### `shelf_info`
- `id` (PK, auto)
- `shelf_code` (integer code)
- `shelf_layer` (integer)
- `shelf_name`
- `shelf_type` (0 standard, 1 large, 2 cold chain, 3 fragile)
- `total_capacity`, `current_usage`
- `location_area`, `priority`
- `status` (0 maintenance, 1 available)
- `create_time`, `update_time`
- `is_deleted` (logical delete)

### `sys_user`
- `id` (PK, auto)
- `username`, `password`, `nickname`
- `email`, `avatar`
- `role` (enum: ADMIN/STAFF/USER)
- `status` (0 disabled, 1 active)
- `create_time`, `update_time`
- `is_deleted` (logical delete)

### `express_user_binding`
- `id` (PK, auto)
- `express_id`, `user_id`
- `create_time`, `update_time`
- `is_deleted` (logical delete)
- unique key `(express_id, user_id)`

### `send_order`
- `id` (PK, auto)
- `user_id`
- `sender_phone`, `sender_address`
- `receiver_name`, `receiver_phone`, `receiver_address`
- `package_type`, `status`, `remark`
- `create_time`, `update_time`
- `is_deleted` (logical delete)

## Known Gaps / TODO
- 可补充 DashboardService 统计口径的服务层单测（当前已覆盖 DashboardController）。

## Test Data
- `src/main/resources/sql/test_data_mysql8.sql` seeds shelves/express/users/send_order/binding。
- 已补充近 7 天入库/出库演示数据（`DB70000001` ~ `DB70000014`），用于管理看板趋势展示。

## Tests
- Controller 单元测试使用 MockMvc + Mock Service，不依赖数据库。
- 覆盖：`SysUserControllerTest`, `ExpressInfoControllerTest`, `ShelfInfoControllerTest`, `SendOrderControllerTest`, `DashboardControllerTest`。
- `CommunityExpressApplicationTests` 为 contextLoads，标记为跳过（避免数据库依赖）。
- 前端暂无自动化测试。
