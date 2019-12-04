package com.itzap.jerseyswagger;

import com.itzap.common.Property;

public enum OpenApiConfiguration implements Property {
    TITLE("title", "API Documentation"),
    VERSION("verison", "v1"),
    BASE_PATH("basePath"),
    CONTACT("contact", "IT Zap"),
    SCAN_PACKAGE("package", "com.itzap"),
    ENABLE_AUTH("enableAuth", "false"),
    SWAGGER_URL("swaggerUrl"),
    API_BASE_URL("apiBaseUrl"),
    SWAGGER_SERVLET_NAME("swaggerServletName", "swagger-servlet")
    ;

    private final String name;
    private final String defaultValue;

    OpenApiConfiguration(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    OpenApiConfiguration(String name) {
        this(name, null);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDefault() {
        return this.defaultValue;
    }
}
