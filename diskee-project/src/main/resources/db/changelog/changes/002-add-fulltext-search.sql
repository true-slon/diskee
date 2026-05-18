CREATE TABLE IF NOT EXISTS file_search_vectors (
    file_id     BIGINT PRIMARY KEY REFERENCES diskee.files(id) ON DELETE CASCADE,
    search_vector tsvector NOT NULL
);

INSERT INTO file_search_vectors (file_id, search_vector)
SELECT id, to_tsvector('russian', coalesce(file_name, ''))
FROM diskee.files
WHERE is_deleted = false
ON CONFLICT (file_id) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_file_search_vectors ON file_search_vectors USING GIN (search_vector);

CREATE OR REPLACE FUNCTION diskee.files_search_insert() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO file_search_vectors (file_id, search_vector)
    VALUES (NEW.id, to_tsvector('russian', coalesce(NEW.file_name, '')))
    ON CONFLICT (file_id) DO UPDATE SET search_vector = to_tsvector('russian', coalesce(NEW.file_name, ''));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_files_search_insert ON diskee.files;
CREATE TRIGGER trg_files_search_insert AFTER INSERT ON diskee.files
    FOR EACH ROW EXECUTE FUNCTION diskee.files_search_insert();

CREATE OR REPLACE FUNCTION diskee.files_search_update() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.file_name <> OLD.file_name THEN
        UPDATE file_search_vectors SET search_vector = to_tsvector('russian', coalesce(NEW.file_name, ''))
        WHERE file_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_files_search_update ON diskee.files;
CREATE TRIGGER trg_files_search_update AFTER UPDATE ON diskee.files
    FOR EACH ROW EXECUTE FUNCTION diskee.files_search_update();

CREATE OR REPLACE FUNCTION diskee.files_search_delete() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM file_search_vectors WHERE file_id = OLD.id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_files_search_delete ON diskee.files;
CREATE TRIGGER trg_files_search_delete AFTER DELETE ON diskee.files
    FOR EACH ROW EXECUTE FUNCTION diskee.files_search_delete();