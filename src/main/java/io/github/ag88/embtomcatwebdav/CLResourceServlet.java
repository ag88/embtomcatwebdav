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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.opt.OptFactory;

/**
 * Class CLResourceServlet.
 * 
 * This is a servlet that serves classpath resources from "/resource" in the classpath.
 * In the default implementation this is mounted at "/res", by {@link WebDavServer}.
 *  
 */
public class CLResourceServlet extends HttpServlet {

	/** The log. */
	Log log = LogFactory.getLog(CLResourceServlet.class);
	
	/** The resources. */
	WebResourceRoot resources;
	
	/**
	 * Instantiates a new CL resource servlet.
	 */
	public CLResourceServlet() {
	}
	
	/**
	 * Servlet init()
	 * 
	 * @param config the config
	 * @throws ServletException the servlet exception
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
        resources = (WebResourceRoot) getServletContext().getAttribute(Globals.RESOURCES_ATTR);
	}
	
	/**
	 * Servlet doGet().
	 *
	 * This actually serves the resources.
	 * 
	 * @param req the req
	 * @param resp the resp
	 * @throws ServletException the servlet exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//super.doGet(req, resp);
		
		String pathinfo = req.getPathInfo();
		
		log.debug(pathinfo);
		
		int psl = pathinfo.lastIndexOf('/');
		if(psl == -1 || psl == pathinfo.length()-1) { 
			// not found or empty string
			// return an empty body
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		
		String filename = "/".concat(pathinfo.substring(psl+1));
		
		URL resurl = App.class.getResource("/resources".concat(filename));
		
		if ( resurl == null ) { // not found
			resp.setStatus(HttpServletResponse.SC_OK);
			log.warn(String.format("resource %s not found in classpath", filename));
			return;			
		}
		
		try {
			
			if(filename.indexOf('.')>=0) {
				String ext = filename.substring(filename.indexOf('.')+1);
				String mimetype = resources.getContext().findMimeMapping(ext);
				if(mimetype == null || mimetype.equals(""))
					mimetype = "text/html";
				resp.setContentType(mimetype);
				resp.setHeader("Content-disposition", "inline");
				log.debug(mimetype);
			}					

			int size = resurl.openConnection().getContentLength();
			log.debug(String.format("size: %d", size));
			resp.setContentLength(size);

			OutputStream os = resp.getOutputStream();
			InputStream is = resurl.openStream();
			byte[] buf = new byte[8192];
			int n = 0;
			while(( n = is.read(buf)) >= 0) {
				os.write(buf, 0, n);
			}
			os.flush();
			os.close();
			is.close();
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			log.error(sw.toString());
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}		
	}
}
