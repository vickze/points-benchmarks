USE points;

-- 账号
CREATE TABLE IF NOT EXISTS `account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(32) NOT NULL COMMENT '名称',
  `balance` BIGINT NOT NULL COMMENT '余额',
  `create_time` TIMESTAMP NOT NULL COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号';

DELIMITER #
CREATE PROCEDURE load_data()
BEGIN

declare v_max int unsigned default 10000;
declare v_counter int unsigned default 0;

  TRUNCATE TABLE account;
  START TRANSACTION;
  while v_counter < v_max do
    INSERT INTO `account`(`name`, `balance`, `create_time`, `update_time`) VALUES ('default', 0, now(), now());
    SET v_counter=v_counter+1;
  end while;
  commit;
END
#

DELIMITER ;

CALL load_data();
DROP PROCEDURE IF EXISTS load_data;

-- 积分明细
CREATE TABLE IF NOT EXISTS `points_transaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `type` TINYINT NOT NULL COMMENT '1-ADD 2-DEDUCT',
  `amount` BIGINT NOT NULL COMMENT '数量',
  `previous_balance` BIGINT NOT NULL COMMENT '变动前余额',
  `after_balance` BIGINT NOT NULL COMMENT '变动后余额',
  `create_time` TIMESTAMP NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_account_id`(`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分明细';