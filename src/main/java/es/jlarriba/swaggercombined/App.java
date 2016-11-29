package es.jlarriba.swaggercombined;

import io.swagger.models.Info;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;

import java.io.FileInputStream;
import java.util.*;

/**
 *
 * @author jlarriba
 */
public class App {

    public static void main(String args[]) throws Exception {

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "0.0.0.0")
                .setHandler(Handlers.path()
                        .addPrefixPath("/healthz", new HttpHandler() {
                            @Override
                            public void handleRequest(HttpServerExchange hse) throws Exception {
                                hse.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                hse.getResponseSender().send("OK");
                            }
                        })
                        .addPrefixPath("/", new SwaggerHandler()))
                .build();
        server.start();
    }
}

class SwaggerHandler implements HttpHandler {

    private String swaggerCombinedTitle;
    private String swaggerCombinedVersion;
    private String[] swaggerCombinedUrls;

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        if (System.getenv("SWAGGER_COMBINED_URLS") != null) {
            swaggerCombinedTitle = System.getenv("SWAGGER_COMBINED_TITLE");
            swaggerCombinedVersion = System.getenv("SWAGGER_COMBINED_VERSION");
            swaggerCombinedUrls = System.getenv("SWAGGER_COMBINED_URLS").split(",");
        } else {
            Properties props = new Properties();
            FileInputStream file = new FileInputStream("./swagger-combined.properties");
            props.load(file);
            file.close();
            swaggerCombinedTitle = props.getProperty("swagger.combined.title");
            swaggerCombinedVersion = props.getProperty("swagger.combined.version");
            swaggerCombinedUrls = props.getProperty("swagger.combined.urls").split(",");
        }

        Swagger swagger = new Swagger();
        swagger.setSwagger("2.0");
        swagger.setBasePath("/");
        Info info = new Info();
        info.setTitle(swaggerCombinedTitle);
        info.setVersion(swaggerCombinedVersion);
        swagger.setInfo(info);
        Map<String, Path> paths = new HashMap<>();
        if (swaggerCombinedUrls != null) {
            for (int i = 0; i < swaggerCombinedUrls.length; i++) {
                Swagger s = new SwaggerParser().read(swaggerCombinedUrls[i]);
                if (s != null) {
                    Map<String, Path> ps = s.getPaths();
                    Set<String> keys = s.getPaths().keySet();
                    for (String key:keys) {
                        Path p = ps.get(key);
                        ps.remove(key);
                        ps.put(s.getBasePath() + key, p);

                    }
                    paths.putAll(ps);
                }
            }
        }
        swagger.setPaths(paths);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(Json.pretty(swagger));
    }
}
