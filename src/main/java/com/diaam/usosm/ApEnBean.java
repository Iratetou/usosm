/*
 */
package com.diaam.usosm;

import com.diaam.usosm.edi.entities.Changeset;
import com.diaam.usosm.edi.entities.Contributeur;
import com.diaam.usosm.edi.entities.Rapport;
import com.diaam.usosm.edi.entities.Tities;
import com.google.common.base.Strings;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author herve
 */
public final class ApEnBean
{
  private Tities m_tities;
  
  ApEnBean(Tities tities)
  {
    m_tities = tities;
  }
  
  public List<Rapport> getRapports()
  {
    return m_tities.rapports();
  }
  
  public Map<Long, String> getPlacesDeChangement()
  {
    return new java.util.AbstractMap<Long, String>() 
    {
      @Override
      public Set<Map.Entry<Long, String>> entrySet()
      {
        HashSet<Map.Entry<Long, String>> entries;
        
        entries = new HashSet<>();
        for (Contributeur trib : m_tities.contributeurs())
        {
          HashMap<String, String> paysrégions;
          StringBuilder sb;
          
          paysrégions = new HashMap<>();
          for (Changeset chg : trib.getChangesets())
          {
            String pays;
            
            pays = chg.getCountry();
            if (pays != null)
            {
              String régions;
              String state;

              pays = pays + " (" + chg.getCountryCode() + ')';
              régions = paysrégions.get(pays);
              if (régions == null)
                régions = "";
              state = chg.getChgState();
              if (state != null)
                if (!régions.contains(state))
                {
                  if (!régions.isEmpty())
                    régions = régions + ", ";
                  régions = régions + state;
                  paysrégions.put(pays, régions);
                }
            }
          }
          sb = new StringBuilder();
          for (String pays : paysrégions.keySet())
          {
            if (sb.length() != 0)
              sb.append(" ; ");
            sb.append(pays);
            if (!Strings.isNullOrEmpty(paysrégions.get(pays)))
            {
              sb.append(" : ");
              sb.append(paysrégions.get(pays));
            }
          }
          entries.add(
           new AbstractMap.SimpleEntry<>(trib.getUID(), sb.toString()));
        }
        // ouf !
        return entries;
      }
    };
  }
}
