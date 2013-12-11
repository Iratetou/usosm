/*
 */
package com.diaam.usosm.edi.entities;

import com.google.common.base.Optional;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author herve
 */
public final class Tities
{
  private EntityManager m_manager;
  private TypedQuery<Diff> m_diffDeSéquence;
  private TypedQuery<Rapport> m_rapports;
  private TypedQuery<Contributeur> m_contributeurs;
  
  public Tities(EntityManager manager)
  {
    m_manager = manager;
    m_diffDeSéquence = m_manager.createNamedQuery(
     "Diff.par séquence", Diff.class);
    m_rapports = m_manager.createNamedQuery("Rapport.tous", Rapport.class);
    m_contributeurs = m_manager.createNamedQuery(
     "tous les contributeurs", Contributeur.class);
  }
  
  public Optional<Diff> diff(long séquence)
  {
    Optional<Diff> op;
    
    m_diffDeSéquence.setParameter("sequence", Long.valueOf(séquence));
    try
    {
      op = Optional.of(m_diffDeSéquence.getSingleResult());
    }
    catch (NoResultException no)
    {
      op = Optional.absent();
    }
//System.out.println("?diff pour "+séquence+" = "+op)    ;
    return op;
  }
  
  public List<Rapport> rapports()
  {
    return m_rapports.getResultList();
  }
  
  public List<Contributeur> contributeurs()
  {
    return m_contributeurs.getResultList();
  }
}
