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

## API Conventions
- Base module path: `/system`.
- Response wrapper: `ApiResponse<T>` with fields `code`, `message`, `data`.
  - Success: `code=200`, `message=success` or custom.
- Error handling: `RuntimeException` => `400`, other `Exception` => `500`.

## Implemented APIs
### ExpressInfoController (`/system/expressInfo`)
- `GET /list`
  - Returns `ApiResponse<List<ExpressInfo>>`.
- `POST /checkin`
  - Body: `ExpressCheckinRequest` (trackingNumber, logisticsCompany, sizeType, receiverName, receiverPhone, shelfCode, shelfLayer, remark, useRecommendShelf).
  - Returns `ApiResponse<ExpressInfo>`.
- `POST /checkout`
  - Body: `ExpressCheckoutRequest` with fields `trackingNumber`, `pickupPhone`.
  - Uses service `checkOut(trackingNumber, pickupPhone)`.

### ShelfInfoController (`/system/shelfInfo`)
- Currently empty.

### SysUserController (`/system/sysUser`)
- Currently empty.

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

### Shelf recommendation
- `IShelfInfoService.getRecommendShelf(sizeType)`:
  - Filters by `shelf_type=sizeType` and `status=1`.
  - Orders by:
    - Not full first: `current_usage < total_capacity`.
    - Then lowest usage ratio.

### Shelf usage update
- `updateUsage(shelfId, delta)` clamps `current_usage` to minimum `0`.
- Over-capacity allowed but logs a warning.

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
- `phone`, `email`, `avatar`
- `role` (ADMIN/STAFF/USER)
- `status` (0 disabled, 1 active)
- `create_time`, `update_time`
- `is_deleted` (logical delete)

## Known Gaps / TODO
- Implement Shelf and User APIs in `ShelfInfoController` and `SysUserController`.
- Consider DTOs + validation annotations for request bodies.
- Add authentication/authorization and role-based access if required.
- Add tests for check-in / check-out and shelf usage updates.

## Test Data
- `src/main/resources/sql/test_data_mysql8.sql` seeds sample shelves and expresses.
