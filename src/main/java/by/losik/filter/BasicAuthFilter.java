package by.losik.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Base64;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Slf4j
public class BasicAuthFilter implements ContainerRequestFilter {
    @ConfigProperty(name = "app.auth.username")
    String VALID_USERNAME;
    @ConfigProperty(name = "app.auth.password")
    String VALID_PASSWORD;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("q/health") || path.startsWith("metrics")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            abortWithUnauthorized(requestContext);
            return;
        }

        try {
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);

            if (values.length == 2 &&
                    VALID_USERNAME.equals(values[0]) &&
                    VALID_PASSWORD.equals(values[1])) {
                log.info("Success! for {}", VALID_USERNAME);
                return;
            }
        } catch (Exception e) {
            log.error("Fail! for {}", VALID_USERNAME);
        }

        abortWithUnauthorized(requestContext);
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Basic realm=\"realm\"")
                .entity("Authentication required")
                .build());
    }
}