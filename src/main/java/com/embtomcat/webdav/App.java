package com.embtomcat.webdav;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

public class App 
{
	
	Log log = LogFactory.getLog(App.class);
	
	Tomcat tomcat; 
	
	int port = 8080;
	String shost = "localhost";
	String path = System.getProperty("user.dir");
	String basedir = null;
	String user = null;
	String passwd = null;
	String realm = "Simple";
	boolean quiet = false;

	public App() {
	}
		
	public void run(String[] args) {
		parseargs(args);
		runserver();
	}
	
	private void runserver() {
		try {
			if(basedir == null)
				basedir = System.getProperty("user.dir")
				.concat("/tomcat.")
				.concat(Integer.toString(port));
			if(!quiet)
				log.info(String.format("tomcat basedir: %s", basedir));
			
			tomcat = new Tomcat();			
			tomcat.setBaseDir(basedir);
			tomcat.setSilent(quiet);
			tomcat.setPort(port);
			tomcat.setHostname(shost);
			
			Thread hook = new Thread() {
				@Override
				public void run() {
					try {
						tomcat.stop();
						tomcat.destroy();
					} catch (LifecycleException e) {
						log.error(e.getMessage(), e);
					}				
				}			
			};			
			Runtime.getRuntime().addShutdownHook(hook);
			
			Context context = tomcat.addContext("", path);
			tomcat.setAddDefaultWebXmlToWebapp(false);
			Tomcat.addDefaultMimeTypeMappings(context);
			
			if(!quiet)
				log.info(String.format("serving path: %s", path));

			if (user != null) {
				tomcat.addUser(user, passwd);
				tomcat.addRole(user, "user");
				Realm realm = tomcat.getEngine().getRealm();
				context.setRealm(realm);
				LoginConfig lconf = new LoginConfig();
				// NONE, BASIC, DIGEST, FORM, or CLIENT-CERT.
				lconf.setAuthMethod("BASIC");
				lconf.setRealmName("Simple");
				lconf.setCharset(Charset.forName("UTF-8"));
				context.setLoginConfig(lconf);
				context.getPipeline().addValve(new BasicAuthenticator());
				SecurityConstraint secconstr = new SecurityConstraint();
				secconstr.addAuthRole("user");
				secconstr.setAuthConstraint(true);
				SecurityCollection sc = new SecurityCollection();
				sc.addPattern("/*");
				secconstr.addCollection(sc);
				context.addConstraint(secconstr);				
			}			
			
			WebdavServlet webdav = new WebdavServlet();
			Wrapper ws = Tomcat.addServlet(context, "webdav", webdav);			
			ws.addInitParameter("debug", "0");
			ws.addInitParameter("listings", "true");
			ws.addInitParameter("sortListings", "true");
			ws.addInitParameter("readonly", "false");
			ws.addInitParameter("allowSpecialPaths", "true");			

			context.addServletMappingDecoded("/webdav/*", "webdav");
			context.setSessionTimeout(30);

			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			log.error(e.getMessage(), e);
		}		
	}
	
	private void parseargs(String[] args) {
		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help").desc("help").build());		
		options.addOption(Option.builder("H").longOpt("host").desc("set host")
				.hasArg().argName("hostname").build());
		options.addOption(Option.builder("p").longOpt("port").desc("set port")
				.hasArg().argName("port").build());
		options.addOption(Option.builder("P").longOpt("path")
				.desc("set path, default current working dir")
				.hasArg().argName("path").build());
		options.addOption(Option.builder("b").longOpt("basedir")
				.desc("set basedir, a work folder for tomcat, default [current working dir]/tomcat.port")
				.hasArg().argName("path").build());
		options.addOption(Option.builder("u").longOpt("user")
				.desc("set user")
				.hasArg().argName("username").build());
		options.addOption(Option.builder("w").longOpt("passwd")
				.desc("set password, you may omit this, it would prompt for it if -u is specified")
				.hasArg().argName("password").build());
		options.addOption(Option.builder("q").longOpt("quiet").desc("mute (most) logs").build());
		
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
						
			if (cmd.hasOption("host")) {
				shost = cmd.getOptionValue("host");
			} 
			
			if (cmd.hasOption("port")) {
				try {
					port = Integer.parseInt(cmd.getOptionValue("port"));
				} catch (NumberFormatException e) {
					log.warn("invalid port: ".concat(cmd.getOptionValue("port"))
							.concat(", using default 8080 instead"));
					port = 8080;
				}
			} 
			
			if (cmd.hasOption("path")) {
				String p = cmd.getOptionValue("path");
				path = new File(p).getAbsolutePath();
			} 
			
			if (cmd.hasOption("basedir")) {
				String p = cmd.getOptionValue("basedir");
				basedir = new File(p).getAbsolutePath();
			}
			
			if (cmd.hasOption("user")) {
				user = cmd.getOptionValue("user");
			} 
			
			if (cmd.hasOption("passwd")) {
				passwd = cmd.getOptionValue("passwd");
			}

			if (user != null && passwd == null) {
				log.info(String.format("enter password for %s:", user));
				Scanner input = new Scanner(System.in);
				passwd = input.nextLine();
				input.close();
			}
			
			if(cmd.hasOption("quiet")) {
				quiet = true;
			}
			
			if(cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				Map<String, String> mkv = readManifest();
				String name = mkv.get("artifactId")
						.concat("-").concat(mkv.get("version"));
				formatter.printHelp(name, options);
				System.exit(0);
			}
			
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
		
	}	
	
	
	private Map<String, String> readManifest() {
		TreeMap<String, String> mret = new TreeMap<String, String>();
		try {

			Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				Manifest manifest = new Manifest(resources.nextElement().openStream());

				Attributes a = manifest.getMainAttributes();
				for (Object o : a.keySet()) {
					String k = o.toString();
					String v = a.get(o).toString();
					mret.put(k, v);
				}

				return mret;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
    public static void main(String[] args)  {
        App app = new App();
        app.run(args);
    }
}
