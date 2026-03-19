-- CommunityExpress 测试数据（MySQL 8）
-- 使用说明：
-- 1) 先确保数据库为 community_express
-- 2) 执行本脚本后，可直接测试 checkin / checkout

USE community_express;

SET NAMES utf8mb4;

-- ========= 清理旧测试数据 =========
DELETE FROM express_info
WHERE tracking_number IN
      ('SF100000001', 'ZT100000002', 'YT100000003', 'JD100000004', 'YD100000005');

DELETE FROM shelf_info
WHERE shelf_code IN (101, 201, 301);

DELETE FROM sys_user
WHERE username IN ('13900000001', '13900000002', '13900000003');

-- ========= 货架测试数据 =========
-- shelf_type: 0-标准小件, 1-大件, 2-冷链, 3-易碎
-- status: 0-维修中, 1-可用
INSERT INTO shelf_info
(shelf_code, shelf_layer, shelf_name, shelf_type, total_capacity, current_usage, location_area, priority, status, create_time, update_time, is_deleted)
VALUES
(101, 1, '标准小件货架101-1层', 0, 20, 2, '门口区', 10, 1, NOW(), NOW(), 0),
(101, 2, '标准小件货架101-2层', 0, 20, 1, '门口区', 10, 1, NOW(), NOW(), 0),
(201, 1, '大件货架201-1层',   1, 10, 1, '仓库区', 8,  1, NOW(), NOW(), 0),
(301, 1, '冷链货架301-1层',   2, 8,  0, '冷链区', 9,  1, NOW(), NOW(), 0);

-- ========= 快递测试数据 =========
-- status: 0-待入库, 1-待取件, 2-已取件, 3-已退回
-- 注意：checkout 以 tracking_number 查询，并在服务端写入 pickup_phone
INSERT INTO express_info
(tracking_number, logistics_company, size_type, receiver_name, receiver_phone, pickup_phone, pickup_code, shelf_code, shelf_layer, status, is_deleted, create_time, update_time, remark)
VALUES
('SF100000001', '顺丰', 0, '张三', '13800000001', NULL, '101-1-1234', 101, 1, 1, 0, NOW(), NOW(), '待取件-可直接核销'),
('ZT100000002', '中通', 1, '李四', '13800000002', NULL, '201-1-2233', 201, 1, 1, 0, NOW(), NOW(), '大件待取件测试单'),
('YT100000003', '圆通', 0, '王五', '13800000003', '13900000003', '101-2-3344', 101, 2, 2, 0, NOW(), NOW(), '已取件状态测试单'),
('JD100000004', '京东', 2, '赵六', '13800000004', NULL, '301-1-4455', 301, 1, 1, 0, NOW(), NOW(), '冷链待取件测试单'),
('YD100000005', '韵达', 3, '周七', '13800000005', NULL, '101-1-5566', 101, 1, 3, 0, NOW(), NOW(), '已退回状态测试单');

-- ========= 用户测试数据 =========
-- 密码默认：123456（BCrypt）
INSERT INTO sys_user
(username, password, nickname, email, avatar, role, status, create_time, update_time, is_deleted)
VALUES
('13900000001', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiJj7T8e1koXSPo6e7i0rGUNShUMp1K', '系统管理员', 'admin@example.com', NULL, 'ADMIN', 1, NOW(), NOW(), 0),
('13900000002', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiJj7T8e1koXSPo6e7i0rGUNShUMp1K', '驿站员工',   'staff@example.com', NULL, 'STAFF', 1, NOW(), NOW(), 0),
('13900000003', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiJj7T8e1koXSPo6e7i0rGUNShUMp1K', '普通用户',   'user@example.com',  NULL, 'USER',  1, NOW(), NOW(), 0);
