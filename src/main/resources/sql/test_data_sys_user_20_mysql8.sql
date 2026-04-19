-- sys_user 测试数据 20 条（密码默认 123456）
USE community_express;
SET NAMES utf8mb4;

-- 清理本批数据（按 username 前缀）
DELETE FROM sys_user WHERE username LIKE '1888800%';

INSERT INTO sys_user
(id, username, password, nickname, email, role, status, create_time, update_time, is_deleted)
VALUES
(2001, '18888000001', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户01', 'u01@test.local', 'ADMIN', 1, NOW(), NOW(), 0),
(2002, '18888000002', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户02', 'u02@test.local', 'ADMIN', 1, NOW(), NOW(), 0),
(2003, '18888000003', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户03', 'u03@test.local', 'STAFF', 1, NOW(), NOW(), 0),
(2004, '18888000004', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户04', 'u04@test.local', 'STAFF', 1, NOW(), NOW(), 0),
(2005, '18888000005', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户05', 'u05@test.local', 'STAFF', 1, NOW(), NOW(), 0),
(2006, '18888000006', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户06', 'u06@test.local', 'STAFF', 1, NOW(), NOW(), 0),
(2007, '18888000007', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户07', 'u07@test.local', 'USER', 1, NOW(), NOW(), 0),
(2008, '18888000008', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户08', 'u08@test.local', 'USER', 1, NOW(), NOW(), 0),
(2009, '18888000009', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户09', 'u09@test.local', 'USER', 1, NOW(), NOW(), 0),
(2010, '18888000010', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户10', 'u10@test.local', 'USER', 1, NOW(), NOW(), 0),
(2011, '18888000011', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户11', 'u11@test.local', 'USER', 1, NOW(), NOW(), 0),
(2012, '18888000012', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户12', 'u12@test.local', 'USER', 1, NOW(), NOW(), 0),
(2013, '18888000013', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户13', 'u13@test.local', 'USER', 1, NOW(), NOW(), 0),
(2014, '18888000014', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户14', 'u14@test.local', 'USER', 1, NOW(), NOW(), 0),
(2015, '18888000015', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户15', 'u15@test.local', 'USER', 1, NOW(), NOW(), 0),
(2016, '18888000016', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户16', 'u16@test.local', 'USER', 1, NOW(), NOW(), 0),
(2017, '18888000017', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户17', 'u17@test.local', 'USER', 1, NOW(), NOW(), 0),
(2018, '18888000018', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户18', 'u18@test.local', 'USER', 1, NOW(), NOW(), 0),
(2019, '18888000019', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户19', 'u19@test.local', 'USER', 1, NOW(), NOW(), 0),
(2020, '18888000020', '$2a$10$0XFhjAbsHa8tnlOWB/s/FuWkvkL9guwNirYel4HGU3/qWF5Uf2C86', '测试用户20', 'u20@test.local', 'USER', 0, NOW(), NOW(), 0);
