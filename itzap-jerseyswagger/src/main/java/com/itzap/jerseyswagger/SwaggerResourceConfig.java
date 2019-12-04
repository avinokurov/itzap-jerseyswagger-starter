package com.itzap.jerseyswagger;

import com.itzap.common.AnyConfig;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;

public class SwaggerResourceConfig extends ResourceConfig {
    SwaggerResourceConfig(AnyConfig properties) {
        init(properties);
    }

    private void init(AnyConfig properties) {
        register(OpenApiResource.class);
        register(SwaggerSerializers.class);
        register(WadlResource.class);
    }
}
