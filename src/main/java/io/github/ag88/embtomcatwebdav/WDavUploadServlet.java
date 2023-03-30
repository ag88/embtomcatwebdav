package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.WebResource;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.util.ServerInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.security.Escape;

public class WDavUploadServlet extends WebdavServlet {
	
	Log log = LogFactory.getLog(WDavUploadServlet.class);

	public WDavUploadServlet() {
		super();
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		log.info(request.getContentType());
		BufferedReader reader = request.getReader();
		StringBuilder sb = new StringBuilder(1024);
		String line = null;
		while((line = reader.readLine())!= null) {
			sb.append(line);
			sb.append(System.lineSeparator());
		}
		log.info(sb.toString());
		doGet(request, response);
	}

	
	@Override
	protected InputStream renderHtml(HttpServletRequest request, String contextPath, WebResource resource,
			String encoding) throws IOException {
        // Prepare a writer to a buffered area
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        PrintWriter writer = new PrintWriter(osWriter);

        StringBuilder sb = new StringBuilder();

        String directoryWebappPath = resource.getWebappPath();
        WebResource[] entries = resources.listResources(directoryWebappPath);

        // rewriteUrl(contextPath) is expensive. cache result for later reuse
        String rewrittenContextPath =  rewriteUrl(contextPath);

        // Render the page header
        sb.append("<!doctype html><html>\r\n");
        /* TODO Activate this as soon as we use smClient with the request locales
        sb.append("<!doctype html><html lang=\"");
        sb.append(smClient.getLocale().getLanguage()).append("\">\r\n");
        */
        sb.append("<head>\r\n");
        sb.append("<title>");
        sb.append(sm.getString("directory.title", directoryWebappPath));
        sb.append("</title>\r\n");
        sb.append("<style>");
        sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
        sb.append("</style> ");
        sb.append("</head>\r\n");
        sb.append("<body>");
        sb.append("<h1>");
        sb.append(sm.getString("directory.title", directoryWebappPath));

        // Render the link to our parent (if required)
        String parentDirectory = directoryWebappPath;
        if (parentDirectory.endsWith("/")) {
            parentDirectory =
                parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {
            String parent = directoryWebappPath.substring(0, slash);
            sb.append(" - <a href=\"");
            sb.append(rewrittenContextPath);
            if (parent.equals("")) {
                parent = "/";
            }
            sb.append(rewriteUrl(parent));
            if (!parent.endsWith("/")) {
                sb.append('/');
            }
            sb.append("\">");
            sb.append("<b>");
            sb.append(sm.getString("directory.parent", parent));
            sb.append("</b>");
            sb.append("</a>");
        }

        sb.append("</h1>");
        sb.append("<hr class=\"line\">");

        sb.append("<table width=\"100%\" cellspacing=\"0\"" +
                     " cellpadding=\"5\" align=\"center\">\r\n");

        /*
        SortManager.Order order;
        if(sortListings && null != request) {
            order = sortManager.getOrder(request.getQueryString());
        } else {
            order = null;
        }
        */
        
        // Render the column headings
        sb.append("<tr>\r\n");
        sb.append("<td align=\"left\"><font size=\"+1\"><strong>");
        if(sortListings && null != request) {        	
            //sb.append("<a href=\"?C=N;O=");
            //sb.append(getOrderChar(order, 'N'));
        	sb.append("<a href=\"?C=N;O=N");
            sb.append("\">");
            sb.append(sm.getString("directory.filename"));
            sb.append("</a>");
        } else {
            sb.append(sm.getString("directory.filename"));
        }
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"center\"><font size=\"+1\"><strong>");
        if(sortListings && null != request) {
            //sb.append("<a href=\"?C=S;O=");
            //sb.append(getOrderChar(order, 'S'));
        	sb.append("<a href=\"?C=S;O=S");
            sb.append("\">");
            sb.append(sm.getString("directory.size"));
            sb.append("</a>");
        } else {
            sb.append(sm.getString("directory.size"));
        }
        sb.append("</strong></font></td>\r\n");
        sb.append("<td align=\"right\"><font size=\"+1\"><strong>");
        if(sortListings && null != request) {
            //sb.append("<a href=\"?C=M;O=");
            //sb.append(getOrderChar(order, 'M'));
        	sb.append("<a href=\"?C=M;O=M");
            sb.append("\">");
            sb.append(sm.getString("directory.lastModified"));
            sb.append("</a>");
        } else {
            sb.append(sm.getString("directory.lastModified"));
        }
        sb.append("</strong></font></td>\r\n");
        sb.append("</tr>");

        /*
        if(null != sortManager && null != request) {
            sortManager.sort(entries, request.getQueryString());
        }
        */

        boolean shade = false;
        for (WebResource childResource : entries) {
            String filename = childResource.getName();
            if (filename.equalsIgnoreCase("WEB-INF") ||
                filename.equalsIgnoreCase("META-INF")) {
                continue;
            }

            if (!childResource.exists()) {
                continue;
            }

            sb.append("<tr");
            if (shade) {
                sb.append(" bgcolor=\"#eeeeee\"");
            }
            sb.append(">\r\n");
            shade = !shade;

            sb.append("<td align=\"left\">&nbsp;&nbsp;\r\n");
            sb.append("<a href=\"");
            sb.append(rewrittenContextPath);
            sb.append(rewriteUrl(childResource.getWebappPath()));
            if (childResource.isDirectory()) {
                sb.append('/');
            }
            sb.append("\"><tt>");
            sb.append(Escape.htmlElementContent(filename));
            if (childResource.isDirectory()) {
                sb.append('/');
            }
            sb.append("</tt></a></td>\r\n");

            sb.append("<td align=\"right\"><tt>");
            if (childResource.isDirectory()) {
                sb.append("&nbsp;");
            } else {
                sb.append(renderSize(childResource.getContentLength()));
            }
            sb.append("</tt></td>\r\n");

            sb.append("<td align=\"right\"><tt>");
            sb.append(childResource.getLastModifiedHttp());
            sb.append("</tt></td>\r\n");

            sb.append("</tr>\r\n");
        }

        // Render the page footer
        sb.append("</table>\r\n");

        sb.append("<hr class=\"line\">");

        String readme = getReadme(resource, encoding);
        if (readme!=null) {
            sb.append(readme);
            sb.append("<hr class=\"line\">");
        }

        String prefix = request.getServletPath();
        sb.append("<h2>Upload File</h2>");
        sb.append("server path: " + prefix + directoryWebappPath + "<br><br>\n");
        sb.append("<form action=\"" + prefix  + directoryWebappPath + "\"" +
        		" enctype=\"multipart/form-data\" method=post>");        
        sb.append("<label for=\"files\">Select file:</label>\n");
        sb.append("<input type=\"file\" id=\"files\" name=\"files\" single><br><br>\n");        
        sb.append("<input type=\"submit\" value=\"upload\">\n");
        sb.append("</form>\n");

        if (showServerInfo) {
            sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>");
        }
        
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");

        // Return an input stream to the underlying bytes
        writer.write(sb.toString());
        writer.flush();
        return new ByteArrayInputStream(stream.toByteArray());
	}
}
