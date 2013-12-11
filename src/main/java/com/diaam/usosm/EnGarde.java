/*
 */
package com.diaam.usosm;

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
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Web application lifecycle listener.
 *
 * @author herve
 */
@WebListener()
public class EnGarde implements ServletContextListener
{
//  private static final Semaphore m_lock = new Semaphore(1);
  
  private ListeningScheduledExecutorService t_exec;
  private CloseableHttpClient t_cliHTTP;
  private String t_précédenteLigneDeSéquence;
  private Tities t_tities;
  private ContribsOSM t_contribs;
  
  @Override
  public void contextInitialized(ServletContextEvent sce)
  {
    try
    {
      sce.getServletContext().log("Et OOOOOoooouuuuiiiiii....");
      t_contribs = new ContribsOSM();
      t_contribs.f_entités.getTransaction().begin();
      t_tities = t_contribs.f_tities;
      t_cliHTTP = HttpClients.createDefault();
      t_exec = MoreExecutors.listeningDecorator(
       Executors.newSingleThreadScheduledExecutor());
      ListenableScheduledFuture<?> nable = t_exec.scheduleAtFixedRate(
       new EcouteurDesDiffs(), 10, 3600, TimeUnit.SECONDS);
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
      
      if (t_cliHTTP != null)
        t_cliHTTP.close();
      if (t_exec != null)
        t_exec.shutdown();
      if (t_contribs != null)
      {
        EntityTransaction et;
        
        et = t_contribs.f_entités.getTransaction(); 
        if (et.isActive())
          if (!et.getRollbackOnly())
            et.commit();
        t_contribs.close();
      }
      sce.getServletContext().log("NOOOOOOOOoooooooonnnnn....");
    }
    catch (IOException mince)
    {
      sce.getServletContext().log(null, mince);
    }
    finally
    {
    }
  }

  private class EcouteurDesDiffs implements java.lang.Runnable
  {
    @Override
    public void run()
    {
      try
      {
        HttpGet getstate;
        CloseableHttpResponse responsestate;
        InputStreamReader isr;
        BufferedReader br;
      
        getstate = new HttpGet(ContribsOSM.uriDiffs().resolve("state.txt"));
        responsestate = t_cliHTTP.execute(getstate);
        isr = new InputStreamReader(responsestate.getEntity().getContent());
        br = new BufferedReader(isr);
        br.readLine();
        String lnseq = br.readLine();
        if (!lnseq.equals(t_précédenteLigneDeSéquence))
        {
          long seq;
          
          seq = Long.valueOf(lnseq.substring(lnseq.indexOf('=')+1)).longValue();
          if (!t_tities.diff(seq).isPresent())
          {
            Diff diff;
            
            diff = new Diff();
            diff.setSequenceNumber(seq);
            String lntimestamp = br.readLine();
            String strtimestamp = lntimestamp
             .substring(lntimestamp.indexOf('=') + 1)
             .replaceAll("\\\\", "");
            Date dttimestamp = 
             ContribsOSM.dateFormatDansLesDiffs().parse(strtimestamp);
            diff.setTimestamp(dttimestamp);            
            t_contribs.f_entités.persist(diff);
            t_contribs.analyseDayDiffEtFaitLeRapport(diff);
          }
          t_précédenteLigneDeSéquence = lnseq;
        }
        br.close();
        responsestate.close();
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
      LoggerFactory.getLogger(getClass()).warn("", t);
    }  
  }
}
