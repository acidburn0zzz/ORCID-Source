package org.orcid.core.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum Features implements Feature {

    @Label("Affiliation org ID in UI")
    AFFILIATION_ORG_ID,

    @Label("Affiliation search")
    AFFILIATION_SEARCH,

    @Label("Badges on member details")
    BADGES,
    
    @Label("Cookie policy banner")
    COOKIE_BANNER,

    @Label("UI changes for GDPR compliance")
    GDPR_UI,

    @Label("Https for links to iDs")
    HTTPS_IDS,

    @Label("Arabic translation")
    LANG_AR,

    @Label("Last modified")
    LAST_MOD,

    @Label("New footer")
    NEW_FOOTER,

    @Label("Mutiple emails on register form")
    REG_MULTI_EMAIL,

    @Label("Revoke access token if authorization code is reused")
    REVOKE_TOKEN_ON_CODE_REUSE,

    @Label("Affiliations in search results")
    SEARCH_RESULTS_AFFILIATIONS,

    @Label("Survey link")
    SURVEY,

    @Label("Two factor authentication")
    TWO_FACTOR_AUTHENTICATION,

    @Label("API analytics debug logging")
    API_ANALYTICS_DEBUG,

    @Label("Self service org ids")
    SELF_SERVICE_ORG_IDS,

    @Label("Turn on angular2 features that are on development")
    ANGULAR2_DEV,

    @Label("Turn on angular2 features that are ready for QA")
    ANGULAR2_QA,

    @Label("Turn off angular1 features that are legacy")
    ANGULAR1_LEGACY,

    @Label("Set the 2.0 API as the default one in the public API")
    PUB_API_2_0_BY_DEFAULT,

    @Label("Set the 2.0 API as the default one in the members API")
    MEMBER_API_2_0_BY_DEFAULT,

    @Label("Disable reCAPTCHA")
    DISABLE_RECAPTCHA,

    @Label("Display the new affiliation types in the workspace")
    DISPLAY_NEW_AFFILIATION_TYPES,

    @Label("Disable 1.2 API from the public API")
    DISABLE_1_2_ON_PUB_API,

    @Label("Disable 1.2 API from the member API")
    DISABLE_1_2_ON_MEMBER_API,

    @Label("Download all my data")
    GET_MY_DATA,

    @Label("Check external id resolution in UI")
    EX_ID_RESOLVER,

    @Label("Remove https://orcid.org from OpenID id_tokens")
    OPENID_SIMPLE_SUBJECT,
    
    @Label("Disable 1.1 version from any API")
    DISABLE_1_1;
    
    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
