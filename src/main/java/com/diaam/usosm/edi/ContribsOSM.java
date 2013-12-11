/*
 */
package com.diaam.usosm.edi;

import com.diaam.usosm.edi.entities.Changeset;
import com.diaam.usosm.edi.entities.Contributeur;
import com.diaam.usosm.edi.entities.Diff;
import com.diaam.usosm.edi.entities.Rapport;
import com.diaam.usosm.edi.entities.Tities;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import javax.persistence.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author herve
 */
public final class ContribsOSM implements java.io.Closeable
{
  private final static URI URI_DIFFS = URI.create(
   "http://planet.openstreetmap.org/replication/day/");
  private final static SimpleDateFormat DATE_FORMAT_DIFF = 
   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");;

  public static final EntityManagerFactory f_fabriqueDesEntités;
  public static final EntityManager f_entités;
  
  static
  {
    f_fabriqueDesEntités = Persistence.createEntityManagerFactory(
     "contribsosm");
    f_entités = f_fabriqueDesEntités.createEntityManager();    ;
  }
  
  public final Tities f_tities;

  public ContribsOSM()
  {
    f_tities = new Tities(f_entités);
  }
  
  @Override
  public void close() throws IOException
  {
    f_entités.close();
    f_fabriqueDesEntités.close();
  }
  
  private static class ManipSAX extends org.xml.sax.helpers.DefaultHandler
  {
    private SimpleDateFormat m_dtFormat;
    private EntityManager m_entities;
    private Query m_selectCorrespondant;
    private String t_précédentUID;
    private String t_précédentChangeset;
    private Contributeur t_précédentContributeur;
    LoadingCache<Long, Contributeur> contributeurs;

    ManipSAX(EntityManager entities)
    {
      m_dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      m_entities = entities;
      m_selectCorrespondant = m_entities.createNamedQuery("un contributeur");
      contributeurs = CacheBuilder.newBuilder()
       .maximumSize(400)
       .build(
       new CacheLoader<Long, Contributeur>()
      {
        @Override
        public Contributeur load(Long key) throws Exception
        {
          Contributeur buteur;

          m_selectCorrespondant.setParameter("uid", key);
          try
          {
            buteur =
             (Contributeur) m_selectCorrespondant.getSingleResult();
          }
          catch (NoResultException no)
          {
            buteur = new Contributeur();
            buteur.setUID(key);
            buteur.setChangesets(new ArrayList<Changeset>());
          }
          return buteur;
        }
      });
    }

    @Override
    public void startElement(
     String uri, String localName, String qName, Attributes attributes) throws 
     SAXException
    {
      try
      {
        switch (localName)
        {
          case "osm":
          case "bounds":
          case "tag":
          case "nd":
          case "member":
          case "osmChange":
          case "modify":
          case "delete":
          case "create":
            break;
          case "node":
          case "way":
          case "relation":
            String strchange;
            boolean mêmequeprécédent;
//            String struser;
            String struid;

            strchange = attributes.getValue("changeset");
//            struser = attributes.getValue("user");
            struid = attributes.getValue("uid");
            mêmequeprécédent = 
             struid.equals(t_précédentUID) 
             && strchange.equals(t_précédentChangeset);
            if (!mêmequeprécédent)
            {
//              if (struser.equals(t_précédentUID))
              if (struid.equals(t_précédentUID))
                ajouteChangeSetSiAbsent(t_précédentContributeur, attributes);
              else
              {
//                List corresp;
                Contributeur buteur;
//                Long uid;

////              System.out.println(
////               attributes.getValue("user")
////               + " ** "
////               + attributes.getValue("changeset"));
////                m_selectCorrespondant.setParameter("nom", struser);
////                corresp = m_selectCorrespondant.getResultList();
////                if (corresp.isEmpty())
//                buteur = contributeurs.get(struser);
                buteur = contributeurs.get(Long.parseLong(struid));
//System.out.println("buteur="+buteur+", struser="+struser)                ;
////                {
//                  Changeset change;
//
//                  buteur = new Contributeur();
//                  buteur.setNom(struser);
//                  buteur.setChangesets(new ArrayList<Changeset>());
//                  change = new Changeset();
//                  change.setIdSet(strchange);
//                  buteur.getChangesets().add(change);
//                  m_entities.persist(buteur);
////                System.out.println("ajout " + attributes.getValue("user"));
//                }
//                else
//                {
////                boolean changepaslà;
//
//                  buteur = (Contributeur) corresp.get(0);
////                changepaslà = true;
////                for (Changeset change : buteur.getChangesets())
////                  if (change.getIdSet().equals(strchange))
////                  {
////                    changepaslà = false;
////                    break;
////                  }
////                if (changepaslà)
////                {
////                  Changeset change;
////
////                  change = new Changeset();
////                  change.setIdSet(strchange);
////                  buteur.getChangesets().add(change);
////                }
                  ajouteChangeSetSiAbsent(buteur, attributes);
                  if (buteur.getId() == null)
                    m_entities.persist(buteur);
//                }
                t_précédentContributeur = buteur;
              }
//              t_précédentUID = struser;
              t_précédentUID = struid;
              t_précédentChangeset = strchange;
            }
            break;
          default:
            throw new IllegalStateException(
             "uri=" + uri + ", localName=" + localName + ", qName=" + qName);
        }
      }
      catch (ParseException|ExecutionException mince)
      {
        throw new IllegalStateException(mince);
      }
      finally
      {
         
      }
    }
    
    private void ajouteChangeSetSiAbsent(
     Contributeur contributeur, Attributes xmlAttributs) throws ParseException
    {
      boolean changepaslà;

      changepaslà = true;
      for (Changeset change : contributeur.getChangesets())
        if (change.getIdSet().equals(xmlAttributs.getValue("changeset")))
        {
          changepaslà = false;
          break;
        }
      if (changepaslà)
      {
        Changeset change;

//System.out.println("changeset="+xmlAttributs.getValue("changeset")+" "+contributeur.getNom()+" / "+xmlAttributs.getValue("user"));
        change = new Changeset();
        change.setIdSet(xmlAttributs.getValue("changeset"));
        change.setChgTimestamp(
         m_dtFormat.parse(xmlAttributs.getValue("timestamp")));
        change.setMaxLat(xmlAttributs.getValue("max_lat"));
        change.setMaxLat(xmlAttributs.getValue("min_lat"));
        change.setMaxLat(xmlAttributs.getValue("max_lon"));
        change.setMaxLat(xmlAttributs.getValue("min_lon"));
        contributeur.getChangesets().add(change);
      }
    }
  }
  
  private static class ManipUserSAX extends org.xml.sax.helpers.DefaultHandler
  {
    private int m_nbChangeSets;
    private String m_displayName;
    
    ManipUserSAX()
    {
      m_nbChangeSets = -1;
      m_displayName = "";
    }
    
    @Override
    public void startElement(
     String uri, String localName, String qName, Attributes attributes) throws 
     SAXException
    {
      switch (localName)
      {
        case "changesets":
          m_nbChangeSets = Integer.parseInt(attributes.getValue("count"));
          break;
        case "user":
          m_displayName = attributes.getValue("display_name");
          break;
        default :
          ;
      }
    }
    
    private int nbChangeSets()
    {
      if (m_nbChangeSets < 0)
        throw new IllegalStateException();
      return m_nbChangeSets;
    }
    
    private String displayName()
    {
      if (m_displayName.isEmpty())
        throw new IllegalStateException();
      return m_displayName;
    }
  }
  
  public static URI uriDiffs()
  {
    return URI_DIFFS;
  }
  
  public static SimpleDateFormat dateFormatDansLesDiffs()
  {
    return DATE_FORMAT_DIFF;
  }
  
  public void analyseDayDiffEtFaitLeRapport(Diff sequence) throws 
   IOException, ParserConfigurationException, SAXException
  {
    ManipSAX manipsax;
    long seqnumb;
    CloseableHttpClient httpclientdiff;
    SAXParserFactory spf;
    SAXParser sp;
    XMLReader xmlrdiffs;
    Rapport rap;
    
      manipsax = new ManipSAX(f_entités);
      httpclientdiff = HttpClients.createDefault();
//      HttpGet getdiffstate = new HttpGet(
//       "http://planet.openstreetmap.org/replication/day/000/000/"
//       +sequence.getSequenceNumber()
//       +".state.txt");
//      CloseableHttpResponse responsestate = httpclientdiff.execute(
//       getdiffstate);
//      InputStreamReader isr = new InputStreamReader(
//       responsestate.getEntity().getContent());
//      BufferedReader br = new BufferedReader(isr);
//      br.readLine();
//      String lnseq = br.readLine();
//      if (
//       Long.valueOf(lnseq.substring(lnseq.indexOf('=')+1)).longValue() 
//       != sequence.getSequenceNumber().longValue())
//        throw new IllegalStateException();
//      String lntimestamp = br.readLine();
//      String strtimestamp = lntimestamp.substring(
//       lntimestamp.indexOf('=')+1).replaceAll("\\\\", "");
//      Date dttimestamp = manipsax.m_dtFormat.parse(strtimestamp);
//      diff.setTimestamp(dttimestamp);
      seqnumb = sequence.getSequenceNumber().longValue();
      HttpGet getdiff = new HttpGet(
       "http://planet.openstreetmap.org/replication/day/000/000/"
       +seqnumb
       +".osc.gz");
      CloseableHttpResponse responsediff = httpclientdiff.execute(getdiff);
      GZIPInputStream gzip = new GZIPInputStream(
       responsediff.getEntity().getContent());
      spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      sp = spf.newSAXParser();
      xmlrdiffs = sp.getXMLReader();
      xmlrdiffs.setContentHandler(manipsax);
      xmlrdiffs.parse(
       new InputSource(gzip));
      //
      gzip.close();
      responsediff.close();
      //
      f_entités.getTransaction().commit();
      /* */
      //
      // Fabriquer un rapport
      /* */
      rap = new Rapport();
      rap.setDiff(sequence);
      rap.setBonsDébutants(new ArrayList<Contributeur>());
//      Query quer = f_entités.createNamedQuery("tous les contributeurs");
//      Date cejour = new Date();
      GregorianCalendar datemédiane = new GregorianCalendar();
      datemédiane.setTime(sequence.getTimestamp());
      datemédiane.add(Calendar.DAY_OF_MONTH, -1);
      CloseableHttpClient httpclient = HttpClients.createDefault();
//      for (Object resu : quer.getResultList())
      f_entités.getTransaction().begin();
      for (Contributeur buteur : f_tities.contributeurs())
      {
//        Contributeur buteur;
        ArrayList<Changeset> après;
        XMLReader xmlr;
        ManipUserSAX usersax;
//        int nbchanges;
        HttpGet getbuteur;
        CloseableHttpResponse resbuteur;
        InputStream isbuteur;

//        buteur = (Contributeur)resu;
        getbuteur = new HttpGet(
         "http://api.openstreetmap.org/api/0.6/user/"+buteur.getUID());
        resbuteur = httpclient.execute(getbuteur);
        xmlr = sp.getXMLReader();
        usersax = new ManipUserSAX();
        xmlr.setContentHandler(usersax);
        isbuteur = resbuteur.getEntity().getContent();
        xmlr.parse(new InputSource(isbuteur));
        isbuteur.close();
        resbuteur.close();
        après = new ArrayList<>();
        for (Changeset change : buteur.getChangesets())
          if (change.getChgTimestamp().after(datemédiane.getTime()))
            après.add(change);
        if (
         ((usersax.nbChangeSets() - après.size() )< 10)  
         &&  (usersax.nbChangeSets() >= 10))
        {
//          System.out.println(
//           buteur.getUID()
//           +" - "
//           +usersax.displayName()
//           +" - "
//           +"http://www.openstreetmap.org/user/"
//           +usersax.displayName());
          buteur.setPseudo(usersax.displayName());
          for (Changeset change : buteur.getChangesets())
          {
              HttpGet getnomi;
              CloseableHttpResponse respnomi;
              InputStream isnomi;
              BufferedReader isrnomi;
              HttpGet getapi;
              CloseableHttpResponse respapi;
              InputStream isapi;
              BufferedReader israpi;
              ManipChangesetSAX changesax;
              ManipNominatimSAX nomisax;

              getapi = new HttpGet(
               "http://api.openstreetmap.org/api/0.6/changeset/"
               +change.getIdSet());
              respapi = httpclient.execute(getapi);
              isapi = respapi.getEntity().getContent();
        xmlr = sp.getXMLReader();
        changesax = new ManipChangesetSAX();
        xmlr.setContentHandler(changesax);
        xmlr.parse(new InputSource(isapi));
        isapi.close();
        respapi.close();
        change.setMaxLat(changesax.t_maxLat);
        change.setMaxLon(changesax.t_maxLon);
        change.setMinLat(changesax.t_minLat);
        change.setMinLon(changesax.t_minLon);
              getnomi = new HttpGet(
               "http://nominatim.openstreetmap.org/reverse?format=xml&lat="
               +change.getMinLat()
               +"&lon="
               +change.getMaxLon());
              respnomi = httpclient.execute(getnomi);
              isnomi = respnomi.getEntity().getContent();
//              isrnomi = new BufferedReader(new InputStreamReader(isnomi));
//              String ligne = isrnomi.readLine();
//              while (ligne != null)
//              {
//                System.out.println(ligne);
//                ligne = isrnomi.readLine();
//              }
//              isrnomi.close();
        xmlr = sp.getXMLReader();
        nomisax = new ManipNominatimSAX();
        xmlr.setContentHandler(nomisax);
        xmlr.parse(new InputSource(isnomi));
              isnomi.close();
              respnomi.close();
              change.setChgState(nomisax.t_chgState);
              change.setCountry(nomisax.t_country);
              change.setCountryCode(nomisax.t_countryCode);
              change.setCounty(nomisax.t_county);
           }
          rap.getBonsDébutants().add(buteur);
        }
      }
      f_entités.persist(rap);
      f_entités.getTransaction().commit();
      System.out.println("Merci c'est tout.");
  }
  
  private static class ManipChangesetSAX extends 
   org.xml.sax.helpers.DefaultHandler
  {
    String t_minLat;
    String t_maxLat;
    String t_minLon;
    String t_maxLon;
    
    @Override
    public void startElement(
     String uri, String localName, String qName, Attributes attributes) throws 
     SAXException
    {
        switch (localName)
        {
          case "changeset":
            t_minLat = attributes.getValue("min_lat");
            t_maxLat = attributes.getValue("max_lat");
            t_minLon = attributes.getValue("min_lon");
            t_maxLon = attributes.getValue("max_lon");
            break;
          default:
            ;
        }
    } 
  }  
  
  private static class ManipNominatimSAX extends 
   org.xml.sax.helpers.DefaultHandler
  {
    private String t_countryCode;
    private String t_country;
    private String t_chgState;
    private String t_county;
    private String t_élément;
  
    @Override
    public void startElement(
     String uri, String localName, String qName, Attributes attributes) throws 
     SAXException
    {
      t_élément = localName;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
      switch (t_élément)
      {
        case "country_code":
        case "country":
        case "state":
        case "county":
          String val;
          
          val = new String(Arrays.copyOfRange(ch, start, start + length));
          switch (t_élément)
          {
            case "country_code":
              t_countryCode = val;
              break;
            case "country":
              t_country = val;
              break;
            case "state":
              t_chgState = val;
              break;
            case "county" :
              t_county = val;
              break;
          }
          break;
      }
    }
  }
}
