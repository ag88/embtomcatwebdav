/*
 Copyright 2023 Andrew Goh http://github.com/ag88
 
 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.WebResource;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.util.ServerInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.apache.tomcat.util.security.Escape;

import io.github.ag88.embtomcatwebdav.opt.Opt;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.util.DefFilePathNameValidator;
import io.github.ag88.embtomcatwebdav.util.FilePathNameValidator;
import io.github.ag88.embtomcatwebdav.util.SortManager;

/**
 * Class WDavUploadServlet - the Upload Servlet.<p>
 * 
 * This Upload servlet is a derived custom implementation from Apache Tomcat's {@link WebdavServlet}
 * and {@link DefaultServlet}.<p>
 * 
 * It works like the {@link DefaultServlet}, but that it adds an interface for form based file upload.
 *  
 * 
 */
public class WDavUploadServlet extends WebdavServlet {
	
	/** The log. */
	Log log = LogFactory.getLog(WDavUploadServlet.class);
		
	/** The quiet. */
	boolean quiet;
	
	/**
	 * Instantiates a new w dav upload servlet.
	 */
	public WDavUploadServlet() {
		super();
	}
	
	/**
	 * Servlet init().
	 *
	 * @param config the config
	 * @throws ServletException the servlet exception
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Opt opt = OptFactory.getInstance().getOpt("quiet");
		if(opt != null)
			quiet = ((Boolean) opt.getValue()).booleanValue();
		else
			quiet = false;
	}
	
	
	/**
	 * Servlet doPost().
	 * 
	 * This hanndles the actual file upload as a Servlet.
	 *
	 * @param request the request
	 * @param response the response
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		log.debug(request.getContentType());
		
		boolean overwrite = false;		

		HttpSession session = request.getSession();		
		if (session.isNew()) {
			// String encodedURL =
			// response.encodeRedirectURL(request.getRequestURL().toString());
			// response.sendRedirect(encodedURL);
			if(!quiet)
			log.warn("session is new ".concat(session.getId()));
			
			String referrer = request.getHeader("referer");
			if (request.getRequestURL().toString().equals(referrer)) {
				try {
					URI uri = new URI(request.getRequestURL().toString());
					uri = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(), uri.getPort(),
						"/res/nocookie.htm",null, null);				
					response.sendRedirect(uri.toURL().toString());
				} catch (URISyntaxException e) {
					log.error(e);
				}
			}			
			return;
		}
				
		if(request.getSession().getAttribute("overwrite") != null) 
			overwrite = (Boolean) request.getSession().getAttribute("overwrite");

		if (ServletFileUpload.isMultipartContent(request)) {

			// Identify the requested resource path
			String path = getRelativePath(request, true);
			if (path.length() == 0) {
				// Context root redirect
				doDirectoryRedirect(request, response);
				return;
			}

			WebResource resource = resources.getResource(path);
			//String prefix = request.getServletPath();
			//String pathinfo = request.getPathInfo();
			// sb.append("server path: " + prefix + directoryWebappPath + "<br><br>\n");
			// sb.append("request.pathinfo: " + request.getPathInfo() + "<br><br>\n");
			// sb.append("canonnical path: " + resource.getCanonicalPath() + "<br><br>\n");
			
			if (resource.exists() && resource.isDirectory()) {

				ArrayList<LogRecord> messages = new ArrayList<LogRecord>(4);
				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iterStream = upload.getItemIterator(request);
				while (iterStream.hasNext()) {
					FileItemStream item = iterStream.next();
					String name = item.getFieldName();
					String filename = item.getName();

					InputStream stream = item.openStream();
					if (!item.isFormField()) { // is file

						// validate filename
						FilePathNameValidator validator = new DefFilePathNameValidator();
						validator.setReadonly(false);
						if (!validator.isValidFilename(filename, messages)) {
							dolog(messages);
							continue;
						}

						// validate path, filename
						Path dirpath = null;
						try {
							String dir = resource.getCanonicalPath();
							dirpath = Paths.get(dir);
						} catch (Exception e) {
							String errmsg = errormsg(dirpath.toString(), filename, "Paths.get(dir)", e);
							LogRecord lr = new LogRecord(Level.SEVERE, errmsg);
							messages.add(lr);
							if (!quiet)
								log.error(errmsg);
							continue;
						}

						if (dirpath == null || !validator.isValidPathname(dirpath, filename, messages)) {
							if (dirpath == null) {
								LogRecord lr = new LogRecord(Level.SEVERE, "basepath is null or path is invalid");
								messages.add(lr);
								if (!quiet)
									log.error("basepath is null or path is invalid");
							}
							dolog(messages);
							continue;
						}

						if (Files.exists(dirpath.resolve(filename))) {
							if (!overwrite) {
								LogRecord lr = new LogRecord(Level.WARNING,
										String.format("file %s exists, not overwriting", filename));
								messages.add(lr);
								if (!quiet)
									log.warn(String.format("file %s exists, not overwriting",
											dirpath.resolve(filename)));
								continue;
							}
						}

						try {
							OutputStream target = Files.newOutputStream(dirpath.resolve(filename));
							byte[] buf = new byte[8192];
							int length;
							while ((length = stream.read(buf)) != -1) {
								target.write(buf, 0, length);
							}
							target.flush();
							target.close();
							stream.close();
						} catch (IOException e) {
							String msg = errormsg(dirpath.toString(), filename, "error writing file", e);
							messages.add(new LogRecord(Level.SEVERE, msg));
							log.error(msg);
							continue;
						}

						// messages.add(new LogRecord(Level.INFO, "name: ".concat(name)));
						messages.add(new LogRecord(Level.INFO, "uploaded filename: ".concat(filename)));
						if (!quiet)
							log.info("uploaded filename:".concat(filename));
					} else {
						String formFieldValue = Streams.asString(stream);
						if (!quiet) {
							messages.add(new LogRecord(Level.INFO, "field: ".concat(name)));
							messages.add(new LogRecord(Level.INFO, "value: ".concat(formFieldValue)));
							log.info(formFieldValue);
						}
					}
				}
				session.setAttribute("msgupload", messages);

				response.sendRedirect(request.getRequestURL().toString());
				// doGet(request, response);
	        } else {
	        	log.error(String.format("directory %s do not exist", resource.getCanonicalPath()));
	        	doGet(request, response);
	        }	        		        

		} else {
        	String overwrites = request.getParameter("overwrite");
        	if(overwrites != null) {
        		overwrite = Boolean.parseBoolean(overwrites);
        		session.setAttribute("overwrite", Boolean.valueOf(overwrite));
        	}  	        	
        	response.sendRedirect(request.getRequestURL().toString());
			//doGet(request, response);
		}
			
	}

	
    /**
     * Do directory redirect.
     *
     * @param request the request
     * @param response the response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void doDirectoryRedirect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        StringBuilder location = new StringBuilder(request.getRequestURI());
        location.append('/');
        if (request.getQueryString() != null) {
            location.append('?');
            location.append(request.getQueryString());
        }
        // Avoid protocol relative redirects
        while (location.length() > 1 && location.charAt(1) == '/') {
            location.deleteCharAt(0);
        }
        response.sendRedirect(response.encodeRedirectURL(location.toString()));
    }
    
	/**
	 * Errormsg.
	 *
	 * @param basepath the basepath
	 * @param filename the filename
	 * @param message the message
	 * @param e the e
	 * @return the string
	 */
	private String errormsg(String basepath, String filename, String message, Exception e) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("invalid file path: ");
		sb.append(basepath);
		sb.append(", ");
		sb.append(filename);
		sb.append(System.lineSeparator());
		if(message != null) {
			sb.append(message);
			sb.append(System.lineSeparator());
		}
		sb.append(e.getMessage());
		sb.append(System.lineSeparator());
		sb.append(e.getStackTrace());
		return sb.toString();
	}
	
	/**
	 * Dolog.
	 *
	 * @param messages the messages
	 */
	private void dolog(List<LogRecord> messages) {
		for(LogRecord lr : messages) {
			if(lr.getLevel().equals(Level.SEVERE))
				log.error(lr.getMessage());
			else if (lr.getLevel().equals(Level.WARNING)) 
				log.warn(lr.getMessage());			
			else if (lr.getLevel().equals(Level.INFO))
				log.info(lr.getMessage());
		}
	}

    
	/**
	 * Render the directory list.
	 *
	 * This render the directory list as like {@link DefaultServlet}. 
	 * However, that this is substantiatially customized to add a file upload form.
	 * And use a responsive html layout rather than a table.
	 * 
	 * @param request the request
	 * @param contextPath the context path
	 * @param resource the resource
	 * @param encoding the encoding
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        sb.append("<link rel=\"stylesheet\" href=\"/res/style.css\">\n");
        sb.append("<script type=\"text/javascript\">\n");
        sb.append("function onupload() {\n" 
        		+ "  document.getElementById(\"upmsg\").style = \"visibility: visible; color: orange;\";\n"
        		+ "  document.getElementById(\"upmsg\").innerHTML = \"uploading...\";\n"        		
        		+ "  return true;\n"
        		+ "}\n");
        sb.append("</script>\r\n");
        sb.append("</head>\r\n");
        sb.append("<body>");
        sb.append("<h1>");        

        // breadcrumb at top
        
        //sb.append(sm.getString("directory.title", directoryWebappPath));
        sb.append("Directory Listing for : &nbsp; &nbsp;");
        
        // Render the link to our parent (if required)
        String parentDirectory = directoryWebappPath;
        if (parentDirectory.endsWith("/")) {
            parentDirectory =
                parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {
            String parent = directoryWebappPath.substring(0, slash);
            sb.append(" <a href=\"");
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
            //sb.append(sm.getString("directory.parent", parent));
            sb.append(parent);
            sb.append("</b>");
            sb.append("</a>");
            sb.append("&nbsp; &nbsp; / &nbsp;");
        }
       
        if(slash < directoryWebappPath.length())
        	sb.append(directoryWebappPath.substring(slash+1));
        else
        	sb.append(directoryWebappPath);
        

        sb.append("</h1>\n");
        sb.append("<hr class=\"line\">\n");
        sb.append("<p>\n");
        
        // directory listing
        SortManager sortmgr = new SortManager(true);

        SortManager.Order order;
        if(sortListings && null != request) {
            order = sortmgr.getOrder(request.getQueryString());
        } else {
            order = null;
        }
        
        // Render the column headings
        
        sb.append("<div class=\"dirlist\">\n");
        sb.append("<div class=\"column-heading\">\n");
        sb.append("<div class=\"col-2\">");
        if(sortListings && null != request) {        	
            sb.append("<a href=\"?C=N;O=");
            sb.append(getOrderChar(order, 'N'));
        	//sb.append("<a href=\"?C=N;O=N");
            sb.append("\">");
            sb.append(sm.getString("directory.filename"));
            sb.append("</a>");
        } else {
            sb.append(sm.getString("directory.filename"));
        }
        sb.append("</div>\n");
        sb.append("<div class=\"col-3\">");
        if(sortListings && null != request) {
            sb.append("<a href=\"?C=S;O=");
            sb.append(getOrderChar(order, 'S'));
        	//sb.append("<a href=\"?C=S;O=S");
            sb.append("\">");
            sb.append(sm.getString("directory.size"));
            sb.append("</a>");
        } else {
            sb.append(sm.getString("directory.size"));
        }
        sb.append("</div>\n");
        sb.append("<div class=\"col-4\">");
        if(sortListings && null != request) {
            sb.append("<a href=\"?C=M;O=");
            sb.append(getOrderChar(order, 'M'));
        	//sb.append("<a href=\"?C=M;O=M");
            sb.append("\">");
            sb.append(sm.getString("directory.lastModified"));
            sb.append("</a>");
        } else {
            sb.append(sm.getString("directory.lastModified"));
        }
        sb.append("</div>\n");
        sb.append("</div>\n");  //col-heading

        if(null != sortmgr && null != request) {
            sortmgr.sort(entries, request.getQueryString());
        }

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

            sb.append(String.format("<div class=\"%s\">\n", shade ? "row shade" : "row" ));

            shade = !shade;
            
            sb.append("<div class=\"col-2\">");
            sb.append("<a href=\"");
            sb.append(rewrittenContextPath);
            sb.append(rewriteUrl(childResource.getWebappPath()));
            if (childResource.isDirectory()) {
                sb.append('/');
            }
            sb.append("\">");
            sb.append(Escape.htmlElementContent(filename));
            if (childResource.isDirectory()) {
                sb.append('/');
            }
            sb.append("</a>");
            sb.append("</div>\n"); //col-2

            sb.append("<div class=\"col-3\">");
            if (childResource.isDirectory()) {
                sb.append("&nbsp;");
            } else {
                sb.append(renderSize(childResource.getContentLength()));
            }
            sb.append("</div>\n"); //col-3

            sb.append("<div class=\"col-4\">");
            sb.append(childResource.getLastModifiedHttp());
            sb.append("</div>\n"); //col-4

            sb.append("</div>\n"); //row
            
        }

        sb.append("</div>\n"); //dirlist
        
        // Render the page footer
        sb.append("<p><p>\n");        
        sb.append("<hr class=\"line\">\n");
        sb.append("<p>\n");
        
        /*
        String readme = getReadme(resource, encoding);
        if (readme!=null) {
            sb.append(readme);
            sb.append("<hr class=\"line\">");
        }
        */

        sb.append("<div class=\"upload\">\n");        
        sb.append("<h2>Upload File</h2>\n");
        String prefix = request.getServletPath();                      
        
        sb.append("<form class=\"upload-file\" action=\"" + prefix  + directoryWebappPath + "\"" 
        		+ " enctype=\"multipart/form-data\""
        		+ " onsubmit=\"onupload()\""
        		+ " method=post>\n");        
        sb.append("<label for=\"files\">Select file:</label>\n");
        sb.append("<input type=\"file\" id=\"files\" name=\"files\" multiple><br><br>\n");        
        sb.append("<input type=\"submit\" value=\"upload\">\n");
        sb.append("<div id=\"upmsg\" style=\"visibility: hidden;\"></div>\n");
        sb.append("</form>\n");
        sb.append("<br><br>\n");
        
        
        sb.append("<form class=\"upload-ovrw\" action=\"" + prefix  + directoryWebappPath + "\" + method=post>\n");
        sb.append("<div>Overwrite</div>\n");
        boolean overwrite = false;
        if(request.getSession().getAttribute("overwrite") != null)
        	overwrite = (Boolean) request.getSession().getAttribute("overwrite");
        sb.append("<input type=\"radio\" id=\"false\" name=\"overwrite\" value=\"false\" ");
        sb.append(overwrite ? "" : "checked");
        sb.append(">\n");
        sb.append("<label for=\"false\">false</label><br>\n");
        sb.append("<input type=\"radio\" id=\"true\" name=\"overwrite\" value=\"true\" ");
        sb.append(overwrite ? "checked" : "");
        sb.append(">\n");        
        sb.append("<label for=\"true\">true</label><br>");
        sb.append("<input type=\"submit\" value=\"update\">\n");        
        sb.append("</form><p>\n");

        
        String styinfo = "style=\"color: black;\"";
        String styerr = "style=\"color: red;\"";
        String stywarn = "style=\"color: orange;\"";
        
        HttpSession session = request.getSession();        
        
        ArrayList<LogRecord> msgupload = (ArrayList<LogRecord>)
        		session.getAttribute("msgupload");
        if (msgupload != null) {
        	sb.append("<div class=\"upload-msg\">\n");
        	for(LogRecord msg : msgupload) {
        		sb.append("<div ");
        		if (msg.getLevel().equals(Level.SEVERE)) {
        			sb.append(styerr);
        		} else if (msg.getLevel().equals(Level.WARNING)) {
        			sb.append(stywarn);
        		} else {
        			sb.append(styinfo);
        		}
        		sb.append("> ");
        		sb.append(msg.getMessage());
        		sb.append("</div>\n");
        	}
        	sb.append("</div><p>\n");
        	session.removeAttribute("msgupload");
        }
        	

        if (showServerInfo) {
            sb.append("<h3>").append(ServerInfo.getServerInfo()).append("</h3>\n");        
        }
        
        URL fturl = App.class.getResource("/resources/footeru1.txt");
		if (fturl != null) {

			sb.append("<p>\n");
			sb.append("<div class=\"footer\">\n");

			BufferedReader reader = new BufferedReader(new InputStreamReader(fturl.openStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			
			sb.append("</div>\n"); // footer
		}
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");

        // Return an input stream to the underlying bytes
        writer.write(sb.toString());
        writer.flush();
        return new ByteArrayInputStream(stream.toByteArray());
	}
	
    /**
     * Gets the ordering character to be used for a particular column.
     *
     * @param order  The order that is currently being applied
     * @param column The column that will be rendered.
     *
     * @return Either 'A' or 'D', to indicate "ascending" or "descending" sort
     *         order.
     */
    private char getOrderChar(SortManager.Order order, char column) {
        if(column == order.column) {
            if(order.ascending) {
                return 'D';
            } else {
                return 'A';
            }
        } else {
            return 'D';
        }
    }

}
