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
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.persistence.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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

  // N'utiliser ces statics que pour le rest jersey
  // ... c'est à dire ne jamais utiliser ces statics.
  private static final EntityManagerFactory F_fabriqueDesEntités;
  public static final EntityManager F_entités;
  //
  
  static
  {
    F_fabriqueDesEntités = Persistence.createEntityManagerFactory(
     "contribsosm");
    F_entités = F_fabriqueDesEntités.createEntityManager();
  }
  
  public final Tities f_tities;
  private final EntityManagerFactory f_fabriqueDesEntités;
  public final EntityManager f_entités;

  public ContribsOSM()
  {
    f_tities = new Tities(F_entités);
    f_fabriqueDesEntités = F_fabriqueDesEntités;
    f_entités = F_entités;
  }
  
  /**
   * Ne pas utiliser. Suite à situation que j'arrive pas à démerder avec Jersey.
   * Pour les tests surtout, et annule les fonctions Rest ;
   * je n'ai pas trouvé comment utiliser jersey sans variables 
   * static, ce qui bousille mon organisation.
   * 
   * @param mapJPA 
   */
  public ContribsOSM(Map mapJPA)
  {
    F_entités.close();
    F_fabriqueDesEntités.close();
    f_fabriqueDesEntités = Persistence.createEntityManagerFactory(
     "contribsosm", mapJPA);
    f_entités = f_fabriqueDesEntités.createEntityManager();
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
            String struid;

            strchange = attributes.getValue("changeset");
            struid = attributes.getValue("uid");
            mêmequeprécédent = 
             struid.equals(t_précédentUID) 
             && strchange.equals(t_précédentChangeset);
            if (!mêmequeprécédent)
            {
              if (struid.equals(t_précédentUID))
                ajouteChangeSetSiAbsent(t_précédentContributeur, attributes);
              else
              {
                Contributeur buteur;

                buteur = contributeurs.get(Long.parseLong(struid));
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
        m_entities.persist(change);
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
  
  public void analyseDayDiffEtFaitLeRapport(
   Diff sequence, CommeDesFluxDeDiffs flux) throws 
   IOException, ParserConfigurationException, SAXException
  {
    ManipSAX manipsax;
    long seqnumb;
//    CloseableHttpClient httpclientdiff;
    SAXParserFactory spf;
    SAXParser sp;
    XMLReader xmlrdiffs;
    Rapport rap;
    Reader readday;
    
      manipsax = new ManipSAX(f_entités);
//      httpclientdiff = HttpClients.createDefault();
////      HttpGet getdiffstate = new HttpGet(
////       "http://planet.openstreetmap.org/replication/day/000/000/"
////       +sequence.getSequenceNumber()
////       +".state.txt");
////      CloseableHttpResponse responsestate = httpclientdiff.execute(
////       getdiffstate);
////      InputStreamReader isr = new InputStreamReader(
////       responsestate.getEntity().getContent());
////      BufferedReader br = new BufferedReader(isr);
////      br.readLine();
////      String lnseq = br.readLine();
////      if (
////       Long.valueOf(lnseq.substring(lnseq.indexOf('=')+1)).longValue() 
////       != sequence.getSequenceNumber().longValue())
////        throw new IllegalStateException();
////      String lntimestamp = br.readLine();
////      String strtimestamp = lntimestamp.substring(
////       lntimestamp.indexOf('=')+1).replaceAll("\\\\", "");
////      Date dttimestamp = manipsax.m_dtFormat.parse(strtimestamp);
////      diff.setTimestamp(dttimestamp);
      seqnumb = sequence.getSequenceNumber().longValue();
//      HttpGet getdiff = new HttpGet(
//       "http://planet.openstreetmap.org/replication/day/000/000/"
//       +seqnumb
//       +".osc.gz");
//      CloseableHttpResponse responsediff = httpclientdiff.execute(getdiff);
//      GZIPInputStream gzip = new GZIPInputStream(
//       responsediff.getEntity().getContent());
      readday = flux.diffDay(seqnumb);
      spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      sp = spf.newSAXParser();
      xmlrdiffs = sp.getXMLReader();
      xmlrdiffs.setContentHandler(manipsax);
      xmlrdiffs.parse(
//       new InputSource(gzip));
       new InputSource(readday));
      //
//      gzip.close();
//      responsediff.close();
      readday.close();
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
//      CloseableHttpClient httpclient = HttpClients.createDefault();
//      for (Object resu : quer.getResultList())
      f_entités.getTransaction().begin();
      for (Contributeur buteur : f_tities.contributeurs())
      {
//        Contributeur buteur;
        ArrayList<Changeset> après;
        XMLReader xmlr;
        ManipUserSAX usersax;
        Reader rbuteur;

        xmlr = sp.getXMLReader();
        usersax = new ManipUserSAX();
        xmlr.setContentHandler(usersax);
        rbuteur = flux.user(buteur);
        xmlr.parse(new InputSource(rbuteur));
        après = new ArrayList<>();
        rbuteur.close();
        for (Changeset change : buteur.getChangesets())
          if (change.getChgTimestamp().after(datemédiane.getTime()))
            après.add(change);
        if (
         ((usersax.nbChangeSets() - après.size() )< 10)  
         &&  (usersax.nbChangeSets() >= 10))
        {
          buteur.setPseudo(usersax.displayName());
          for (Changeset change : buteur.getChangesets())
          {
              ManipChangesetSAX changesax;
              ManipNominatimSAX nomisax;
              Reader rchange;
              Reader rnomi;

              rchange = flux.changeset(change);
        xmlr = sp.getXMLReader();
        changesax = new ManipChangesetSAX();
        xmlr.setContentHandler(changesax);
        xmlr.parse(new InputSource(rchange));
        rchange.close();
        change.setMaxLat(changesax.t_maxLat);
        change.setMaxLon(changesax.t_maxLon);
        change.setMinLat(changesax.t_minLat);
        change.setMinLon(changesax.t_minLon);
        rnomi = flux.lieu(change);
        xmlr = sp.getXMLReader();
        nomisax = new ManipNominatimSAX();
        xmlr.setContentHandler(nomisax);
//        xmlr.parse(new InputSource(isnomi));
//              isnomi.close();
//              respnomi.close();
        xmlr.parse(new InputSource(rnomi));
        rnomi.close();
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
