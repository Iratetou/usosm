/*
 */
package com.diaam.usosm.edi.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.config.CacheIsolationType;

/**
 *
 * @author herve
 */
@Entity
@XmlRootElement
@NamedQueries(
 {
 @NamedQuery(
 name = "Changeset.par idSet", 
 query = "select c from Changeset c WHERE c.idSet = :idSet"),
 })
public class Changeset implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(unique=true, nullable=false)
  private String idSet;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date chgTimestamp;
  
  private String minLon;
  private String maxLon;
  private String minLat;
  private String maxLat;
  
  private String countryCode;
  private String country;
  private String chgState;
  private String county;
  
  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  @Override
  public int hashCode()
  {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object)
  {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Changeset))
    {
      return false;
    }
    Changeset other = (Changeset) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return 
     "com.diaam.contribsosm.entities.Changeset[ id=" 
     + id 
     + ", idSet=" 
     + idSet 
     + " ]";
  }

  public String getIdSet()
  {
    return idSet;
  }

  public void setIdSet(String idSet)
  {
    this.idSet = idSet;
  }

  public Date getChgTimestamp()
  {
    return chgTimestamp;
  }

  public void setChgTimestamp(Date timestamp)
  {
    this.chgTimestamp = timestamp;
  }

  public String getMaxLat()
  {
    return maxLat;
  }

  public String getMaxLon()
  {
    return maxLon;
  }

  public String getMinLat()
  {
    return minLat;
  }

  public String getMinLon()
  {
    return minLon;
  }

  public void setMaxLat(String maxLat)
  {
    this.maxLat = maxLat;
  }

  public void setMaxLon(String maxLon)
  {
    this.maxLon = maxLon;
  }

  public void setMinLat(String minLat)
  {
    this.minLat = minLat;
  }

  public void setMinLon(String minLon)
  {
    this.minLon = minLon;
  }

  public String getCountry()
  {
    return country;
  }

  public void setCountry(String country)
  {
    this.country = country;
  }

  public String getCountryCode()
  {
    return countryCode;
  }

  public void setCountryCode(String countryCode)
  {
    this.countryCode = countryCode;
  }

  public String getCounty()
  {
    return county;
  }

  public void setCounty(String county)
  {
    this.county = county;
  }

  public String getChgState()
  {
    return chgState;
  }

  public void setChgState(String state)
  {
    this.chgState = state;
  }
}
