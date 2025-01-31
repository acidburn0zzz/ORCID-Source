package org.orcid.jaxb.model.v3.dev1.record.summary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.orcid.jaxb.model.v3.dev1.common.LastModifiedDate;
import org.orcid.jaxb.model.v3.dev1.record.Group;
import org.orcid.jaxb.model.v3.dev1.record.GroupsContainer;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "lastModifiedDate", "fundingGroup" })
@XmlRootElement(name = "fundings", namespace = "http://www.orcid.org/ns/activities")
public class Fundings implements GroupsContainer, Serializable {
    
    private static final long serialVersionUID = -1446924819201177350L;
    @XmlElement(name = "last-modified-date", namespace = "http://www.orcid.org/ns/common")
    protected LastModifiedDate lastModifiedDate;
    @XmlElement(name = "group", namespace = "http://www.orcid.org/ns/activities")
    List<FundingGroup> fundingGroup;
    @XmlAttribute
    protected String path;
    
    public List<FundingGroup> getFundingGroup() {
        if (fundingGroup == null)
            fundingGroup = new ArrayList<FundingGroup>();
        return fundingGroup;
    }

    public Collection<? extends Group> retrieveGroups() {
        return getFundingGroup();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fundingGroup == null) ? 0 : fundingGroup.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Fundings other = (Fundings) obj;
        if (fundingGroup == null) {
            if (other.fundingGroup != null)
                return false;
        } else if (!fundingGroup.equals(other.fundingGroup))
            return false;
        return true;
    }

    public LastModifiedDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LastModifiedDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
