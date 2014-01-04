/*
 */
package com.diaam.usosm.edi.entities;

import com.google.common.base.Optional;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.slf4j.LoggerFactory;

/**
 *
 * @author herve
 */
public final class Tities
{
  public final EntityManager f_manager;
  private TypedQuery<Diff> m_diffDeSéquence;
  private TypedQuery<Rapport> m_rapports;
  private TypedQuery<Contributeur> m_contributeurs;
  private TypedQuery<Changeset> m_changesetSelonIdSet;
  
  public Tities(EntityManager manager)
  {
    f_manager = manager;
    m_diffDeSéquence = f_manager.createNamedQuery(
     "Diff.par séquence", Diff.class);
    m_rapports = f_manager.createNamedQuery("Rapport.tous", Rapport.class);
    m_contributeurs = f_manager.createNamedQuery(
     "tous les contributeurs", Contributeur.class);
    m_changesetSelonIdSet = f_manager.createNamedQuery(
     "Changeset.par idSet", Changeset.class);
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
    return op;
  }
  
  public List<Rapport> rapports()
  {
    return m_rapports.getResultList();
  }
  
  public List<Contributeur> contributeurs()
  {
    EntityTransaction et;

    et = f_manager.getTransaction();
    if (!et.isActive())
      et.begin();
    try
    {
      f_manager.flush();
    }
    catch (PersistenceException pe)
    {
      LoggerFactory.getLogger(getClass()).warn("", pe);
      et.rollback();
    }
    return m_contributeurs.getResultList();
  }
  
  public Optional<Changeset> changeset(String idSet)
  {
    Optional<Changeset> op;
    
    m_changesetSelonIdSet.setParameter("idSet", idSet);
    try
    {
      op = Optional.of(m_changesetSelonIdSet.getSingleResult());
    }
    catch (NoResultException no)
    {
      op = Optional.absent();
    }
    return op;    
  }
}
