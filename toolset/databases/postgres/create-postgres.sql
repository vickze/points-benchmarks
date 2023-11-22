BEGIN;

CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- 账号
CREATE TABLE IF NOT EXISTS "account" (
  "id" BIGSERIAL NOT NULL,
  "name" VARCHAR(32) NOT NULL,
  "balance" BIGINT NOT NULL,
  "create_time" TIMESTAMP NOT NULL,
  "update_time" TIMESTAMP NOT NULL,
  PRIMARY KEY ("id")
);

COMMENT ON TABLE "account" IS '账号';
COMMENT ON COLUMN "account"."id" IS 'ID';
COMMENT ON COLUMN "account"."name" IS '名称';
COMMENT ON COLUMN "account"."balance" IS '余额';
COMMENT ON COLUMN "account"."create_time" IS '创建时间';
COMMENT ON COLUMN "account"."update_time" IS '更新时间';

INSERT INTO "account" (name, balance, create_time, update_time)
SELECT 'default' AS d, 0 AS b, now() as c, now() as u FROM generate_series(1,10000) as x(id);


CREATE TABLE IF NOT EXISTS "points_transaction" (
  "id" BIGSERIAL NOT NULL,
  "account_id" BIGINT NOT NULL,
  "type" INTEGER NOT NULL,
  "amount" BIGINT NOT NULL,
  "previous_balance" BIGINT NOT NULL,
  "after_balance" BIGINT NOT NULL,
  "create_time" TIMESTAMP NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX "idx_account_id" ON "points_transaction" ("account_id");

COMMENT ON TABLE "points_transaction" IS '积分明细';
COMMENT ON COLUMN "points_transaction"."id" IS 'ID';
COMMENT ON COLUMN "points_transaction"."account_id" IS '账户ID';
COMMENT ON COLUMN "points_transaction"."type" IS '1-ADD 2-DEDUCT';
COMMENT ON COLUMN "points_transaction"."amount" IS '数量';
COMMENT ON COLUMN "points_transaction"."previous_balance" IS '变动前余额';
COMMENT ON COLUMN "points_transaction"."after_balance" IS '变动后余额';
COMMENT ON COLUMN "points_transaction"."create_time" IS '创建时间';


COMMIT;