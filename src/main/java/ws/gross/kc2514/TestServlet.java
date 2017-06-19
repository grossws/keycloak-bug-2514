package ws.gross.kc2514;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.util.JsonSerialization;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TestServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String tokenJson = JsonSerialization.writeValueAsPrettyString(getKSC(req).getToken());

    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("text/plain");
    resp.getWriter().write("auth ok with token: ");
    resp.getWriter().write(tokenJson);
    resp.getWriter().flush();
  }

  private KeycloakSecurityContext getKSC(HttpServletRequest req) {
    return (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
  }
}
