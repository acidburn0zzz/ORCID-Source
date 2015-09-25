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
package org.orcid.core.manager;

import java.util.List;

import org.orcid.jaxb.model.message.ResearcherUrls;
import org.orcid.persistence.jpa.entities.ResearcherUrlEntity;

public interface ResearcherUrlManager {

    /**
     * Return the list of researcher urls associated to a specific profile
     * @param orcid
     * @return 
     *          the list of researcher urls associated with the orcid profile
     * */
    public List<ResearcherUrlEntity> getResearcherUrls(String orcid);

    
    /**
     * Return the list of researcher urls associated to a specific profile
     * @param orcid
     * @return 
     *          the list of researcher urls associated with the orcid profile
     * */
    public org.orcid.jaxb.model.record.ResearcherUrls getResearcherUrlsV2(String orcid);
    
    /**
     * Deleted a researcher url from database
     * @param id
     * @return true if the researcher url was successfully deleted
     * */
    public boolean deleteResearcherUrl(long id);

    /**
     * Retrieve a researcher url from database
     * @param id
     * @return the ResearcherUrlEntity associated with the parameter id
     * */
    public ResearcherUrlEntity getResearcherUrl(long id);

    /**
     * Retrieve a researcher url from database
     * @param id
     * @return the ResearcherUrlEntity associated with the parameter id
     * */
    public org.orcid.jaxb.model.record.ResearcherUrl getResearcherUrlV2(long id);
    
    /**
     * Updates an existing url
     * @param orcid
     * @param researcherUrl
     * @return the updated researcher url
     * */
    public org.orcid.jaxb.model.record.ResearcherUrl updateResearcherUrlV2(String orcid, org.orcid.jaxb.model.record.ResearcherUrl researcherUrl);
    
    
    /**
     * Adds a researcher url to a specific profile
     * @param orcid
     * @param url
     * @param urlName
     * @return true if the researcher url was successfully created on database
     * */
    public void addResearcherUrls(String orcid, String url, String urlName);

    /**
     * Add a new researcher url to a specific profile
     * @param researcherUrl
     * @return true if the researcher url was successfully created on database
     * */
    public void addResearcherUrlV2(org.orcid.jaxb.model.record.ResearcherUrl researcherUrl);
    
    
    /**
     * Update the researcher urls associated with a specific account
     * @param orcid
     * @param researcherUrls
     * */
    public boolean updateResearcherUrls(String orcid, ResearcherUrls researcherUrls);
}
