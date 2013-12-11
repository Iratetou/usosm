/*
 */
package com.diaam.usosm.edi.entities;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
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

/**
 *
 * @author herve
 */
@Entity
@NamedQueries(
 {
 @NamedQuery(
 name = "Diff.par séquence", 
 query = "select s FROM Diff s WHERE s.sequenceNumber = :sequence"),
 })
@XmlRootElement
public class Diff implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(unique=true, nullable=false)
  private Long sequenceNumber;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date diffTimestamp;
    
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
    if (!(object instanceof Diff))
    {
      return false;
    }
    Diff other = (Diff) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return "com.diaam.contribsosm.entities.Diff[ id=" + id + " ]";
  }

  public Long getSequenceNumber()
  {
    return sequenceNumber;
  }

  public void setSequenceNumber(Long sequenceNumber)
  {
    this.sequenceNumber = sequenceNumber;
  }

  public Date getTimestamp()
  {
    return diffTimestamp;
  }

  public void setTimestamp(Date timestamp)
  {
    this.diffTimestamp = timestamp;
  } 
}
