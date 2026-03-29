-- 灾害文件管理表
-- 用于存储暴雨和地震灾害相关的文件信息

DROP TABLE IF EXISTS disaster_files;

CREATE TABLE "public"."disaster_files" (
  "file_id" int8 NOT NULL DEFAULT nextval('disaster_files_seq'::regclass),
  "disaster_id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "disaster_type" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "file_name" varchar(255) COLLATE "pg_catalog"."default",
  "file_path" varchar(500) COLLATE "pg_catalog"."default",
  "local_file_path" varchar(500) COLLATE "pg_catalog"."default",
  "file_size" int8,
  "file_type" varchar(50) COLLATE "pg_catalog"."default",
  "file_description" varchar(500) COLLATE "pg_catalog"."default",
  "upload_by" varchar(100) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  "update_time" timestamp(6),
  "is_deleted" int2 DEFAULT 0,
  CONSTRAINT "disaster_files_pkey" PRIMARY KEY ("file_id")
);

COMMENT ON COLUMN "public"."disaster_files"."file_id" IS '文件 ID（主键）';

COMMENT ON COLUMN "public"."disaster_files"."disaster_id" IS '灾害 ID（暴雨 rain_id 或地震 disaster_id）';

COMMENT ON COLUMN "public"."disaster_files"."disaster_type" IS '灾害类型（rain:暴雨，earthquake:地震）';

COMMENT ON COLUMN "public"."disaster_files"."file_name" IS '文件名称';

COMMENT ON COLUMN "public"."disaster_files"."file_path" IS '文件路径（相对路径）';

COMMENT ON COLUMN "public"."disaster_files"."local_file_path" IS '本地文件路径（绝对路径）';

COMMENT ON COLUMN "public"."disaster_files"."file_size" IS '文件大小（字节）';

COMMENT ON COLUMN "public"."disaster_files"."file_type" IS '文件类型（后缀名）';

COMMENT ON COLUMN "public"."disaster_files"."file_description" IS '文件描述';

COMMENT ON COLUMN "public"."disaster_files"."upload_by" IS '上传人';

COMMENT ON COLUMN "public"."disaster_files"."create_time" IS '上传时间';

COMMENT ON COLUMN "public"."disaster_files"."update_time" IS '修改时间';

COMMENT ON COLUMN "public"."disaster_files"."is_deleted" IS '逻辑删除标志（0:未删除，1:已删除）';

COMMENT ON TABLE "public"."disaster_files" IS '灾害文件管理表';

-- 创建索引以提高查询性能
CREATE INDEX "idx_disaster_id" ON "public"."disaster_files" USING btree (
  "disaster_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

CREATE INDEX "idx_disaster_type" ON "public"."disaster_files" USING btree (
  "disaster_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

CREATE INDEX "idx_disaster_composite" ON "public"."disaster_files" USING btree (
  "disaster_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "disaster_type" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

ALTER TABLE "public"."disaster_files" 
  OWNER TO "postgres";
