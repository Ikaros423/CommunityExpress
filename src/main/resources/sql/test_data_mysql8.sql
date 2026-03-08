-- CommunityExpress 测试数据（MySQL 8）
-- 使用说明：
-- 1) 先确保数据库为 community_express
-- 2) 执行本脚本后，可直接测试 checkin / checkout

USE community_express;

SET NAMES utf8mb4;

-- ========= 清理旧测试数据 =========
DELETE FROM express_info
WHERE tracking_number IN ('SF100000001', 'ZT100000002', 'YT100000003', 'JD100000004');

DELETE FROM shelf_info
WHERE shelf_code IN ('A-01', 'B-01', 'C-01');

-- ========= 货架测试数据 =========
-- shelf_type: 0-标准小件, 1-大件, 2-冷链, 3-易碎
-- status: 0-维修中, 1-可用
INSERT INTO shelf_info
(shelf_code, shelf_name, shelf_type, total_capacity, current_usage, location_area, priority, status, create_time, update_time, is_deleted)
VALUES
('A-01', '标准小件货架A01', 0, 20, 2, '门口区', 10, 1, NOW(), NOW(), 0),
('B-01', '大件货架B01',   1, 10, 1, '仓库区', 8,  1, NOW(), NOW(), 0),
('C-01', '冷链货架C01',   2, 8,  0, '冷链区', 9,  1, NOW(), NOW(), 0);

-- ========= 快递测试数据 =========
-- status: 0-待入库, 1-待取件, 2-已取件, 3-已退回
-- 注意：pickup_code 999111 可直接用于 checkout 联调
INSERT INTO express_info
(tracking_number, logistics_company, size_type, receiver_name, receiver_phone, pickup_code, shelf_location, shelf_id, status, is_deleted, create_time, update_time, remark)
SELECT
    'SF100000001', '顺丰', 0, '张三', '13800000001', '999111', s.shelf_code, s.id, 1, 0, NOW(), NOW(), '可直接核销的测试单'
FROM shelf_info s
WHERE s.shelf_code = 'A-01'
LIMIT 1;

INSERT INTO express_info
(tracking_number, logistics_company, size_type, receiver_name, receiver_phone, pickup_code, shelf_location, shelf_id, status, is_deleted, create_time, update_time, remark)
SELECT
    'ZT100000002', '中通', 1, '李四', '13800000002', '999222', s.shelf_code, s.id, 1, 0, NOW(), NOW(), '大件待取件测试单'
FROM shelf_info s
WHERE s.shelf_code = 'B-01'
LIMIT 1;

INSERT INTO express_info
(tracking_number, logistics_company, size_type, receiver_name, receiver_phone, pickup_code, shelf_location, shelf_id, status, is_deleted, create_time, update_time, remark)
SELECT
    'YT100000003', '圆通', 0, '王五', '13800000003', '999333', s.shelf_code, s.id, 2, 0, NOW(), NOW(), '已取件状态测试单'
FROM shelf_info s
WHERE s.shelf_code = 'A-01'
LIMIT 1;

-- 该条用于测试 checkin（可不依赖已有快递数据）
INSERT INTO express_info
(tracking_number, logistics_company, size_type, receiver_name, receiver_phone, pickup_code, shelf_location, shelf_id, status, is_deleted, create_time, update_time, remark)
SELECT
    'JD100000004', '京东', 2, '赵六', '13800000004', '999444', s.shelf_code, s.id, 1, 0, NOW(), NOW(), '冷链待取件测试单'
FROM shelf_info s
WHERE s.shelf_code = 'C-01'
LIMIT 1;
