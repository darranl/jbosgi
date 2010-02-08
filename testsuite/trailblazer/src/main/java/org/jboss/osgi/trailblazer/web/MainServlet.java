/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.trailblazer.web;

//$Id$

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;


/**
 * A servlet that gets registered with the {@link HttpService} to 
 * render the Trailblazer portal 
 * 
 * @author thomas.diesler@jboss.com
 * @since 10-May-2009
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet
{
   private BundleContext context;

   public MainServlet(BundleContext context)
   {
      this.context = context;
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      PrintWriter out = res.getWriter();
      
      String path = req.getPathInfo();
      if (path == null)
         path = "/";

      renderHeader(req, res);
      
      renderFooter(req, res);
      
      out.close();
   }
   
   private void renderHeader(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();
      
      out.println("<html>");
      out.println("<head>");
      out.println("<link rel='stylesheet' href='" + req.getServletPath() + "/style/osgishop.css' type='text/css'>");
      out.println("</head>");
      out.println("<body>");
      
      out.println("<a href='" + req.getServletPath() + "'>home</a>");
      out.println("<a href='" + req.getServletPath() + "/cart'>cart</a>");
      out.println("<p/>");
      out.println("<table><tr valign='top'><td width='400'>");
   }

   private void renderFooter(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();

      out.println("</td><td>");
      
      renderNotes(req, res);
      
      out.println("</td></tr>");
      out.println("</table>");
      out.println("</body>");
      out.println("</html>");
   }

   private void renderNotes(HttpServletRequest req, HttpServletResponse res) throws IOException
   {
      PrintWriter out = res.getWriter();
      
      String path = req.getPathInfo();
      if (path == null)
         path = "/";
      
      if (path.equals("/"))
         path = "/home";
      
      out.println("<div class='notes'>");
      
      String notesPath = "notes" + path;
      
      if (path.equals("/list"))
      {
         String paramShop = req.getParameter("shop");
         if (paramShop != null)
            notesPath += "-" + paramShop;
      }
      
      notesPath = notesPath.toLowerCase() + ".html";
      
      URL htmlRes = context.getBundle().getResource(notesPath);
      if (htmlRes != null)
      {
         BufferedReader br = new BufferedReader(new InputStreamReader(htmlRes.openStream()));
         String line = br.readLine();
         while (line != null)
         {
            out.println(line);
            line = br.readLine();
         }
      }
      else
      {
         out.println("Cannot find: " + notesPath);
      }
      out.println("</div>");
   }
}
