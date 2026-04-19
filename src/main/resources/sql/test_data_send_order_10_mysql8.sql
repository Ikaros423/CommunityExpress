-- send_order 测试数据 10 条
USE community_express;
SET NAMES utf8mb4;

-- 清理本批数据（按 sender_phone 前缀）
DELETE FROM send_order WHERE sender_phone LIKE '1888800%';

INSERT INTO send_order
(id, user_id, sender_phone, sender_address, receiver_name, receiver_phone, receiver_address, package_type, status, remark, create_time, update_time, is_deleted)
VALUES
(3001, 2001, '18888000001', '测试小区2栋2单元', '收件测试01', '17777000001', '目的地大道3号', 0, 0, '寄件测试数据01', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), 0),
(3002, 2002, '18888000002', '测试小区3栋3单元', '收件测试02', '17777000002', '目的地大道6号', 1, 1, '寄件测试数据02', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 0),
(3003, 2003, '18888000003', '测试小区4栋1单元', '收件测试03', '17777000003', '目的地大道9号', 2, 2, '寄件测试数据03', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 0),
(3004, 2004, '18888000004', '测试小区5栋2单元', '收件测试04', '17777000004', '目的地大道12号', 3, 3, '寄件测试数据04', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(3005, 2005, '18888000005', '测试小区1栋3单元', '收件测试05', '17777000005', '目的地大道15号', 0, 0, '寄件测试数据05', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(3006, 2006, '18888000006', '测试小区2栋1单元', '收件测试06', '17777000006', '目的地大道18号', 1, 1, '寄件测试数据06', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 0),
(3007, 2007, '18888000007', '测试小区3栋2单元', '收件测试07', '17777000007', '目的地大道21号', 2, 2, '寄件测试数据07', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(3008, 2008, '18888000008', '测试小区4栋3单元', '收件测试08', '17777000008', '目的地大道24号', 3, 3, '寄件测试数据08', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(3009, 2009, '18888000009', '测试小区5栋1单元', '收件测试09', '17777000009', '目的地大道27号', 0, 0, '寄件测试数据09', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
(3010, 2010, '18888000010', '测试小区1栋2单元', '收件测试10', '17777000010', '目的地大道30号', 1, 1, '寄件测试数据10', DATE_SUB(NOW(), INTERVAL 0 DAY), DATE_SUB(NOW(), INTERVAL 0 DAY), 0);
