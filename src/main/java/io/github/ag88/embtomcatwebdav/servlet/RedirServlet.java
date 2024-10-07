package io.github.ag88.embtomcatwebdav.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.opt.OptFactory;

/**
 * Class RedirServlet.
 * <p>
 * This servlet simply redirects to the path given when creating the object.
 */
public class RedirServlet extends HttpServlet {
	
	Log log = LogFactory.getLog(RedirServlet.class);
	
	String redirpath;
	boolean quiet;

	/**
	 * Instantiates a new redir servlet. 
	 *
	 * @param path the path to redirect to
	 */
	public RedirServlet(String path) {
		redirpath = path;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		quiet = (Boolean) OptFactory.getInstance().getOpts().get("quiet").getValue();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		/* do not redirect favicon requests */
		if (req.getRequestURI().equals("/favicon.ico")) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		if (!quiet)
			log.info("Redirect from: ".concat(req.getRequestURI()));					
		
		StringBuilder sb = new StringBuilder(100);
		sb.append(req.getScheme());
		sb.append("://");
		sb.append(req.getServerName());
		if(req.getServerPort() != 80) {
			sb.append(":");
			sb.append(req.getServerPort());
		}
		sb.append(req.getContextPath());
		if(!req.getContextPath().endsWith("/"))
			sb.append("/");
		if (redirpath.startsWith("/"))
			redirpath = redirpath.substring(1);
		sb.append(redirpath);
		resp.sendRedirect(sb.toString());
	}

}
