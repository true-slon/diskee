CREATE TABLE IF NOT EXISTS dat_users (
    id                  BIGSERIAL       PRIMARY KEY,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password_hash       VARCHAR(255)    NOT NULL,
    display_name        VARCHAR(255),
    storage_used_bytes  BIGINT          NOT NULL DEFAULT 0,
    storage_limit_bytes BIGINT          NOT NULL DEFAULT 10737418240, -- 10 ГБ по умолчанию
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);


CREATE UNIQUE INDEX IF NOT EXISTS idx_dat_users_email ON dat_users (email) WHERE deleted_at IS NULL;

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    refresh_token_hash  TEXT            NOT NULL,
    ip_address          VARCHAR(45),                -- поддержка IPv6
    user_agent          TEXT,
    expires_at          TIMESTAMPTZ     NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires_at ON user_sessions (expires_at);

------------------------------------------------------------
-- 3. Подтверждение email
------------------------------------------------------------
CREATE TABLE IF NOT EXISTS email_confirmations (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    token       VARCHAR(255)    NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ     NOT NULL,
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_email_confirmations_token ON email_confirmations (token);
CREATE INDEX IF NOT EXISTS idx_email_confirmations_user_id ON email_confirmations (user_id);

------------------------------------------------------------
-- 4. Сброс пароля
------------------------------------------------------------
CREATE TABLE IF NOT EXISTS password_resets (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    token       VARCHAR(255)    NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ     NOT NULL,
    is_used     BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_password_resets_token ON password_resets (token);
CREATE INDEX IF NOT EXISTS idx_password_resets_user_id ON password_resets (user_id);


CREATE TABLE IF NOT EXISTS folders (
    id               BIGSERIAL       PRIMARY KEY,
    user_id          BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    parent_folder_id BIGINT          REFERENCES folders(id) ON DELETE SET NULL,
    folder_name      VARCHAR(255)    NOT NULL,
    full_path        TEXT            NOT NULL,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    deleted_at       TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_folders_user_id ON folders (user_id);
CREATE INDEX IF NOT EXISTS idx_folders_parent_folder_id ON folders (parent_folder_id);
CREATE INDEX IF NOT EXISTS idx_folders_full_path ON folders (full_path);


CREATE TABLE IF NOT EXISTS files (
    id                 BIGSERIAL       PRIMARY KEY,
    user_id            BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    parent_folder_id   BIGINT          REFERENCES folders(id) ON DELETE SET NULL,
    file_name          VARCHAR(255)    NOT NULL,
    file_extension     VARCHAR(50),
    mime_type          VARCHAR(255),
    file_size_bytes    BIGINT          NOT NULL DEFAULT 0,
    storage_object_key TEXT            NOT NULL,
    preview_object_key TEXT,
    is_deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_files_user_id ON files (user_id);
CREATE INDEX IF NOT EXISTS idx_files_parent_folder_id ON files (parent_folder_id);
CREATE INDEX IF NOT EXISTS idx_files_storage_object_key ON files (storage_object_key);
CREATE INDEX IF NOT EXISTS idx_files_is_deleted ON files (is_deleted) WHERE is_deleted = FALSE;


CREATE TABLE IF NOT EXISTS link_files_folders (
    id            BIGSERIAL       PRIMARY KEY,
    file_id       BIGINT          NOT NULL REFERENCES files(id) ON DELETE CASCADE,
    folder_id     BIGINT          NOT NULL REFERENCES folders(id) ON DELETE CASCADE,
    owner_user_id BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    display_name  VARCHAR(255),
    is_original   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_link_files_folders_file_id ON link_files_folders (file_id);
CREATE INDEX IF NOT EXISTS idx_link_files_folders_folder_id ON link_files_folders (folder_id);
CREATE INDEX IF NOT EXISTS idx_link_files_folders_owner_user_id ON link_files_folders (owner_user_id);


CREATE TABLE IF NOT EXISTS shared_links (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    file_id         BIGINT          REFERENCES files(id) ON DELETE CASCADE,
    folder_id       BIGINT          REFERENCES folders(id) ON DELETE CASCADE,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    permission      VARCHAR(50)     NOT NULL DEFAULT 'view',  -- view, download, edit
    password_hash   VARCHAR(255),
    expires_at      TIMESTAMPTZ,
    download_count  INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT shared_links_file_or_folder_check CHECK (
        (file_id IS NOT NULL AND folder_id IS NULL) OR
        (file_id IS NULL AND folder_id IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_shared_links_token ON shared_links (token);
CREATE INDEX IF NOT EXISTS idx_shared_links_file_id ON shared_links (file_id);
CREATE INDEX IF NOT EXISTS idx_shared_links_folder_id ON shared_links (folder_id);


CREATE TABLE IF NOT EXISTS trash_bin (
    id            BIGSERIAL       PRIMARY KEY,
    user_id       BIGINT          NOT NULL REFERENCES dat_users(id) ON DELETE CASCADE,
    file_id       BIGINT          REFERENCES files(id) ON DELETE CASCADE,
    folder_id     BIGINT          REFERENCES folders(id) ON DELETE CASCADE,
    original_path TEXT            NOT NULL,
    deleted_at    TIMESTAMPTZ     NOT NULL DEFAULT now(),
    auto_delete_at TIMESTAMPTZ    NOT NULL,
    CONSTRAINT trash_bin_file_or_folder_check CHECK (
        (file_id IS NOT NULL AND folder_id IS NULL) OR
        (file_id IS NULL AND folder_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_trash_bin_user_id ON trash_bin (user_id);
CREATE INDEX IF NOT EXISTS idx_trash_bin_file_id ON trash_bin (file_id);
CREATE INDEX IF NOT EXISTS idx_trash_bin_folder_id ON trash_bin (folder_id);
CREATE INDEX IF NOT EXISTS idx_trash_bin_auto_delete_at ON trash_bin (auto_delete_at);