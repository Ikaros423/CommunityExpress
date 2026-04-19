-- 一键导入测试数据（MySQL 8）
-- 使用方式（示例）：
-- mysql -u root -p community_express < src/main/resources/sql/run_all_test_data_mysql8.sql

USE community_express;
SET NAMES utf8mb4;

SOURCE src/main/resources/sql/test_data_shelf_info_20_mysql8.sql;
SOURCE src/main/resources/sql/test_data_sys_user_20_mysql8.sql;
SOURCE src/main/resources/sql/test_data_send_order_10_mysql8.sql;
SOURCE src/main/resources/sql/test_data_express_info_100_mysql8.sql;

