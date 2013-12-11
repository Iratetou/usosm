/*
 */
package com.diaam.usosm.edi.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
//import org.eclipse.persistence.annotations.Index;

/**
 *
 * @author herve
 */
@Entity
@NamedQueries(
 {
 @NamedQuery(
 name = "un contributeur", 
 query = "select c from Contributeur c WHERE c.UID = :uid"),
 @NamedQuery(
 name = "tous les contributeurs", 
 query = "select c from Contributeur c")
 })
@XmlRootElement
public class Contributeur implements Serializable
{
  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  
//  @Column(unique=true, nullable=false)
//  private String nom;
  
  @Column(unique=true, nullable=false)
  private Long UID;
  
  private String pseudo;

  @OneToMany(cascade = CascadeType.PERSIST)
  private List<Changeset> changesets;
  
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
    if (!(object instanceof Contributeur))
    {
      return false;
    }
    Contributeur other = (Contributeur) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return "com.diaam.contribsosm.entities.Contributeur[ id=" + id + " ]";
  }

//  public String getNom()
//  {
//    return nom;
//  }
//
//  public void setNom(String nom)
//  {
//    this.nom = nom;
//  }

  @XmlTransient
  public List<Changeset> getChangesets()
  {
    return changesets;
  }

  public void setChangesets(List<Changeset> changesets)
  {
    this.changesets = changesets;
  }

  public Long getUID()
  {
    return UID;
  }

  public void setUID(Long UID)
  {
    this.UID = UID;
  }

  public String getPseudo()
  {
    return pseudo;
  }

  public void setPseudo(String pseudo)
  {
    this.pseudo = pseudo;
  }
}
