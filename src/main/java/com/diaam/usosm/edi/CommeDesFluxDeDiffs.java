/*
 */
package com.diaam.usosm.edi;

import com.diaam.usosm.edi.entities.Changeset;
import com.diaam.usosm.edi.entities.Contributeur;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author herve
 */
public interface CommeDesFluxDeDiffs
{
  public Reader state() throws IOException;
  public Reader diffDay(long séquence) throws IOException;
  public Reader user(Contributeur contributeur) throws IOException;
  public Reader changeset(Changeset change) throws IOException;
  public Reader lieu(Changeset change) throws IOException ;
  
  public static final class SurWeb implements CommeDesFluxDeDiffs
  {
    private final CloseableHttpClient m_http;
    
    public SurWeb()
    {
      m_http = HttpClients.createDefault();
    }
    
    @Override
    public Reader state() throws IOException
    {
      HttpGet getstate;
      final CloseableHttpResponse responsestate;
      final InputStreamReader isr;
      
        getstate = new HttpGet(ContribsOSM.uriDiffs().resolve("state.txt"));
        responsestate = m_http.execute(getstate);
        isr = new InputStreamReader(responsestate.getEntity().getContent());
        return new LecteurReseau()
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
    }

    @Override
    public Reader diffDay(long séquence) throws IOException
    {
      HttpGet getdiff;
      GZIPInputStream gzip;
      final InputStreamReader isr;
      final CloseableHttpResponse responsediff;

      getdiff = new HttpGet(
       "http://planet.openstreetmap.org/replication/day/000/000/"
       + séquence
       + ".osc.gz");
      responsediff = m_http.execute(getdiff);
      gzip = new GZIPInputStream(responsediff.getEntity().getContent());
      isr = new InputStreamReader(gzip);
      return new LecteurReseau()
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
          responsediff.close();
        }
      };
    }

    @Override
    public Reader user(Contributeur contributeur) throws IOException
    {
      HttpGet getbuteur;
      final CloseableHttpResponse resbuteur;
      final InputStream isbuteur;
      final InputStreamReader isr;

      getbuteur = new HttpGet(
       "http://api.openstreetmap.org/api/0.6/user/" + contributeur.getUID());
System.out.println("getbuteur = "+getbuteur.getURI())          ;
      resbuteur = m_http.execute(getbuteur);
      isbuteur = resbuteur.getEntity().getContent();
      isr = new InputStreamReader(isbuteur);
      return new LecteurReseau()
      {
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException
        {
          int i;

          i = isr.read(cbuf, off, len);
if (i >= 0)System.out.println("i = "+i+", len="+len+", read = "+new String(cbuf, off, i))          ;else System.out.println("read fini.");
          return i;
        }

        @Override
        public void close() throws IOException
        {
          isr.close();
          resbuteur.close();
        }
      };
    }

    @Override
    public Reader changeset(Changeset change) throws IOException
    {
      HttpGet getapi;
      final CloseableHttpResponse respapi;
      final InputStreamReader isr;
      
              getapi = new HttpGet(
               "http://api.openstreetmap.org/api/0.6/changeset/"
               +change.getIdSet());
              respapi = m_http.execute(getapi);
              isr = new InputStreamReader(respapi.getEntity().getContent());
              return new LecteurReseau()
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
    }

    @Override
    public Reader lieu(Changeset change) throws IOException
    {
      HttpGet getnomi;
      final CloseableHttpResponse respnomi;
      final InputStreamReader isr;

      getnomi = new HttpGet(
       "http://nominatim.openstreetmap.org/reverse?format=xml&lat="
       + change.getMinLat()
       + "&lon="
       + change.getMaxLon());
      respnomi = m_http.execute(getnomi);
      isr = new InputStreamReader(respnomi.getEntity().getContent());
      return new LecteurReseau()
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
    }
  }

  public static interface ParLigne extends java.io.Closeable
  {
    String ligne() throws IOException;
  }
  
  public static abstract class LecteurReseau extends java.io.Reader
  {
    
  }
}
