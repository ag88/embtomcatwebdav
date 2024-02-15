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

package io.github.ag88.embtomcatwebdav.servlet;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.servlet.RequestDispatcher;
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

import java.io.StringWriter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import io.github.ag88.embtomcatwebdav.App;
import io.github.ag88.embtomcatwebdav.model.HtmDirEntry;
import io.github.ag88.embtomcatwebdav.model.HtmLogEntry;
import io.github.ag88.embtomcatwebdav.opt.Opt;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.util.DefFilePathNameValidator;
import io.github.ag88.embtomcatwebdav.util.FilePathNameValidator;
import io.github.ag88.embtomcatwebdav.util.QueryString;
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
public class WDavUploadServlet2 extends WebdavServlet {
	
	/** The log. */
	Log log = LogFactory.getLog(WDavUploadServlet2.class);
		
	/** The quiet. */
	boolean quiet;
	
	/** DL Zip path */
	String dlzip_path;
	
	/**
	 * Instantiates a new w dav upload servlet.
	 */
	public WDavUploadServlet2() {
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

		Properties p = new Properties();
		p.put("resource.loaders", "class");
		p.put("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
				
		Opt opt = OptFactory.getInstance().getOpt("quiet");
		if(opt != null)
			quiet = ((Boolean) opt.getValue()).booleanValue();
		else
			quiet = false;
		
		String dlzippath = (String) OptFactory.getInstance().getOpt("dlzip_path").getValue();
		if(null == dlzippath || dlzippath.equals(""))
			this.dlzip_path = (String) OptFactory.getInstance().getOpt("dlzip_path").getDefaultval();
		else
			this.dlzip_path = dlzippath;
	}

	/* this override is to keep path evaluation the same
	 * be it inheriting from WebdavServlet or DefaultServlet
	 */
    @Override
    protected String getRelativePath(HttpServletRequest request, boolean allowEmptyPath) {
        String pathInfo;

        if (request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) != null) {
            // For includes, get the info from the attributes
            pathInfo = (String) request.getAttribute(RequestDispatcher.INCLUDE_PATH_INFO);
        } else {
            pathInfo = request.getPathInfo();
        }

        StringBuilder result = new StringBuilder();
        if (pathInfo != null) {
            result.append(pathInfo);
        }
        if (result.length() == 0) {
            result.append('/');
        }

        return result.toString();
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

		VelocityContext context = new VelocityContext();
		
		//context.put("querystr", request.getQueryString());
		
		Map<String,String[]> params = request.getParameterMap();
		/*
		for(String k: params.keySet()) {
			sb = new StringBuilder(32);
			sb.append("key:");
			sb.append(k);
			sb.append("\n");
			boolean first = true;
			for(String v : params.get(k)) {
				if(first)
					first = false;
				else
					sb.append(", ");
				sb.append(v);
			}
			sb.append("\n");
			log.info(sb.toString());
		}
		*/
		
						
		Template template;
		String multidl = request.getParameter("multidl");
		if(null != multidl && multidl.equals("y")) {
			template = loadvmtemplate("velocity/dirlistsel.vm");
			context.put("querystr", request.getQueryString());
		} else {
			QueryString qs = new QueryString();
			template = loadvmtemplate("velocity/dirlist.vm");
			qs.getParams().putAll(params);
			qs.put("multidl", "y");
			context.put("querystr", qs.getQueryString());
		}
		
				
        // Render the page header
		context.put("title", sm.getString("directory.title", directoryWebappPath));
		
        // breadcrumb at top
        
        //sb.append(sm.getString("directory.title", directoryWebappPath));
        
        // Render the link to our parent (if required)
        String parentDirectory = directoryWebappPath;
        String parentpath = "/", parent = "/";
        String currdir;
        sb = new StringBuilder(100);
        if (parentDirectory.endsWith("/")) {
            parentDirectory =
                parentDirectory.substring(0, parentDirectory.length() - 1);
        }
        int slash = parentDirectory.lastIndexOf('/');
        if (slash >= 0) {
            parent = directoryWebappPath.substring(0, slash);
            sb.append(rewrittenContextPath);
            if (parent.equals("")) {
                parent = "/";
            }
            sb.append(rewriteUrl(parent));
            if (!parent.endsWith("/")) {
                sb.append('/');
            }
            parentpath = sb.toString();
        }
       
        if(slash < directoryWebappPath.length())
        	currdir = directoryWebappPath.substring(slash+1);
        else
        	currdir = directoryWebappPath;
        

        context.put("parentpath", parentpath);
        context.put("parent", parent);
        context.put("currdir", currdir);        
        
        // directory listing
        SortManager sortmgr = new SortManager(true);

        SortManager.Order order;
        if(sortListings && null != request) {
            //order = sortmgr.getOrder(request.getQueryString());
        	order = sortmgr.getOrder(params);
        } else {
            order = null;
        }
        
        // Render the column headings 
		Map<String, String[]> param_rest = new TreeMap<String,String[]>();
		param_rest.putAll(params);
		param_rest.remove("SCOL");
		param_rest.remove("SORD");		

		QueryString qs_col = new QueryString();
		qs_col.put("SCOL", "N");
		qs_col.put("SORD", String.valueOf(getOrderChar(order, 'N')));
		qs_col.getParams().putAll(param_rest);
		
        context.put("fn_sortop", qs_col.getQueryString());        
        context.put("lb_fn", sm.getString("directory.filename"));
        
        qs_col.clear();
		qs_col.put("SCOL", "S");
		qs_col.put("SORD", String.valueOf(getOrderChar(order, 'S')));
		qs_col.getParams().putAll(param_rest);
		
        context.put("size_sortop", qs_col.getQueryString());        
        context.put("lb_size", sm.getString("directory.size"));

        qs_col.clear();
		qs_col.put("SCOL", "M");
		qs_col.put("SORD", String.valueOf(getOrderChar(order, 'M')));
		qs_col.getParams().putAll(param_rest);

        context.put("modif_sortop", qs_col.getQueryString()); 
        context.put("lb_modif", sm.getString("directory.lastModified"));

        if(null != sortmgr && null != request) {
            //sortmgr.sort(entries, request.getQueryString());
        	sortmgr.sort(entries, request.getParameterMap());
        }

        List<HtmDirEntry> direntries = new ArrayList<HtmDirEntry>(20);
        
        for (WebResource childResource : entries) {
            String filename = childResource.getName();
            if (filename.equalsIgnoreCase("WEB-INF") ||
                filename.equalsIgnoreCase("META-INF")) {
                continue;
            }

            if (!childResource.exists()) {
                continue;
            }
            
            String path = rewrittenContextPath.concat(childResource.getWebappPath());
            if (childResource.isDirectory()) {
                path = path.concat("/");
            }
            HtmDirEntry entry = new HtmDirEntry(Escape.htmlElementContent(filename), path,
            	childResource.isDirectory(), childResource.getContentLength(), childResource.getLastModified(), 
            	"S".concat(Integer.toString(direntries.size())));
            direntries.add(entry);          
        }
                
        context.put("direntries", direntries);
		context.put("dirselformpath", dlzip_path);        
        
        // Render the page footer
        // upload form
        
        String prefix = request.getServletPath();
        String uploadformpath = prefix  + directoryWebappPath;
        boolean overwrite = false;
        if(request.getSession().getAttribute("overwrite") != null)
        	overwrite = (Boolean) request.getSession().getAttribute("overwrite");

        context.put("uploadformpath", uploadformpath);
        context.put("overwrite", overwrite);
                	
        HttpSession session = request.getSession();                
        ArrayList<LogRecord> msgupload = (ArrayList<LogRecord>)
        		session.getAttribute("msgupload");
        if (msgupload != null) {
        	List<HtmLogEntry> logrecs = new ArrayList<HtmLogEntry>(3);
        	for(LogRecord msg : msgupload) {
        		logrecs.add(new HtmLogEntry(msg));
        	}
        	context.put("logrecs", logrecs);
        	session.removeAttribute("msgupload");
        }
        
        if (showServerInfo) {
        	context.put("serverinfo", ServerInfo.getServerInfo());
        }
                
        URL fturl = App.class.getResource("/resources/footeru1.txt");
		if (fturl != null) {

			sb = new StringBuilder(100);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(fturl.openStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
			}
			reader.close();
			
			context.put("footer", sb.toString());
		}		
		
   		template.merge( context, writer );

        // Return an input stream to the underlying bytes
        //writer.write(sb.toString());
        writer.flush();
        return new ByteArrayInputStream(stream.toByteArray());
	}
	
	
	public Template loadvmtemplate(String filename) throws IOException {
		Template template = null;
		try	{
		  template = Velocity.getTemplate(filename);
		  return template;
		} catch( ResourceNotFoundException rnfe ) {
			// couldn't find the template
			log.error("cannot find template ".concat(filename));
			log.error(rnfe);
			throw new IOException(rnfe);
		} catch( ParseErrorException pee ) {
			// syntax error: problem parsing the template
			log.error("template parse error ".concat(filename));
			log.error(pee);
			throw new IOException(pee);
		} catch( MethodInvocationException mie ) {
			// something invoked in the template
			// threw an exception
			throw new IOException(mie);
		} catch( Exception e ) {
			throw new IOException(e);
		}
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
