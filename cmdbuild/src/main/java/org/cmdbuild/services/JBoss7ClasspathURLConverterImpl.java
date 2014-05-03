package org.cmdbuild.services;

import org.apache.log4j.Logger;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

/**
 * Supports the special vfs* URLs used by class loaders of JBoss 7.0.2.
 *
 * @author Benjamin Bentmann
 * @author <a href="mailto:simon.dierl@cs.tu-dortmund.de">Simon Dierl</a>
 * @author <a
 *         href="http://wiki.apache.org/tapestry/HowToRunTapestry5OnJBoss6Dot1"
 *         >Tapestry Wiki</a>
 * @author Johannes Neubauer
 * @see <a href="https://issues.apache.org/jira/browse/TAP5-576">TAP5-576</a>
 */
public class JBoss7ClasspathURLConverterImpl{
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(JBoss7ClasspathURLConverterImpl.class);


    public static URL convert(URL url) {
        if ((url != null) && url.getProtocol().startsWith("vfs")) {
            try {
                final URL realURL;
                final String urlString = url.toString();
                // If the virtual URL involves a JAR file,
                // we have to figure out its physical URL ourselves because
                // in JBoss 7.0.2 the JAR files exploded into the VFS are empty
                // (see https://issues.jboss.org/browse/JBAS-8786).
                // Our workaround is that they are available, unexploded,
                // within the otherwise exploded WAR file.
                if (urlString.contains(".jar")) {
                    // An example URL:
                    // "vfs:/devel/jboss-6.1.0.Final/server/default/deploy/myapp.ear/myapp.war/WEB-INF/\
                    // lib/tapestry-core-5.2.6.jar/org/apache/tapestry5/corelib/components/"
                    // Break the URL into its WAR part, the JAR part,
                    // and the Java package part.
                    final int warPartEnd = urlString.indexOf(".war") + 4;
                    final String warPart = urlString.substring(0, warPartEnd);
                    final int jarPartEnd = urlString.indexOf(".jar") + 4;
                    final String jarPart = urlString.substring(warPartEnd, jarPartEnd);
                    final String packagePart = urlString.substring(jarPartEnd);
                    // Ask the VFS where the exploded WAR is.
                    final URL warURL = new URL(warPart);
                    final URLConnection warConnection = warURL.openConnection();
                    final VirtualFile jBossVirtualWarDir = (VirtualFile) warConnection.getContent();
                    final File physicalWarDir = jBossVirtualWarDir.getPhysicalFile();
                    final String physicalWarDirStr = physicalWarDir.toURI().toString();
                    // Return a "jar:" URL constructed from the parts
                    // eg.
                    // "jar:file:/devel/jboss-6.1.0.Final/server/default/tmp/vfs/automount40a6ed1db5eabeab/\
                    // myapp.war-43e2c3dfa858f4d2//WEB-INF/lib/tapestry-core-5.2.6.jar!/org/apache/tapestry5/corelib/components/".

//                    final String actualJarPath = "jar:" + physicalWarDirStr + jarPart + "!" + packagePart;
                    final String actualJarPath = physicalWarDirStr + jarPart;
                    return new URL(actualJarPath);
                }
                // Otherwise, ask the VFS what the physical URL is...
                else {
                    final URLConnection connection = url.openConnection();
                    final VirtualFile virtualFile = (VirtualFile) connection.getContent();
                    realURL = VFSUtils.getPhysicalURL(virtualFile);
                }
                return realURL;
            }
            catch (final Exception e) {
                LOGGER.fatal("Unable to convert URL " + url, e);
            }
        }
        return url;
    }
}
