# Example to reproduce [KEYCLOAK-2514][bug]

## to reproduce:

1. Create confidential client in Keycloak and export its `keycloak.json`
to repo root.
2. Create `user` role and user that is assigned to it.
3. Build the project (`mvn clean package`).
4. Start server with `java -jar target/keycloak-2514-0.1.0-SNAPSHOT-uber.jar`.
5. Visit `http://localhost:8080/test` and get NPE instead of normal result.

`TokenStore` from `AdapterConfig` is overridden in [`Main.java`][config].

All works fine with `session` `TokenStore`.

[bug]: https://issues.jboss.org/browse/KEYCLOAK-2514
[config]: src/main/java/ws/gross/kc2514/Main.java#L49
