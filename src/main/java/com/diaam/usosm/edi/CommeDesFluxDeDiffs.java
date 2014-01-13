/*
 */
package com.diaam.usosm.edi;

import com.diaam.usosm.edi.entities.Changeset;
import com.diaam.usosm.edi.entities.Contributeur;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author herve
 */
public interface CommeDesFluxDeDiffs extends java.io.Closeable
{
  public Reader state() throws IOException;
  public Reader diffDay(long séquence) throws IOException;
  public Reader user(Contributeur contributeur) throws IOException;
  public Reader changeset(Changeset change) throws IOException;
  public Reader lieu(Changeset change) throws IOException ;
  
  public static final class SurWeb implements CommeDesFluxDeDiffs
  {
    private final CloseableHttpClient m_http;
    private ArrayList<Closeable> m_àCloser;
    
    public SurWeb()
    {
      m_http = HttpClients.createDefault();
      m_àCloser = new ArrayList<>();
    }
    
    @Override
    public Reader state() throws IOException
    {
      HttpGet getstate;
      final CloseableHttpResponse responsestate;
      final InputStreamReader isr;
      Reader rstate;
      
        getstate = new HttpGet(ContribsOSM.uriDiffs().resolve("state.txt"));
        responsestate = m_http.execute(getstate);
        isr = new InputStreamReader(responsestate.getEntity().getContent());
        rstate = new LecteurReseau()
        {
          @Override
          public int read(char[] cbuf, int off, int len) throws IOException
          {
            return isr.read(cbuf, off, len);
          }

          @Override
          public void close() throws IOException
          {
            isr.close();
            responsestate.close();
          }
        };
        m_àCloser.add(rstate);
        return rstate;
    }

    @Override
    public Reader diffDay(long séquence) throws IOException
    {
      HttpGet getdiff;
      final GZIPInputStream gzip;
      final InputStreamReader isr;
      final CloseableHttpResponse responsediff;
      Reader rdiff;

      getdiff = new HttpGet(
       "http://planet.openstreetmap.org/replication/day/000/000/"
       + séquence
       + ".osc.gz");
      responsediff = m_http.execute(getdiff);
      gzip = new GZIPInputStream(responsediff.getEntity().getContent());
      isr = new InputStreamReader(gzip);
      rdiff = new LecteurReseau()
      {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
          return isr.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException
        {
          isr.close();
          gzip.close();
          responsediff.close();
System.out.println(getClass().toString()+" bien fermé (j'espère).");
        }
      };
      m_àCloser.add(rdiff);
      return rdiff;
    }

    @Override
    public Reader user(Contributeur contributeur) throws IOException
    {
      HttpGet getbuteur;
      final CloseableHttpResponse resbuteur;
      final InputStream isbuteur;
      final InputStreamReader isr;
      Reader ruser;

      getbuteur = new HttpGet(
       "http://api.openstreetmap.org/api/0.6/user/" + contributeur.getUID());
      resbuteur = m_http.execute(getbuteur);
      isbuteur = resbuteur.getEntity().getContent();
      isr = new InputStreamReader(isbuteur);
      ruser = new LecteurReseau()
      {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
          int i;

          i = isr.read(cbuf, off, len);
          return i;
        }

        @Override
        public void close() throws IOException
        {
          isr.close();
          resbuteur.close();
        }
      };
      m_àCloser.add(ruser);
      return ruser;
    }

    @Override
    public Reader changeset(Changeset change) throws IOException
    {
      HttpGet getapi;
      final CloseableHttpResponse respapi;
      final InputStreamReader isr;
      Reader rchange;
      
              getapi = new HttpGet(
               "http://api.openstreetmap.org/api/0.6/changeset/"
               +change.getIdSet());
              respapi = m_http.execute(getapi);
              isr = new InputStreamReader(respapi.getEntity().getContent());
              rchange = new LecteurReseau()
              {

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
          return isr.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException
        {
          isr.close();
          respapi.close();
        }
                
              };
              m_àCloser.add(rchange);
              return rchange;
    }

    @Override
    public Reader lieu(Changeset change) throws IOException
    {
      HttpGet getnomi;
      final CloseableHttpResponse respnomi;
      final InputStreamReader isr;
      Reader rlieu;

      getnomi = new HttpGet(
       "http://nominatim.openstreetmap.org/reverse?format=xml&lat="
       + change.getMinLat()
       + "&lon="
       + change.getMaxLon());
      respnomi = m_http.execute(getnomi);
      isr = new InputStreamReader(respnomi.getEntity().getContent());
      rlieu = new LecteurReseau()
      {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
          return isr.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException
        {
          isr.close();
          respnomi.close();
        }
      };
      m_àCloser.add(rlieu);
      return rlieu;
    }
    
    @Override
    public void close() throws IOException
    {
      for (Closeable clos : m_àCloser)
        clos.close();
      m_àCloser.clear();
    }
  }
  
  public static abstract class LecteurReseau extends java.io.Reader
  {
  }
}
