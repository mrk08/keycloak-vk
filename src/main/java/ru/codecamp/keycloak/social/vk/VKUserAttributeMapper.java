package ru.codecamp.keycloak.social.vk;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * Created by zk on 22/06/2018.
 */
public class VKUserAttributeMapper extends AbstractJsonUserAttributeMapper {

    private static final String[] cp = new String[]{VKIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "vk-user-attribute-mapper";
    }
}
