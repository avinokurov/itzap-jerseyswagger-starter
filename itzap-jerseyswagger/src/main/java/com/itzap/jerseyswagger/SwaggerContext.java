package com.itzap.jerseyswagger;

import com.itzap.common.AnyConfig;
import com.itzap.common.exception.IZapException;
import com.itzap.common.utils.JarUtils;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.tomcat.util.buf.UDecoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.itzap.common.utils.JarUtils.extractDir;
import static com.itzap.common.utils.JarUtils.getAppPath;
import static com.itzap.common.utils.JarUtils.isRunningJar;

public final class SwaggerContext {
    private static final Pattern INDEX_PAGE_PATTER = Pattern.compile("(.*)url:\\s+\"(.*)\",(.*)");
    private static final String INDEX_PAGE_NAME = "index.html";

    private SwaggerContext() {
    }

    public static void addSwaggerServlet(Tomcat tomcat,
                                         Context context,
                                         AnyConfig config,
                                         Class appClass) {
        String docURLBase = config.getString(OpenApiConfiguration.SWAGGER_URL);
        String version = config.getString(OpenApiConfiguration.VERSION);
        String servletName = config.getString(OpenApiConfiguration.SWAGGER_SERVLET_NAME);

        boolean endsWith = StringUtils.endsWith(docURLBase, "/");

        if (StringUtils.isBlank(docURLBase)) {
            docURLBase = "/api/" + version;
        } else {
            docURLBase = endsWith ? docURLBase + version :
                    docURLBase + "/" + version;
        }

        // Setup Swagger-UI static resources
        File swaggerContentPath = new File(".");
        Class<?> srcPackage = SwaggerContext.class;
        String openApiUrl = buildURL(tomcat, docURLBase + "/openapi.json");

        try {
            if (isRunningJar(appClass)) {
                srcPackage = appClass;
                File tempDir = getAppPath(srcPackage);
                swaggerContentPath = new File(tempDir, "swagger");


                FileUtils.forceMkdir(swaggerContentPath);
                FileUtils.forceDeleteOnExit(swaggerContentPath);
                ByteArrayOutputStream indexPage = new ByteArrayOutputStream();
                MutableObject<File> indexFile = new MutableObject<>();

                extractDir(srcPackage, swaggerContentPath, "swagger", new JarUtils.DefaultExtractor(srcPackage,
                        swaggerContentPath) {
                    @Override
                    protected OutputStream getOutputStream(File file) throws IOException {
                        if (INDEX_PAGE_NAME.equalsIgnoreCase(file.getName())) {
                            indexFile.setValue(file);
                            return indexPage;
                        } else {
                            return super.getOutputStream(file);
                        }
                    }
                });
                // replace url in index page to base url
                indexPage.flush();
                replaceUrl(IOUtils.readLines(new ByteArrayInputStream(indexPage.toByteArray()),
                        Charset.defaultCharset()), indexFile.getValue(), openApiUrl);

            } else {
                swaggerContentPath = new File(getAppPath(srcPackage), "swagger");
                File indexFile = new File(swaggerContentPath, INDEX_PAGE_NAME);
                replaceUrl(FileUtils.readLines(indexFile, Charset.defaultCharset()),
                        indexFile, openApiUrl);
            }

        } catch (IOException e) {
            throw new IZapException(String.format("Failed to extract swagger dir to path: %s",
                    swaggerContentPath.getAbsolutePath()), e);
        }
        tomcat.addWebapp(docURLBase + "/swagger",
                swaggerContentPath.getAbsolutePath());

        try {
            SwaggerContainer container = new SwaggerContainer(new SwaggerResourceConfig(config),
                    config);
            Tomcat.addServlet(context, servletName, container);
        } catch (Exception e) {
            throw new IZapException("Failed to init swagger container", e);
        }

        String swaggerURL = UDecoder.URLDecode(docURLBase + "/*", Charset.defaultCharset());
        context.addServletMappingDecoded(swaggerURL, servletName);
    }

    private static void replaceUrl(List<String> pageLines, File outFile, String appBase) {
        List<String> newLines = pageLines.stream().map(line -> {
            Matcher matcher = INDEX_PAGE_PATTER.matcher(line);
            if (matcher.find()) {
                return matcher.group(1) + "url: \"" + appBase + "\", " + matcher.group(3);
            } else {
                return line;
            }
        }).collect(Collectors.toList());

        try {
            FileUtils.writeLines(outFile, newLines);
        } catch (IOException e) {
            throw new IZapException(String.format("Failed to write updated index page to file %s",
                    outFile.getAbsolutePath()), e);
        }
    }

    private static String buildURL(Tomcat tomcat, String path) {
        try {
            URL url = new URL(tomcat.getConnector().getScheme(),
                    tomcat.getServer().getAddress(), tomcat.getConnector().getPort(), path);
            return url.toString();
        } catch (MalformedURLException e) {
            throw new IZapException("Failed to build URL for tomcat", e);
        }
    }
}
