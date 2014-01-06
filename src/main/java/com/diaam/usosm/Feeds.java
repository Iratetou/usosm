/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diaam.usosm;

import com.diaam.usosm.edi.entities.Contributeur;
import com.diaam.usosm.edi.entities.Rapport;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author herve
 */
@WebServlet(name = "Feeds", urlPatterns =
{
  "/feeds/*"
})
public class Feeds extends HttpServlet
{

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
    try
    {
      /* TODO output your page here. You may use following sample code. */
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Servlet Feeds</title>");      
      out.println("</head>");
      out.println("<body>");
      out.println("<h1>Servlet Feeds at " + request.getContextPath() + "</h1>");
      out.println("</body>");
      out.println("</html>");
    }
    finally
    {      
      out.close();
    }
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException
  {
    try
    {
      SyndFeedImpl synd;
      SyndFeedOutput feed;
      ArrayList<SyndEntryImpl> entries;
      ApEnBean ap;
      Map<Long, String> places;

      synd = new SyndFeedImpl();
      synd.setFeedType("atom_1.0");
      synd.setAuthor("usosm");
      synd.setTitle("usosm : débutants (>10 diffs) sur osm");
      entries = new ArrayList<>();
      ap = (ApEnBean) request.getServletContext().getAttribute("ap");
      places = ap.getPlacesDeChangement();
      for (Rapport rap : ap.getRapports())
      {
        for (Contributeur trib : rap.getBonsDébutants())
        {
          SyndEntryImpl entri;
          SyndContentImpl contdiff;

          entri = new SyndEntryImpl();
          entri.setTitle(trib.getPseudo());
          entri.setPublishedDate(rap.getDiff().getTimestamp());
          entri.setLink("http://www.openstreetmap.org/user/"+trib.getPseudo());
          entri.setAuthor("usosm");
          contdiff = new SyndContentImpl();
          contdiff.setValue(
           "Du diff journalier "
           +rap.getDiff().getSequenceNumber()
           +"pour les lieux suivants : "
           +places.get(trib.getUID()));
          entri.setDescription(contdiff);
          entries.add(entri);
        }
      }
      synd.setEntries(entries);
      response.setContentType("text/html;charset=UTF-8");
      feed = new SyndFeedOutput();
      feed.output(synd, response.getWriter());
    }
    catch (FeedException exc)
    {
      log("", exc);
      response.sendError(
       HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "erreur feed");
    }
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "Short description";
  }// </editor-fold>
}
