/*
 */
package integration.com.diaam.usosm;

import com.diaam.usosm.EnGarde;
import com.diaam.usosm.edi.ContribsOSM;
import com.diaam.usosm.edi.entities.Changeset;
import com.diaam.usosm.edi.entities.Contributeur;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import test.com.diaam.Integration;
import test.com.diaam.Unitaire;

/**
 *
 * @author herve
 */
public class JPAIT
{
  
  public JPAIT()
  {
  }
  
  @BeforeClass
  public static void setUpClass()
  {
  }
  
  @AfterClass
  public static void tearDownClass()
  {
  }
  
  @Before
  public void setUp()
  {
  }
  
  @After
  public void tearDown()
  {
  }
  // TODO add test methods here.
  // The methods must be annotated with annotation @Test. For example:
  //
  // @Test
  // public void hello() {}
  
  @Test
  @Integration
  public void écoutePlusieursFois()
  {
    EnGarde.EcouteurDesDiffs écou;
    HashMap<String, String> propsjpa;
    
    propsjpa = new HashMap<>();
    propsjpa.put("javax.persistence.jdbc.url", "jdbc:h2:~/contribsosmh3");
    écou = new EnGarde.EcouteurDesDiffs(new ContribsOSM(propsjpa));
    écou.setFluxDeDiff(new FluxTest());
    écou.run();
    écou.run();
  }
  
  private static class FluxTest implements com.diaam.usosm.edi.CommeDesFluxDeDiffs
  {
    @Override
    public Reader state() throws IOException
    {
      return new StringReader(
       "#Sat Dec 14 00:05:51 UTC 2013\n"
       + "sequenceNumber=458\n"
       + "timestamp=2013-12-14T00\\:00\\:00Z");
    }

    @Override
    public Reader diffDay(long séquence) throws IOException
    {
      return new StringReader("<osm/>");
    }

    @Override
    public Reader user(Contributeur contributeur) throws IOException
    {
      return new StringReader("");
    }

    @Override
    public Reader changeset(Changeset change) throws IOException
    {
      return new StringReader("");
    }

    @Override
    public Reader lieu(Changeset change) throws IOException
    {
      return new StringReader("");
    } 

    @Override
    public void close() throws IOException
    {
    }
  }
}