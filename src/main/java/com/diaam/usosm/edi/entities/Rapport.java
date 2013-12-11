/*
 */
package com.diaam.usosm.edi.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 *
 * @author herve
 */
@Entity
@NamedQueries(
 {
 @NamedQuery(
 name = "Rapport.tous", 
 query = "select r from Rapport r")
 })
public class Rapport implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  
  private Diff diff;
  
  @OneToMany
  private List<Contributeur> bonsDébutants;

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
    if (!(object instanceof Rapport))
    {
      return false;
    }
    Rapport other = (Rapport) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return "com.diaam.usosm.edi.entities.Rapport[ id=" + id + " ]";
  }

  public Diff getDiff()
  {
    return diff;
  }

  public void setDiff(Diff diff)
  {
    this.diff = diff;
  }

  public List<Contributeur> getBonsDébutants()
  {
    return bonsDébutants;
  }

  public void setBonsDébutants(List<Contributeur> bonsDébutants)
  {
    this.bonsDébutants = bonsDébutants;
  }
}
