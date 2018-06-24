package ru.codecamp.keycloak.social.vk;


import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;


/**
 * Created by zk on 22/06/2018.
 */
public class VKIdentityProviderFactory extends AbstractIdentityProviderFactory<VKIdentityProvider> implements SocialIdentityProviderFactory<VKIdentityProvider> {
    public static final String PROVIDER_ID = "vk";

    @Override
    public String getName() {
        return "VK";
    }

    @Override
    public VKIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new VKIdentityProvider(session, new OAuth2IdentityProviderConfig(model));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
