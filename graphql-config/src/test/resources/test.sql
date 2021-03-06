DROP SCHEMA "PUBLIC" CASCADE;

CREATE TABLE "USER" (
  "ID"   INT         NOT NULL,
  "NAME" VARCHAR(32) NOT NULL
);

CREATE TABLE "MODERATOR" (
  "ID"               INT NOT NULL,
  "MODERATED_THREAD" INT NOT NULL
);

COMMENT ON TABLE "USER" IS 'User';
COMMENT ON COLUMN "USER"."ID" IS 'User id';
COMMENT ON COLUMN "USER"."NAME" IS 'User name';

CREATE TABLE "MESSAGE" (
  "ID"         INT          NOT NULL,
  "BODY"       VARCHAR(512) NOT NULL,
  "CREATED_AT" TIMESTAMP    NOT NULL,
  "AUTHOR"     INT          NOT NULL,
  "THREAD"     INT          NOT NULL
);

COMMENT ON TABLE "MESSAGE" IS 'Message';
COMMENT ON COLUMN "MESSAGE"."ID" IS 'Message id';
COMMENT ON COLUMN "MESSAGE"."BODY" IS 'Message contents';
COMMENT ON COLUMN "MESSAGE"."CREATED_AT" IS 'Message creation date';
COMMENT ON COLUMN "MESSAGE"."AUTHOR" IS 'Message author';
COMMENT ON COLUMN "MESSAGE"."THREAD" IS 'Message thread';

CREATE TABLE "THREAD" (
  "ID"               INT          NOT NULL,
  "TITLE"            VARCHAR(128) NOT NULL,
  "ORIGINAL_MESSAGE" INT          NOT NULL
);

COMMENT ON TABLE "THREAD" IS 'Thread';
COMMENT ON COLUMN "THREAD"."ID" IS 'Thread id';
COMMENT ON COLUMN "THREAD"."TITLE" IS 'Thread title';
COMMENT ON COLUMN "THREAD"."ORIGINAL_MESSAGE" IS 'Original thread message';

ALTER TABLE "USER"
  ADD CONSTRAINT "USER_PK" PRIMARY KEY ("ID");
ALTER TABLE "MESSAGE"
  ADD CONSTRAINT "MESSAGE_PK" PRIMARY KEY ("ID");
ALTER TABLE "THREAD"
  ADD CONSTRAINT "THREAD_PK" PRIMARY KEY ("ID");

ALTER TABLE "THREAD"
  ADD CONSTRAINT "THREAD_MESSAGE_FK" FOREIGN KEY ("ORIGINAL_MESSAGE") REFERENCES "MESSAGE" ("ID");
ALTER TABLE "MESSAGE"
  ADD CONSTRAINT "MESSAGE_THREAD_FK" FOREIGN KEY ("THREAD") REFERENCES "THREAD" ("ID");
ALTER TABLE "MESSAGE"
  ADD CONSTRAINT "MESSAGE_AUTHOR" FOREIGN KEY ("AUTHOR") REFERENCES "USER" ("ID");

ALTER TABLE "MODERATOR"
  ADD CONSTRAINT "MODERATOR_PK" PRIMARY KEY ("ID");

ALTER TABLE "MODERATOR"
  ADD CONSTRAINT "MODERATOR_THREAD_FK" FOREIGN KEY ("MODERATED_THREAD") REFERENCES "THREAD" ("ID");

ALTER TABLE "MODERATOR"
  ADD CONSTRAINT "MODERATOR_USER_FK" FOREIGN KEY ("ID") REFERENCES "USER" ("ID");
