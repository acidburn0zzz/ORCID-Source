/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.frontend.web.controllers;

import javax.annotation.Resource;

import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.ThirdPartyLinkManager;
import org.orcid.pojo.ajaxForm.Errors;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

/**
 * @author rcpeters
 */
@Controller("cacheClearController")
@RequestMapping(value = { "/cacheClear" })
public class CacheClearController extends BaseWorkspaceController {

    @Resource
    private ThirdPartyLinkManager thirdPartyLinkManager;

    @Resource
    private ProfileEntityCacheManager profileEntityCacheManager;

    @Resource(name = "salesForceMembersListCache")
    private SelfPopulatingCache salesForceMembersListCache;

    @RequestMapping(value = "/thirdPartyLinkManager.json", method = RequestMethod.GET)
    public @ResponseBody Errors clearThirdPartyLinkManager() {
        thirdPartyLinkManager.evictAll();
        return new Errors();
    }

    @RequestMapping(value = "/profileEntityCache.json", method = RequestMethod.GET)
    public @ResponseBody Errors clearProfileEntityCacheManager() {
        profileEntityCacheManager.removeAll();
        return new Errors();
    }

    @RequestMapping(value = "/salesForceCache.json", method = RequestMethod.GET)
    public @ResponseBody Errors clearSalesForceCacheManager() {
        salesForceMembersListCache.removeAll();
        return new Errors();
    }

}