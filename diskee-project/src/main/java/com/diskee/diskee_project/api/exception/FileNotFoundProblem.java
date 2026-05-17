package com.diskee.diskee_project.api.exception;

import com.diskee.diskee_project.utils.SwaggerConstants;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public final class FileNotFoundProblem extends BaseProblemException {

    public static final String SCHEMA_NAME = "FileNotFoundProblem";
    public static final String SCHEMA_REF = SwaggerConstants.SCHEMA_PREFIX + SCHEMA_NAME;

    private static final String TYPE = "/problem/file/not-found";
    private static final String TITLE = "Файл не найден";
    private static final String DETAIL = "Файл не найден по id: ";

    private final Long id;

    public FileNotFoundProblem(Long id) {
        super(HttpStatus.NOT_FOUND, TYPE, TITLE, DETAIL + id);
        this.id = id;
        getBody().setProperty("id", id);
    }

    public static ObjectSchema getSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema
                .addProperty("type", new StringSchema().example(TYPE))
                .addProperty("title", new StringSchema().example(TITLE))
                .addProperty("detail", new StringSchema().example(DETAIL + 1))
                .addProperty("id", new IntegerSchema().example(1));
        return schema;
    }
}