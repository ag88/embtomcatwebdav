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
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.util.ServerInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.apache.tomcat.util.security.Escape;

import io.github.ag88.embtomcatwebdav.util.DefFilePathNameValidator;
import io.github.ag88.embtomcatwebdav.util.FilePathNameValidator;

public class WDavUploadServlet extends WebdavServlet {
	
	Log log = LogFactory.getLog(WDavUploadServlet.class);
		
	public WDavUploadServlet() {
		super();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		HttpSession session = request.getSession();
		if (session.isNew()) {			
			session.setAttribute("nocookies", Boolean.TRUE);
			String encodedURL = response.encodeRedirectURL(request.getRequestURL().toString());
			response.sendRedirect(encodedURL);
			return;
		} else {			
			boolean nocookies;
			if(session.getAttribute("nocookies") == null)
				nocookies = true;
			else
				nocookies = (Boolean) session.getAttribute("nocookies");
			if (nocookies) {
				Cookie[] cookies = request.getCookies();
				if (cookies != null)
					for (Cookie c : cookies) {
						if(c.getName().equals("JSESSIONID")) { 
							//&&c.getValue().equals(session.getId())) {
							nocookies = false;
							break;
						}
					}
					if(!nocookies)
						session.setAttribute("nocookies", Boolean.FALSE);
					else {
						StringBuilder sb = new StringBuilder();
				        // Render the page header
				        sb.append("<!doctype html><html>\r\n");
				        /* TODO Activate this as soon as we use smClient with the request locales
				        sb.append("<!doctype html><html lang=\"");
				        sb.append(smClient.getLocale().getLanguage()).append("\">\r\n");
				        */
				        sb.append("<head>\r\n");
				        sb.append("<title>");
				        sb.append("Enable (session) cookies");
				        sb.append("</title>\r\n");
				        sb.append("<style>");
				        sb.append(org.apache.catalina.util.TomcatCSS.TOMCAT_CSS);
				        sb.append("</style> ");
				        sb.append("</head>\r\n");
				        sb.append("<body>\n");
				        sb.append("<h1>Please enable session cookies for this app/website</h1>\n\n");
				        sb.append("This website / app requires (session) cookies to orderly function.  ");
				        sb.append("It does not track as the data is normally cleared when the browser is closed.<br>");
				        sb.append("It is also required as it is mainly to maintain a session id, ");
				        sb.append("for more secure functioning of the app/website.<br><br>");
				        sb.append("It is adequate to enable cookies in your browser for this site/app ");
				        sb.append("that is deleted when the browser is closed. Or to simply enable cookies for this website.<br>");
				        sb.append("</body></html>");
				        
				        response.setContentType("text/html");
				        response.setContentLength(sb.length());
				        BufferedWriter writer = new BufferedWriter(response.getWriter());
				        writer.write(sb.toString());
				        writer.flush();
				        writer.close();
				        return;
					}
			}
		}

		super.doGet(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		log.info(request.getContentType());
		
		boolean overwrite = false;		

		HttpSession session = request.getSession();
		if (session.isNew())
			log.warn("session is new ".concat(session.getId()));
		if (session.isNew()) {
			// String encodedURL =
			// response.encodeRedirectURL(request.getRequestURL().toString());
			// response.sendRedirect(encodedURL);
			response.sendRedirect(request.getRequestURL().toString());
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

					// validate filename
					FilePathNameValidator validator = new DefFilePathNameValidator();
					validator.setReadonly(false);
					if(!validator.isValidFilename(filename, messages)) {
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
						log.error(errmsg);
						continue;
					}
					
					if(dirpath == null || !validator.isValidPathname(dirpath, filename, messages)) {
						if(dirpath == null) {
							LogRecord lr = new LogRecord(Level.SEVERE, "basepath is null");
							messages.add(lr);
							log.error("basepath is null");
						}
						dolog(messages);
						continue;
					}
					
					if(Files.exists(dirpath.resolve(filename))) {
						if(! overwrite) {
							LogRecord lr = new LogRecord(Level.WARNING, 
								String.format("file %s exists, not overwriting", filename));
							messages.add(lr);
							log.warn(String.format("file %s exists, not overwriting", dirpath.resolve(filename)));							
							continue;
						}
					}
					
					InputStream stream = item.openStream();
					if (!item.isFormField()) { // is file
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
													
						messages.add(new LogRecord(Level.INFO, "name: ".concat(name)));
						messages.add(new LogRecord(Level.INFO, "uploaded filename: ".concat(filename)));
						log.info("uploaded filename:".concat(filename));
					} else {
						String formFieldValue = Streams.asString(stream);
						messages.add(new LogRecord(Level.INFO, "field: ".concat(name)));
						messages.add(new LogRecord(Level.INFO, "value: ".concat(formFieldValue)));
						log.info(formFieldValue);
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
        sb.append("request.pathinfo: " + request.getPathInfo() + "<br><br>\n");        
        sb.append("canonnical path: " + resource.getCanonicalPath() + "<br><br>\n");
        
        sb.append("<form action=\"" + prefix  + directoryWebappPath + "\" + method=post>");
        sb.append("<label>Overwrite</label><br>\n");
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
        sb.append("</form><br>\n");
        
        sb.append("<form action=\"" + prefix  + directoryWebappPath + "\"" +
        		" enctype=\"multipart/form-data\" method=post>");        
        sb.append("<label for=\"files\">Select file:</label>\n");
        sb.append("<input type=\"file\" id=\"files\" name=\"files\" multiple><br><br>\n");        
        sb.append("<input type=\"submit\" value=\"upload\">\n");
        sb.append("</form><br>\n");
        
        String styinfo = "style=\"color: black;\"";
        String styerr = "style=\"color: red;\"";
        String stywarn = "style=\"color: orange;\"";
        
        HttpSession session = request.getSession();        
        sb.append("sessionid: ");
        if(session.isNew())
        	sb.append("new ");
        sb.append(session.getId());
        sb.append("<br>");        
        sb.append("request URI:");
        sb.append(request.getRequestURI());
        sb.append("<br>");
        sb.append("request URL:");
        sb.append(request.getRequestURL());
        sb.append("<br>");
        sb.append("request method:");
        sb.append(request.getMethod());
        sb.append("<br>");
        sb.append("request querystring:");
        sb.append(request.getQueryString());
        sb.append("<br>");
        
        ArrayList<LogRecord> msgupload = (ArrayList<LogRecord>)
        		session.getAttribute("msgupload");
        if (msgupload != null) {
        	sb.append("<div>\n");
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
        	sb.append("</div>\n");
        	session.removeAttribute("msgupload");
        }
        	

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
