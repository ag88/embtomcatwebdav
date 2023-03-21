package com.embtomcat.webdav;

import javax.servlet.ServletConfig;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;

public class App 
{
	
	final int port = 8082;
	final String host = "localhost";
	
	Tomcat tomcat; 
	
	public App() {
		tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.setHostname(host);
		tomcat.setBaseDir(System.getProperty("user.dir"));
		
		Thread hook = new Thread() {
			@Override
			public void run() {
				try {
					tomcat.stop();
					tomcat.destroy();
				} catch (LifecycleException e) {
					e.printStackTrace();
				}				
			}			
		};			
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	
	public void run(String[] args) {
		try {
			Context context = tomcat.addContext("", System.getProperty("user.dir"));
			
			WebdavServlet webdav = new WebdavServlet();
			Wrapper ws = Tomcat.addServlet(context, "webdav", webdav);			
			ws.addInitParameter("debug", "0");
			ws.addInitParameter("listings", "true");
			ws.addInitParameter("readonly", "false");
			ws.addInitParameter("allowSpecialPaths", "true");

			context.addServletMappingDecoded("/webdav/*", "webdav");

			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args)  {
        App app = new App();
        app.run(args);
    }
}
