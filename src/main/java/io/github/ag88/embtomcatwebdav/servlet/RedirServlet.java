package io.github.ag88.embtomcatwebdav.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirServlet extends HttpServlet {
	
	String redirpath;

	public RedirServlet(String path) {
		redirpath = path;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
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
