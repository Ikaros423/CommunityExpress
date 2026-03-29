-- CommunityExpress 测试数据（MySQL 8）
-- 使用说明：
-- 1) 先确保数据库为 community_express
-- 2) 执行本脚本后，可直接测试 checkin / checkout

USE community_express;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS express_user_binding (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    express_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_express_user (express_id, user_id)
);

CREATE TABLE IF NOT EXISTS send_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    sender_address VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(64) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    package_type TINYINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0
);

-- ========= 清理旧测试数据 =========
DELETE FROM express_user_binding WHERE user_id IN (1, 2, 3);
DELETE FROM send_order WHERE user_id IN (1, 2, 3);

DELETE FROM express_info
WHERE tracking_number IN
      ('SF100000001', 'ZT100000002', 'YT100000003', 'JD100000004', 'YD100000005',
       'ST100000006', 'YD100000007',
       'DB70000001', 'DB70000002', 'DB70000003', 'DB70000004', 'DB70000005', 'DB70000006', 'DB70000007',
       'DB70000008', 'DB70000009', 'DB70000010', 'DB70000011', 'DB70000012', 'DB70000013', 'DB70000014');

DELETE FROM shelf_info
WHERE shelf_code IN (101, 102, 201, 301, 401);

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
(102, 1, '标准小件货架102-1层', 0, 20, 0, '门口区', 9,  1, NOW(), NOW(), 0),
(201, 1, '大件货架201-1层',   1, 10, 1, '仓库区', 8,  1, NOW(), NOW(), 0),
(301, 1, '冷链货架301-1层',   2, 8,  1, '冷链区', 9,  1, NOW(), NOW(), 0),
(401, 1, '易碎货架401-1层',   3, 6,  0, '易碎区', 7,  1, NOW(), NOW(), 0);

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
('YD100000005', '韵达', 3, '周七', '13800000005', NULL, '101-1-5566', 101, 1, 3, 0, NOW(), NOW(), '已退回状态测试单'),
('ST100000006', '申通', 0, '钱八', '13800000006', NULL, '101-1-6677', 101, 1, 1, 0, NOW(), NOW(), '标准件待取件补充单'),
('YD100000007', '韵达', 0, '孙九', '13800000007', NULL, '102-1-7788', 102, 1, 0, 0, NOW(), NOW(), '待入库状态测试单');

-- ========= 看板近7天趋势演示数据 =========
-- 每天2条：1条待取件（入库），1条已取件（用于出库趋势）。
INSERT INTO express_info
(tracking_number, logistics_company, size_type, receiver_name, receiver_phone, pickup_phone, pickup_code, shelf_code, shelf_layer, status, is_deleted, create_time, update_time, remark)
VALUES
('DB70000001', '顺丰', 0, '演示用户A1', '13700000001', NULL,         '101-1-7001', 101, 1, 1, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:15:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '09:15:00'), '看板演示-6天前入库待取件'),
('DB70000002', '中通', 1, '演示用户B1', '13700000002', '13900000002', '201-1-7002', 201, 1, 2, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '10:20:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 6 DAY), '18:30:00'), '看板演示-6天前已取件'),
('DB70000003', '顺丰', 0, '演示用户A2', '13700000003', NULL,         '101-2-7003', 101, 2, 1, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:10:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '09:10:00'), '看板演示-5天前入库待取件'),
('DB70000004', '圆通', 0, '演示用户B2', '13700000004', '13900000003', '102-1-7004', 102, 1, 2, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '11:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY), '16:45:00'), '看板演示-5天前已取件'),
('DB70000005', '申通', 1, '演示用户A3', '13700000005', NULL,         '201-1-7005', 201, 1, 1, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:55:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:55:00'), '看板演示-4天前入库待取件'),
('DB70000006', '京东', 2, '演示用户B3', '13700000006', '13900000002', '301-1-7006', 301, 1, 2, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '14:15:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY), '20:10:00'), '看板演示-4天前已取件'),
('DB70000007', '韵达', 0, '演示用户A4', '13700000007', NULL,         '101-1-7007', 101, 1, 1, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '10:05:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '10:05:00'), '看板演示-3天前入库待取件'),
('DB70000008', '中通', 3, '演示用户B4', '13700000008', '13900000001', '401-1-7008', 401, 1, 2, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '12:20:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY), '19:05:00'), '看板演示-3天前已取件'),
('DB70000009', '顺丰', 0, '演示用户A5', '13700000009', NULL,         '101-2-7009', 101, 2, 1, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:40:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:40:00'), '看板演示-2天前入库待取件'),
('DB70000010', '圆通', 1, '演示用户B5', '13700000010', '13900000002', '201-1-7010', 201, 1, 2, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '13:05:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '17:50:00'), '看板演示-2天前已取件'),
('DB70000011', '申通', 2, '演示用户A6', '13700000011', NULL,         '301-1-7011', 301, 1, 1, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:45:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:45:00'), '看板演示-昨天入库待取件'),
('DB70000012', '京东', 0, '演示用户B6', '13700000012', '13900000003', '102-1-7012', 102, 1, 2, 0, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '15:35:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '21:00:00'), '看板演示-昨天已取件'),
('DB70000013', '韵达', 3, '演示用户A7', '13700000013', NULL,         '401-1-7013', 401, 1, 1, 0, TIMESTAMP(CURDATE(), '09:25:00'), TIMESTAMP(CURDATE(), '09:25:00'), '看板演示-今天入库待取件'),
('DB70000014', '中通', 0, '演示用户B7', '13700000014', '13900000001', '101-1-7014', 101, 1, 2, 0, TIMESTAMP(CURDATE(), '11:30:00'), TIMESTAMP(CURDATE(), '18:20:00'), '看板演示-今天已取件');

-- ========= 用户测试数据 =========
-- 密码默认：123456（BCrypt）
INSERT INTO sys_user
(username, password, nickname, email, role, status, create_time, update_time, is_deleted)
VALUES
('13900000001', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '系统管理员', 'admin@example.com', 'ADMIN', 1, NOW(), NOW(), 0),
('13900000002', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '驿站员工',   'staff@example.com', 'STAFF', 1, NOW(), NOW(), 0),
('13900000003', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '普通用户',   'user@example.com',  'USER',  1, NOW(), NOW(), 0);
