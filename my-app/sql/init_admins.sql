-- 管理员表
CREATE TABLE IF NOT EXISTS `admins` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '管理员名称',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号（登录账号）',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` VARCHAR(20) NOT NULL DEFAULT 'admin' COMMENT '角色: admin/superadmin',
  `last_login` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认超级管理员账号 admin / admin123
INSERT INTO `admins` (`name`, `phone`, `password`, `role`, `status`)
VALUES ('系统管理员', 'admin', 'admin123', 'superadmin', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
