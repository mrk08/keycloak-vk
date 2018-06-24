package ru.codecamp.keycloak.social.vk;

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.oidc.util.JsonSimpleHttp;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.truststore.JSSETruststoreConfigurator;

/**
 * Created by zk on 22/06/2018.
 */
public class VKIdentityProvider extends AbstractOAuth2IdentityProvider implements SocialIdentityProvider {
    private static final String OAUTH2_PARAMETER_EMAIL = "email";
    private static final String AUTH_URL = "https://oauth.vk.com/authorize";
    private static final String TOKEN_URL = "https://oauth.vk.com/access_token";
    private static final String PROFILE_URL = "https://api.vk.com/method/users.get";
    private static final String DEFAULT_SCOPE = "email,offline";
    private static final String VK_API_VERSION = "5.80";


    VKIdentityProvider(KeycloakSession session, OAuth2IdentityProviderConfig config) {
        //noinspection unchecked
        super(session, config);
        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setUserInfoUrl(PROFILE_URL);
    }

    public BrokeredIdentityContext getFederatedIdentity(String response) {
        String accessToken = extractTokenFromResponse(response, OAUTH2_PARAMETER_ACCESS_TOKEN);
        String email = extractTokenFromResponse(response, OAUTH2_PARAMETER_EMAIL);
        if (accessToken == null) {
            throw new IdentityBrokerException("No access token available in OAuth server response: " + response);
        }
        return doGetFederatedIdentity(accessToken, email);
    }

    private BrokeredIdentityContext doGetFederatedIdentity(String accessToken, String email) {
        try {
            JsonNode resp = JsonSimpleHttp.asJson(SimpleHttp.doGet(PROFILE_URL)
                    .param("access_token", accessToken)
                    .param("v", VK_API_VERSION)
                    .param("fields", "domain"));
            if (resp.has("error")) {
                throw new IdentityBrokerException("Could not obtain user profile from vk.");
            }
            // https://vk.com/dev/users.get
            JsonNode profile = resp.findValue("response").get(0);
            // extract data from response
            String id = getJsonProperty(profile, "id");
            String username = getJsonProperty(profile, "domain");
            String firstName = getJsonProperty(profile, "first_name");
            String lastName = getJsonProperty(profile, "last_name");
            // in some cases user domain could be null
            if (username == null) {
                if (email != null) {
                    username = email;
                } else {
                    username = id;
                }
            }
            // in some cases last name could be null (really?)
            if (lastName == null) {
                lastName = "";
            } else {
                lastName = " " + lastName;
            }
            // setup user context
            BrokeredIdentityContext user = new BrokeredIdentityContext(id);
            user.setEmail(email);
            user.setUsername(username);
            user.setName(firstName + lastName);
            user.setIdpConfig(getConfig());
            user.setIdp(this);
            AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
            return user;
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from vk.", e);
        }
    }

    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new AbstractOAuth2IdentityProvider.Endpoint(callback, realm, event);
    }

    protected class Endpoint extends AbstractOAuth2IdentityProvider.Endpoint {
        public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
            super(callback, realm, event);
        }

        @Override
        public SimpleHttp generateTokenRequest(String authorizationCode) {
            JSSETruststoreConfigurator configurator = new JSSETruststoreConfigurator(session);
            return SimpleHttp.doGet(getConfig().getTokenUrl())
                    .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                    .param(OAUTH2_PARAMETER_CLIENT_ID, getConfig().getClientId())
                    .param(OAUTH2_PARAMETER_CLIENT_SECRET, getConfig().getClientSecret())
                    .param(OAUTH2_PARAMETER_REDIRECT_URI, uriInfo.getAbsolutePath().toString())
                    .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
                    .sslFactory(configurator.getSSLSocketFactory())
                    .hostnameVerifier(configurator.getHostnameVerifier());
        }
    }
}