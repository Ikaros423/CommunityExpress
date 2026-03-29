-- CommunityExpress 核心3表小范围重构脚本（MySQL 8）
-- 适用场景：本地可重置库 + 一次性切换
-- 重构范围：express_info / shelf_info / send_order
-- 目标：统一字段约束与默认值，补齐 CHECK 约束，提升模型可维护性

USE community_express;

SET NAMES utf8mb4;

-- ============================================================
-- 0) 前置清理（允许重复执行脚本）
-- ============================================================
DROP TABLE IF EXISTS express_info_new;
DROP TABLE IF EXISTS shelf_info_new;
DROP TABLE IF EXISTS send_order_new;

-- 可选：如果你确认不需要旧备份，可先清理历史备份表
-- DROP TABLE IF EXISTS express_info_bak;
-- DROP TABLE IF EXISTS shelf_info_bak;
-- DROP TABLE IF EXISTS send_order_bak;

-- ============================================================
-- 1) 创建新表结构（带约束与索引）
-- ============================================================

CREATE TABLE express_info_new (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tracking_number VARCHAR(64) NOT NULL COMMENT '快递单号',
    logistics_company VARCHAR(64) NOT NULL DEFAULT '未知' COMMENT '物流公司',
    size_type TINYINT COMMENT '规格: 0标准,1大件,2冷链,3易碎',
    receiver_name VARCHAR(64) COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收件人手机号',
    pickup_code VARCHAR(64) COMMENT '取件码',
    shelf_code INT COMMENT '货架编号',
    shelf_layer INT COMMENT '货架层数',
    pickup_phone VARCHAR(20) COMMENT '实际取件人手机号',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待入库,1待取件,2已取件,3已退回',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删,1已删',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(255) COMMENT '备注',
    CONSTRAINT chk_express_size_type CHECK (size_type IN (0, 1, 2, 3) OR size_type IS NULL),
    CONSTRAINT chk_express_status CHECK (status IN (0, 1, 2, 3)),
    CONSTRAINT chk_express_is_deleted CHECK (is_deleted IN (0, 1)),
    KEY idx_express_tracking_number (tracking_number),
    KEY idx_express_deleted_status_create_id (is_deleted, status, create_time, id),
    KEY idx_express_deleted_create_time (is_deleted, create_time),
    KEY idx_express_deleted_status_update_time (is_deleted, status, update_time),
    KEY idx_express_deleted_shelf (is_deleted, shelf_code, shelf_layer)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递信息表(重构版)';

CREATE TABLE shelf_info_new (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    shelf_code INT NOT NULL DEFAULT 0 COMMENT '货架编号',
    shelf_layer INT NOT NULL DEFAULT 0 COMMENT '货架层数',
    shelf_name VARCHAR(128) COMMENT '货架名称',
    shelf_type TINYINT NOT NULL DEFAULT 0 COMMENT '货架类型: 0标准,1大件,2冷链,3易碎',
    total_capacity INT NOT NULL DEFAULT 0 COMMENT '总容量',
    current_usage INT NOT NULL DEFAULT 0 COMMENT '当前占用',
    location_area VARCHAR(128) COMMENT '区域',
    priority INT NOT NULL DEFAULT 0 COMMENT '推荐优先级',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0维修,1可用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删,1已删',
    CONSTRAINT chk_shelf_type CHECK (shelf_type IN (0, 1, 2, 3)),
    CONSTRAINT chk_shelf_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_shelf_capacity CHECK (total_capacity >= 0),
    CONSTRAINT chk_shelf_usage CHECK (current_usage >= 0),
    CONSTRAINT chk_shelf_is_deleted CHECK (is_deleted IN (0, 1)),
    KEY idx_shelf_code_layer (shelf_code, shelf_layer),
    KEY idx_shelf_deleted_type_status (is_deleted, shelf_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='货架信息表(重构版)';

CREATE TABLE send_order_new (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    sender_phone VARCHAR(20) NOT NULL COMMENT '寄件人手机号',
    sender_address VARCHAR(255) NOT NULL DEFAULT '' COMMENT '寄件地址',
    receiver_name VARCHAR(64) COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收件人手机号',
    receiver_address VARCHAR(255) NOT NULL DEFAULT '' COMMENT '收件地址',
    package_type TINYINT NOT NULL DEFAULT 0 COMMENT '包裹类型: 0标准,1大件,2冷链,3易碎',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待处理,1已受理,2已寄出,3已取消',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0未删,1已删',
    CONSTRAINT chk_send_order_package_type CHECK (package_type IN (0, 1, 2, 3)),
    CONSTRAINT chk_send_order_status CHECK (status IN (0, 1, 2, 3)),
    CONSTRAINT chk_send_order_is_deleted CHECK (is_deleted IN (0, 1)),
    KEY idx_send_order_deleted_user_status_update (is_deleted, user_id, status, update_time),
    KEY idx_send_order_deleted_status_update (is_deleted, status, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寄件申请表(重构版)';

-- ============================================================
-- 2) 数据迁移与清洗
-- ============================================================

INSERT INTO express_info_new
(
    id, tracking_number, logistics_company, size_type, receiver_name, receiver_phone,
    pickup_code, shelf_code, shelf_layer, pickup_phone, status, is_deleted,
    create_time, update_time, remark
)
SELECT
    id,
    COALESCE(NULLIF(TRIM(tracking_number), ''), CONCAT('MIG-', id)) AS tracking_number,
    COALESCE(NULLIF(TRIM(logistics_company), ''), '未知') AS logistics_company,
    CASE WHEN size_type IN (0, 1, 2, 3) THEN size_type ELSE NULL END AS size_type,
    receiver_name,
    COALESCE(NULLIF(TRIM(receiver_phone), ''), '') AS receiver_phone,
    pickup_code,
    shelf_code,
    shelf_layer,
    pickup_phone,
    CASE WHEN status IN (0, 1, 2, 3) THEN status ELSE 0 END AS status,
    CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END AS is_deleted,
    COALESCE(create_time, NOW()) AS create_time,
    COALESCE(update_time, COALESCE(create_time, NOW())) AS update_time,
    remark
FROM express_info;

INSERT INTO shelf_info_new
(
    id, shelf_code, shelf_layer, shelf_name, shelf_type, total_capacity, current_usage,
    location_area, priority, status, create_time, update_time, is_deleted
)
SELECT
    id,
    COALESCE(shelf_code, 0) AS shelf_code,
    COALESCE(shelf_layer, 0) AS shelf_layer,
    shelf_name,
    CASE WHEN shelf_type IN (0, 1, 2, 3) THEN shelf_type ELSE 0 END AS shelf_type,
    GREATEST(COALESCE(total_capacity, 0), 0) AS total_capacity,
    GREATEST(COALESCE(current_usage, 0), 0) AS current_usage,
    location_area,
    COALESCE(priority, 0) AS priority,
    CASE WHEN status IN (0, 1) THEN status ELSE 1 END AS status,
    COALESCE(create_time, NOW()) AS create_time,
    COALESCE(update_time, COALESCE(create_time, NOW())) AS update_time,
    CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END AS is_deleted
FROM shelf_info;

INSERT INTO send_order_new
(
    id, user_id, sender_phone, sender_address, receiver_name, receiver_phone, receiver_address,
    package_type, status, remark, create_time, update_time, is_deleted
)
SELECT
    id,
    COALESCE(user_id, 0) AS user_id,
    COALESCE(NULLIF(TRIM(sender_phone), ''), '') AS sender_phone,
    COALESCE(sender_address, '') AS sender_address,
    receiver_name,
    COALESCE(NULLIF(TRIM(receiver_phone), ''), '') AS receiver_phone,
    COALESCE(receiver_address, '') AS receiver_address,
    CASE WHEN package_type IN (0, 1, 2, 3) THEN package_type ELSE 0 END AS package_type,
    CASE WHEN status IN (0, 1, 2, 3) THEN status ELSE 0 END AS status,
    remark,
    COALESCE(create_time, NOW()) AS create_time,
    COALESCE(update_time, COALESCE(create_time, NOW())) AS update_time,
    CASE WHEN is_deleted = 1 THEN 1 ELSE 0 END AS is_deleted
FROM send_order;

-- ============================================================
-- 3) 切换表（保留原表备份）
-- ============================================================

DROP TABLE IF EXISTS express_info_bak;
DROP TABLE IF EXISTS shelf_info_bak;
DROP TABLE IF EXISTS send_order_bak;

RENAME TABLE
    express_info TO express_info_bak,
    express_info_new TO express_info,
    shelf_info TO shelf_info_bak,
    shelf_info_new TO shelf_info,
    send_order TO send_order_bak,
    send_order_new TO send_order;

-- ============================================================
-- 4) 验证
-- ============================================================

-- SHOW CREATE TABLE express_info;
-- SHOW CREATE TABLE shelf_info;
-- SHOW CREATE TABLE send_order;

-- SELECT COUNT(*) AS express_cnt_old FROM express_info_bak;
-- SELECT COUNT(*) AS express_cnt_new FROM express_info;
-- SELECT COUNT(*) AS shelf_cnt_old FROM shelf_info_bak;
-- SELECT COUNT(*) AS shelf_cnt_new FROM shelf_info;
-- SELECT COUNT(*) AS send_order_cnt_old FROM send_order_bak;
-- SELECT COUNT(*) AS send_order_cnt_new FROM send_order;

-- ============================================================
-- 5) 确认无误后可清理备份表（谨慎执行）
-- ============================================================
-- DROP TABLE IF EXISTS express_info_bak;
-- DROP TABLE IF EXISTS shelf_info_bak;
-- DROP TABLE IF EXISTS send_order_bak;
