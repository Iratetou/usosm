/*
 */
package com.diaam.usosm;

import com.diaam.usosm.edi.CommeDesFluxDeDiffs;
import com.diaam.usosm.edi.ContribsOSM;
import com.diaam.usosm.edi.entities.Diff;
import com.diaam.usosm.edi.entities.Tities;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.Executors;
//import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Web application lifecycle listener.
 *
 * @author herve
 */
@WebListener()
public final class EnGarde implements ServletContextListener
{
  private ListeningScheduledExecutorService t_exec;
  private Tities t_tities;
  private ContribsOSM t_contribs;
  private EcouteurDesDiffs t_desDiffs;

  public EnGarde()
  {
  }
  
  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    try
    {
      sce.getServletContext().log("Et OOOOOoooouuuuiiiiii....");
      t_contribs = new ContribsOSM();
      t_contribs.f_entités.clear();
      t_contribs.f_entités.getTransaction().begin();
      t_tities = t_contribs.f_tities;
      t_desDiffs = new EcouteurDesDiffs(t_contribs);
      t_exec = MoreExecutors.listeningDecorator(
       Executors.newSingleThreadScheduledExecutor());
      ListenableScheduledFuture<?> nable = t_exec.scheduleAtFixedRate(
       t_desDiffs, 10, 3600, TimeUnit.SECONDS);
      Futures.addCallback(nable, new ResultatEcoute());
      sce.getServletContext().setAttribute("ap", new ApEnBean(t_tities));
    }
    finally
    {
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce)
  {
    try
    {
      if (t_desDiffs != null)
        t_desDiffs.m_cliHTTP.close();
      if (t_exec != null)
        t_exec.shutdownNow();
      if (t_contribs != null)
      {
        EntityTransaction et;
        Driver driv;
        EntityManager mana;
        
        mana = t_contribs.f_entités;
        et = mana.getTransaction(); 
        if (et.isActive())
          if (!et.getRollbackOnly())
            et.commit();
        driv = DriverManager.getDriver(
         (String)mana.getProperties().get("javax.persistence.jdbc.url"));
        t_contribs.close();
        DriverManager.deregisterDriver(driv);
      }
      sce.getServletContext().log("NOOOOOOOOoooooooonnnnn....");
    }
    catch (IOException| SQLException mince)
    {
      sce.getServletContext().log(null, mince);
    }
    finally
    {
    }
  }

  public final static class EcouteurDesDiffs implements java.lang.Runnable
  {
    private final CloseableHttpClient m_cliHTTP;
    private String m_précédenteLigneDeSéquence;
    private final ContribsOSM m_osm;
    private CommeDesFluxDeDiffs m_fluxDeDiffs;

    public EcouteurDesDiffs(ContribsOSM osm)
    {
      m_cliHTTP = HttpClients.createDefault();
      m_précédenteLigneDeSéquence = "";
      m_osm = osm;
      m_fluxDeDiffs = new CommeDesFluxDeDiffs.SurWeb();
    }
    
    @Override
    public void run()
    {
      Diff diff;
      EntityManager em;
      
      diff = null;
      em = m_osm.f_entités;
      try (CommeDesFluxDeDiffs flux = m_fluxDeDiffs)
      {
        BufferedReader pl;
        String lnseq;
      
        pl = new BufferedReader(flux.state());
        pl.readLine();
        lnseq = pl.readLine();
        if (!lnseq.equals(m_précédenteLigneDeSéquence))
        {
          long seq;
          
          seq = Long.valueOf(lnseq.substring(lnseq.indexOf('=')+1)).longValue();
          if (!m_osm.f_tities.diff(seq).isPresent())
          {
            String lntimestamp;
            
            diff = new Diff();
            diff.setSequenceNumber(seq);
            lntimestamp = pl.readLine();
            String strtimestamp = lntimestamp
             .substring(lntimestamp.indexOf('=') + 1)
             .replaceAll("\\\\", "");
            Date dttimestamp = 
             ContribsOSM.dateFormatDansLesDiffs().parse(strtimestamp);
            diff.setTimestamp(dttimestamp);
            if (!em.getTransaction().isActive())
              em.getTransaction().begin();
            em.persist(diff);
            em.flush();
            m_osm.analyseDayDiffEtFaitLeRapport(diff, flux);
          }
          m_précédenteLigneDeSéquence = lnseq;
        }
        pl.close();
      }
      catch (SAXParseException|ConnectionClosedException parsex)
      {
        LoggerFactory.getLogger(getClass()).warn(
         diff == null ? "diff null" : diff.toString(), parsex);
        if (diff != null)
        {
          em.getTransaction().rollback();
          em.remove(diff);
        }
      }
      catch (
       IOException
        | ParseException 
        | ParserConfigurationException 
        | SAXException mince)
      {
        LoggerFactory.getLogger(getClass()).warn(null, mince);
      }
    } 
  
    public void setFluxDeDiff(CommeDesFluxDeDiffs flux) 
    {
      m_fluxDeDiffs = flux;
    }

    public CommeDesFluxDeDiffs getFluxDeDiffs()
    {
      return m_fluxDeDiffs;
    }
  }
  
  private static class ResultatEcoute implements 
   com.google.common.util.concurrent.FutureCallback
  {
    @Override
    public void onSuccess(Object result)
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onFailure(Throwable t)
    {
      System.err.println("en onFailure");
      t.printStackTrace();
      System.err.println("FIN en onFailure");
      LoggerFactory.getLogger(getClass()).warn("", t);
    }  
  }
}
