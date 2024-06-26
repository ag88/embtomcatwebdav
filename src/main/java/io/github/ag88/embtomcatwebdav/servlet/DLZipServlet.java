package io.github.ag88.embtomcatwebdav.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.opt.OptFactory;

/**
 * Class DLZipServlet.
 * <p>
 * This servlet provides download selected files as zip service.
 * 
 */
public class DLZipServlet extends HttpServlet {
	
	Log log = LogFactory.getLog(WDavUploadServlet2.class);
	
	protected transient WebResourceRoot resources = null;
	protected Path wpath;
	protected String urlprefix;
	
	public DLZipServlet() {				
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
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//prparams(req, resp);
		doDLZip(req, resp);
	}
	
	protected void doDLZip(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
				
		ArrayList<WebResource> files = new ArrayList<WebResource>(5);
		String[] values = req.getParameterValues("sel");
		for (String v : values) {
			String rpath = v.substring(urlprefix.length());			
			try {
				WebResource resource = resources.getResource(rpath);
				if (! resource.exists()) {
					log.warn("not found file :".concat(rpath));
					continue;
				}
				if (! resource.isFile()) {
					log.warn("invalid file :".concat(rpath));
					continue;
				}
				files.add(resource);				
			} catch (IllegalArgumentException e) {
				log.warn("path :".concat(rpath));
				log.warn(e);
			}
		}
		
		Path zipp = Files.createTempFile(wpath, null, ".zip");
		
		try {
			ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zipp));
			for(WebResource r : files) {
				ZipEntry entry = new ZipEntry(r.getName());
				zout.putNextEntry(entry);
	            InputStream is = r.getInputStream();	            
	            byte[] bytes = new byte[4096];
	            int len = 0;
	            while((len = is.read(bytes)) != -1) {
	            	zout.write(bytes, 0, len);
	            }
	            is.close();
	            zout.flush();
	            zout.closeEntry();	            
			}
			zout.flush();
			zout.close();
		} catch (IOException e) {
			log.error(e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		} 
				
		resp.setContentType("application/zip");
		resp.addHeader("Content-Disposition", "attachment; filename=files.zip");
		resp.setContentLengthLong(Files.size(zipp));
		
        try {
            FileInputStream fileInputStream = new FileInputStream(zipp.toFile());
            OutputStream respOutputStream = resp.getOutputStream();
            int bytes;
            while ((bytes = fileInputStream.read()) != -1) {
                respOutputStream.write(bytes);
            }
            respOutputStream.flush();
            
            //clean up delete the temp zip file
            Files.deleteIfExists(zipp);
        } catch (IOException e) {
            log.error(e);
        }

	}
	
	protected void prparams(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		PrintWriter writer = resp.getWriter();		
		
		StringBuilder sb = new StringBuilder();		
		sb.append("pathinfo:");
		sb.append(req.getPathInfo());
		sb.append(System.lineSeparator());
		writer.write(sb.toString());
		sb = new StringBuilder();
		sb.append("contextpath:");
		sb.append(req.getContextPath());
		sb.append(System.lineSeparator());
		writer.write(sb.toString());
		sb = new StringBuilder();
		sb.append("catalina base:");
		sb.append(System.getProperty("catalina.base"));
		sb.append(System.lineSeparator());
		writer.write(sb.toString());
		sb = new StringBuilder();
		writer.write(sb.toString());
		sb.append("catalina home:");
		sb.append(System.getProperty("catalina.home"));
		sb.append(System.lineSeparator());
		writer.write(sb.toString());
		sb = new StringBuilder();
		writer.write(sb.toString());
		sb.append("servletpath:");
		sb.append(req.getServletPath());
		sb.append(System.lineSeparator());
		writer.write(sb.toString());
		sb = new StringBuilder();
		sb.append("URI:");
		sb.append(req.getRequestURI());
		sb.append(System.lineSeparator());
		writer.write(sb.toString());					
		sb = new StringBuilder();
		sb.append("urlprefix:");
		sb.append(urlprefix);
		sb.append(System.lineSeparator());
		writer.write(sb.toString());
		
		writer.write("headers:\n");
		Enumeration<String> en = req.getHeaderNames();
		while(en.hasMoreElements()) {
			String name = en.nextElement();
			String value = req.getHeader(name);
			writer.write(name);
			writer.write(" : ");
			writer.write(value);
			writer.write(System.lineSeparator());
		}
		
		writer.write(System.lineSeparator());
		writer.write("param:");
		writer.write(System.lineSeparator());

		String[] values = req.getParameterValues("sel");
		for (String v : values) {
			String path = v.substring(urlprefix.length());
			writer.write(path);
			WebResource resource;
			try {
				resource = resources.getResource(path);
				writer.write(" , is file:");
				writer.write(Boolean.toString(resource.isFile()));
				writer.write(" , exists:");
				writer.write(Boolean.toString(resource.exists()));
				writer.write(System.lineSeparator());
			} catch (IllegalArgumentException e) {
				log.warn("path :".concat(path));
				log.warn(e);
			}
		}

		writer.write("resource urls\n");		
		for(URL url : resources.getBaseUrls()) {
			writer.write(url.toString());
			writer.write(System.lineSeparator());			
		}
		
		writer.write("pre resources\n");
		prresources(resources.getPreResources(), writer);

		writer.write("jar resources\n");
		prresources(resources.getJarResources(), writer);

		writer.write("post resources\n");
		prresources(resources.getPostResources(), writer);
		
		writer.flush();		
	}

	private void prresources(WebResourceSet[] resources, PrintWriter writer) {
		for(WebResourceSet wrs : resources) {
			writer.write("baseurl:");
			writer.write(wrs.getBaseUrl().toString());
			writer.write(System.lineSeparator());
			writer.write("state:");
			writer.write(wrs.getStateName());
			writer.write(System.lineSeparator());
			writer.write("static only:");
			writer.write(Boolean.toString(wrs.getStaticOnly()));
			writer.write(System.lineSeparator());
		}
	}
	
}
