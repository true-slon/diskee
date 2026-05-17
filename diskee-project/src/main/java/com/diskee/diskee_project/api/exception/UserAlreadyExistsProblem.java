package com.diskee.diskee_project.api.exception;

import com.diskee.diskee_project.utils.SwaggerConstants;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public final class UserAlreadyExistsProblem extends BaseProblemException {

    public static final String SCHEMA_NAME = "UserAlreadyExistsProblem";
    public static final String SCHEMA_REF = SwaggerConstants.SCHEMA_PREFIX + SCHEMA_NAME;

    private static final String TYPE = "/problem/user/already-exists";
    private static final String TITLE = "Пользователь уже существует";
    private static final String DETAIL = "Пользователь с таким email уже зарегистрирован: ";

    private final String email;

    public UserAlreadyExistsProblem(String email) {
        super(HttpStatus.CONFLICT, TYPE, TITLE, DETAIL + email);
        this.email = email;
        // Добавляем кастомное поле в тело ответа
        getBody().setProperty("email", email);
    }

    public static ObjectSchema getSchema() {
        ObjectSchema schema = new ObjectSchema();
        schema
                .addProperty("type", new StringSchema().example(TYPE))
                .addProperty("title", new StringSchema().example(TITLE))
                .addProperty("detail", new StringSchema().example(DETAIL + "user@example.com"))
                .addProperty("email", new StringSchema().example("user@example.com"));
        return schema;
    }
}