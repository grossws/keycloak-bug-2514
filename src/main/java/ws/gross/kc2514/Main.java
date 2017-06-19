package ws.gross.kc2514;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.jetty.KeycloakJettyAuthenticator;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.eclipse.jetty.servlet.ServletContextHandler.SECURITY;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    ServletContextHandler sch = new ServletContextHandler(SESSIONS | SECURITY);
    sch.setContextPath("/");
    sch.addServlet(new ServletHolder(new TestServlet()), "/test");

    // default is ConstraintSecurityHandler.class as of Jetty 9.3.x
    ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler) sch.getSecurityHandler();
    configureSecurity(securityHandler);

    Server server = new Server(8080);
    server.setHandler(sch);

    log.info("Starting server on http://127.0.0.1:8080/");
    server.start();
    log.info("Started server on http://127.0.0.1:8080/");
    server.join();
  }

  private static void configureSecurity(ConstraintSecurityHandler handler) throws IOException {
    InputStream is = Files.newInputStream(Paths.get("keycloak.json"));
    AdapterConfig config = KeycloakDeploymentBuilder.loadAdapterConfig(is);
    // override TokenStore type
    config.setTokenStore("cookie");

    KeycloakJettyAuthenticator authenticator = new KeycloakJettyAuthenticator();
    authenticator.setAdapterConfig(config);

    handler.setRealmName(config.getRealm());
    handler.setAuthenticator(authenticator);

    handler.addConstraintMapping(createConstraintMapping("user", false, "/*"));
  }

  private static ConstraintMapping createConstraintMapping(String role, boolean requireTls, String path) {
    Constraint constraint = createConstraint(requireTls ? Constraint.DC_CONFIDENTIAL : Constraint.DC_NONE, role);

    ConstraintMapping mapping = new ConstraintMapping();
    mapping.setConstraint(constraint);
    mapping.setPathSpec(Objects.requireNonNull(path, "path in paths"));

    return mapping;
  }

  /**
   * Create simple {@link Constraint} for given {@code roles} and {@code dataConstraint}
   * <p>
   * This constraint requires:
   * <ul>
   * <li>to be authenticated,</li>
   * <li>to have {@code role} in roles for this app,</li>
   * <li>to communicate over secured channel if {@code dataConstraint} is {@link Constraint#DC_CONFIDENTIAL}</li>
   * </ul>
   *
   * @param dataConstraint requirement for data transmission channel
   * @param roles roles which are sufficient to access to the secured resource
   * @return created constraint
   * @see Constraint#setDataConstraint(int)
   */
  private static Constraint createConstraint(int dataConstraint, String... roles) {
    Objects.requireNonNull(roles, "role");
    if (roles.length < 1) {
      throw new IllegalArgumentException("At least one role required");
    }
    for (String role : roles) {
      Objects.requireNonNull(role, "role in roles");
    }
    Constraint constraint = new Constraint();
    constraint.setAuthenticate(true);
    constraint.setDataConstraint(dataConstraint);
    constraint.setRoles(roles);
    return constraint;
  }
}
