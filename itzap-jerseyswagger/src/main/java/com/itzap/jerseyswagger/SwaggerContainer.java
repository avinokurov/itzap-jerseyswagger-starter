package com.itzap.jerseyswagger;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.itzap.common.AnyConfig;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.HashSet;
import java.util.List;

public class SwaggerContainer extends ServletContainer {
    private final AnyConfig properties;

    SwaggerContainer(ResourceConfig resourceConfig,
                     AnyConfig properties) {
        super(resourceConfig);
        this.properties = properties;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title(properties.getString(OpenApiConfiguration.TITLE))
                .version(properties.getString(OpenApiConfiguration.VERSION))
                .contact(new Contact()
                        .name(properties.getString(OpenApiConfiguration.CONTACT)));

        oas.info(info);
        List<String> packages = ImmutableList.<String>builder()
                .addAll(Splitter.on(",")
                        .splitToList(properties.getString(OpenApiConfiguration.SCAN_PACKAGE)))
                .add("io.swagger.v3.jaxrs2.integration.resources")
                .build();
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .id(properties.getString(OpenApiConfiguration.SWAGGER_SERVLET_NAME))
                .openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(new HashSet<>(packages));

        oas.setServers(Lists.newArrayList(new Server()
                .url(properties.getString(OpenApiConfiguration.API_BASE_URL))));
        try {
            new JaxrsOpenApiContextBuilder()
                    .servletConfig(config)
                    .openApiConfiguration(oasConfig)
                    .buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }
}
