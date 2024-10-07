package io.github.ag88.embtomcatwebdav.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.util.VelUtil;

/**
 * Class MkDirServlet.
 * <p>
 * This servlet creates a folder in a parent folder
 * 
 */
public class MkDirServlet extends HttpServlet {
	
	Log log = LogFactory.getLog(MkDirServlet.class);
	
	protected transient WebResourceRoot resources = null;
	protected Path wpath;
	protected String urlprefix;
	protected boolean quiet;
	
	public MkDirServlet() {				
	}
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		resources = (WebResourceRoot) getServletContext().getAttribute(Globals.RESOURCES_ATTR);

		String basedir =  (String) OptFactory.getInstance().getOpt("basedir").getValue();
		
		Path wpath = Paths.get(basedir); 
		if(! (Files.exists(wpath) && Files.isDirectory(wpath)))
			throw new ServletException("invalid workdir (not found):".concat(basedir));
		
		this.wpath = wpath;
		
		this.urlprefix =  (String) OptFactory.getInstance().getOpt("urlprefix").getValue();
		if(null == this.urlprefix)
			this.urlprefix = (String) OptFactory.getInstance().getOpt("urlprefix").getDefaultval();
		
		this.quiet = (Boolean) OptFactory.getInstance().getOpt("quiet").getValue();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		VelocityContext context = new VelocityContext();
		
        doGetTemplate(req, resp, context);
	}
	
	protected void doGetTemplate(HttpServletRequest req, HttpServletResponse resp, VelocityContext context) 
			throws ServletException, IOException {
		Template template = VelUtil.getInstance().loadvmtemplate("velocity/mkdir.vm");
		context.put("mkdirpath", req.getServletPath());		
		for(Cookie c: req.getCookies()) {
			if (c.getName().equals("x-wdav-path")) {
				String path = c.getValue();
				context.put("parentdir", path);
				break;
			}				
		}
		
		String backurl = urlprefix;
		if (! backurl.endsWith("/"))
			backurl = backurl.concat("/");
		if (context.get("parentdir") != null)
			backurl = backurl.concat((String) context.get("parentdir"));
		context.put("back", backurl);
				
		PrintWriter writer = resp.getWriter(); 
		
        template.merge(context, writer); 
        writer.flush();
        writer.close();        

	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//prparams(req, resp);
		
		HttpSession session = req.getSession();		
		if (session.isNew()) {
			// String encodedURL =
			// response.encodeRedirectURL(request.getRequestURL().toString());
			// response.sendRedirect(encodedURL);
			if (!quiet)
				log.warn("session is new ".concat(session.getId()));
			
			String referrer = req.getHeader("referer");
			if (req.getRequestURL().toString().equals(referrer)) {
				try {
					URI uri = new URI(req.getRequestURL().toString());
					uri = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(), uri.getPort(),
						"/res/nocookie.htm",null, null);				
					resp.sendRedirect(uri.toURL().toString());
				} catch (URISyntaxException e) {
					log.error(e);
				}
			}			
			return;
		}
		
		doMKDir(req, resp);
	}
	
	
	protected void doMKDir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
						
		String parentdir = req.getParameter("parentdir");
		
		if (parentdir == null || parentdir == "") {
			String msg = "parent directory cannot be null or empty";
			if(!quiet) log.warn(msg);
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			doGetTemplate(req, resp, context);
			return;
		}
		
		WebResource resource = resources.getResource(parentdir);
		if (! resource.exists()) {
			String msg = "parent directory not found";
			if(!quiet)
				log.warn(msg + ": ".concat(parentdir));
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			doGetTemplate(req, resp, context);
			return;
		}
		
		if (! resource.isDirectory()) {
			String msg = "parent is not directory";
			if(!quiet)
				log.warn(msg + ": ".concat(parentdir));
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			doGetTemplate(req, resp, context);
			return;
		}

		String newfoldername = req.getParameter("newfoldername");

		if (newfoldername == null || newfoldername == "") {
			String msg = "new folder name cannot be null or empty";
			if(!quiet)
				log.warn(msg);
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			doGetTemplate(req, resp, context);
			return;
		}
		
		if (!parentdir.endsWith("/"))
			parentdir = parentdir.concat("/");		
		
		if (!resources.mkdir(parentdir.concat(newfoldername))) {
			String msg = "IO error creating directory: ".concat(newfoldername);
			log.warn(msg);
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			doGetTemplate(req, resp, context);
			return;
		} else {
			String msg = "Directory created: " + newfoldername;
			if(!quiet)
				log.info("Directory created: " + parentdir.concat(newfoldername));
			VelocityContext context = new VelocityContext();
			context.put("msg", msg);
			doGetTemplate(req, resp, context);
			return;
		}
	}
	
	
}
