package org.enhydra.shark.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.servlet.GenericServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;
import org.enhydra.shark.Shark;

public class SharkInitServlet
        extends HttpServlet
{
    public static final String ENVIRONMENT_TYPE = "EnvironmentType";

    public SharkInitServlet() {}

    public void init()
            throws ServletException
    {
        System.out.println("++++++++++++++++++++++++++++++++++++++ init - start");
        String environmentType= "";
        UserTransaction t = null;
        try
        {
            Properties props = new Properties();
            File sc = null;
            try
            {
                File orig = new File(getServletContext().getRealPath("/"));
                sc = new File(orig.getCanonicalPath() + "/conf/Shark.conf");
                if (!sc.exists())
                {
                    File pjboss = orig.getParentFile().getParentFile().getParentFile().getParentFile();
                    sc = new File(pjboss.getCanonicalPath() + "/sharkFiles/conf/Shark.conf");
                }
            }
            catch (Throwable ex)
            {
                ex.printStackTrace();
            }
            InputStream is = null;
            try
            {
                if ((sc != null) && (sc.exists()))
                {
                    is = new FileInputStream(sc.getCanonicalPath());
                    System.out.println("Shark will be configured from file " + sc.getCanonicalPath());
                }
                else
                {
                    URL u = SharkInitServlet.class.getClassLoader().getResource("Shark.conf");
                    System.out.println("Shark will be configured from the resource file " + u);
                    URLConnection urlConnection = u.openConnection();
                    is = urlConnection.getInputStream();
                }
                props.load(is);
                try
                {
                    is.close();
                }
                catch (Exception ex) {}
                environmentType = (String)props.get("EnvironmentType");
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    is.close();
                }
                catch (Exception ex) {}
            }

            System.out.println("environmentType= " + environmentType);
            if ((environmentType != null) && (environmentType.equalsIgnoreCase("tomcat"))) {
                t = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
            } else {
                t = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
            }
            System.out.println("    t= " + t);

            t.begin();

            System.out.println("\tpre configure . . . . . .");
            Shark.configure(props);

            System.out.println("\tpre getConnection . . . . . .");
            Shark.getInstance().getSharkConnection();

            System.out.println("\tpre commit . . . . . .");
            t.commit();

            System.out.println("++++++++++++++++++++++++++++++++++++++ init - end");
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            try
            {
                if (t.getStatus() != 6) {
                    t.rollback();
                }
            }
            catch (Exception e)
            {
                System.out.println("++++++++++++++++++++++++++++++++++++++ init - error!!!");
                e.printStackTrace();
            }
            throw new Error("Unable to put shark into the running state!");
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException
    {
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println("*****************************************************************");
        out.println("***** This servlet is used for Shark Engine initialization! *****");
        out.println("*****************************************************************");

        System.out.println("++++++++++++++++++++++++++++++++++++++ doGet");
    }
}
