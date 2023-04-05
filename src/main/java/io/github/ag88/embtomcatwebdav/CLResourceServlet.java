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

public class CLResourceServlet extends HttpServlet {

	Log log = LogFactory.getLog(CLResourceServlet.class);
	
	WebResourceRoot resources;
	
	public CLResourceServlet() {
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
        resources = (WebResourceRoot) getServletContext().getAttribute(Globals.RESOURCES_ATTR);
	}
	
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
