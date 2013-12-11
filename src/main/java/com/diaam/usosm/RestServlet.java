/*
 */
package com.diaam.usosm;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.glassfish.jersey.servlet.WebConfig;

/**
 *
 * @author herve
 */
//@WebServlet(name = "RestServlet", urlPatterns =
//{
//  "/RestServlet"
//})
public class RestServlet extends org.glassfish.jersey.servlet.ServletContainer
{
  @Override
  public void init() throws ServletException
  {
//System.out.println("init")    ;
    super.init(); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException
  {
System.out.println("init filter")    ;
    super.init(filterConfig); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void init(ServletConfig config) throws ServletException
  {
//System.out.println(">init config="+config)    ;
    super.init(config);
//    getConfiguration().register(new BinderInjection());
//System.out.println("<init config "+getConfiguration().getApplication())    ;
  }

  @Override
  protected void init(WebConfig webConfig) throws ServletException
  {
//System.out.println("init web")    ;
    super.init(webConfig); //To change body of generated methods, choose Tools | Templates.
  }
  
  
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
      out.println("<title>Servlet RestServlet</title>");      
      out.println("</head>");
      out.println("<body>");
      out.println("<h1>Servlet RestServlet at " + request.getContextPath() + "</h1>");
      out.println("</body>");
      out.println("</html>");
    }
    finally
    {      
      out.close();
    }
  }
  
  private class BinderInjection extends org.glassfish.hk2.utilities.binding.AbstractBinder
  {
    @Override
    protected void configure()
    {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } 
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP
   * <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP
   * <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException
  {
    processRequest(request, response);
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
