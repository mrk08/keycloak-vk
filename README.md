# keycloak-vk
Keycloak vk.com OAuth provider (works well, ignore ugly README)


I've installed it as a wildfly module:
1. mvn clean install
2. $JBOSS_HOME/bin/jboss-cli.sh --command="module add --name=ru.codecamp.keycloak.social-vk --resources=/home/zk/keycloak-social-vk.jar --dependencies=org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,com.fasterxml.jackson.core.jackson-databind"
3. Added as a provider https://www.keycloak.org/docs/latest/server_development/index.html#register-a-provider-using-modules (<provider>module:ru.codecamp.keycloak.social-vk</provider>)
4. cd $JBOSS_HOME/themes/base/admin/resources/partials && cp realm-identity-provider-facebook.html realm-identity-provider-vk.html && cp realm-identity-provider-facebook-ext.html realm-identity-provider-vk-ext.html


WARNING!
Tested against keycloak 2.5.0 but it is not issue to upgrade for 4.0.0
