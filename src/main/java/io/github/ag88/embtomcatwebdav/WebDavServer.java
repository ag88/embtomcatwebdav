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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.swing.SwingUtilities;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
import org.apache.catalina.servlets.WebdavServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.webresources.StandardRoot;
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

import io.github.ag88.embtomcatwebdav.gui.Gui;
import io.github.ag88.embtomcatwebdav.gui.Util;
import io.github.ag88.embtomcatwebdav.opt.Opt;
import io.github.ag88.embtomcatwebdav.opt.OptAccesslogDays;
import io.github.ag88.embtomcatwebdav.opt.OptAccesslogDir;
import io.github.ag88.embtomcatwebdav.opt.OptAccesslogRot;
import io.github.ag88.embtomcatwebdav.opt.OptBaseDir;
import io.github.ag88.embtomcatwebdav.opt.OptConf;
import io.github.ag88.embtomcatwebdav.opt.OptDigest;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.opt.OptGenconf;
import io.github.ag88.embtomcatwebdav.opt.OptGenpasswd;
import io.github.ag88.embtomcatwebdav.opt.OptHelp;
import io.github.ag88.embtomcatwebdav.opt.OptHost;
import io.github.ag88.embtomcatwebdav.opt.OptKeystoreFile;
import io.github.ag88.embtomcatwebdav.opt.OptKeystorePasswd;
import io.github.ag88.embtomcatwebdav.opt.OptPasswd;
import io.github.ag88.embtomcatwebdav.opt.OptPath;
import io.github.ag88.embtomcatwebdav.opt.OptPort;
import io.github.ag88.embtomcatwebdav.opt.OptQuiet;
import io.github.ag88.embtomcatwebdav.opt.OptRealm;
import io.github.ag88.embtomcatwebdav.opt.OptSecure;
import io.github.ag88.embtomcatwebdav.opt.OptUrlPrefix;
import io.github.ag88.embtomcatwebdav.opt.OptUser;
import io.github.ag88.embtomcatwebdav.servlet.CLResourceServlet;
import io.github.ag88.embtomcatwebdav.servlet.DLZipServlet;
import io.github.ag88.embtomcatwebdav.servlet.RedirServlet;
import io.github.ag88.embtomcatwebdav.servlet.WDavUploadServlet2;
import io.github.ag88.embtomcatwebdav.util.DigestPWUtil;

/**
 * This is a WebDAV server based on Apache Tomcat's WebDAV servlet and embedded Tomcat server.<p>
 * 
 * The parameters required to start the Tomcat server and WebDAV servlet are maintained as 
 * instance variables in this class.<p>
 * 
 * Normally, {@link App} class's  is the main entry point of this App. 
 * See {@link App} class for more detail.<p>
 * 
 * To use this class in an embedded application, first call the various getter/setter methods to setup
 * parameters for the app.<p>
 * 
 * Then call {@link #runserver()} which starts the Tomcat server/instance and host the WebDAV servlet.
 * 
 */
public class WebDavServer 
{
	
	/** The log. This isn't intended to be used externally.
	 * Note tha it uses Tomcat's JULI logging framework */
	private Log log = LogFactory.getLog(WebDavServer.class);
	
	/** The tomcat. */
	private Tomcat tomcat; 
	
	/** The keystorefile. used for SSL*/
	String keystorefile = null;
	
	/** The keystorepasswd. used for SSL*/
	String keystorepasswd = null;
		
	/** The port. default 8080*/
	int port = 8080;
	
	/** The host.
	 * This takes a string for the hostname, accordingly, the embedded Tomcat server 
	 * can resolve this as a IP address string as well, default localhost*/
	String shost = "localhost";
	
	
	/** urlprefix. default "/webdav" */
	String urlprefix = "/webdav";
	
	/** The path. default user.dir
	 * This is the path which is served. It is actually the context path.
	 * 
	 * */	
	String path = System.getProperty("user.dir");
	
	/** The basedir. 
	 * This directory/folder is used by tomcat for (temporary) files.
	 * e.g. used by JSP container, places server keystore file for SSL
	 * certificates etc. 
	 * 
	 * When runserver() starts, if this is null, it set it to the 
	 * default path [user.dir]/tomcat.port 
	 * */
	String basedir = null;
	
	/** Flag to set if DIGEST authentication is selected, 
	 * if it is false, it defaults to BASIC authentication */
	boolean digest = false;
	
	/** The Authentication realm for BASIC/DIGEST authentication,
	 *  default "Simple" */
	String realm = "Simple";
	
	/** The username.
	 *  If both username and passwords are specified, the server would authentication 
	 *  with BASIC authentication. To use DIGEST authentication, set digest to true as well*/
	String user = null;
	
	/** The password for the user.
	 * 
	 * For DIGEST authentication it can be stored in the format:
	 * digest(MD5(user:realm:password)) where MD5(user:realm:password) a hex string representing 
	 * the hashed value of MD5(user:realm:password). The wrapping syntax digest(xxxx) is used by 
	 * runserver to determine that xxxx is encoded and hence will not apply further encoding/hashing
	 * on it */
	String passwd = null;
	
	/** Quiet flag, reduce the amount of logs/info messages on running the embedded Tomcat Server */
	boolean quiet = false;
	
	/** 
	 * use upload servlet
	 */
	boolean uploadservlet = false;
	

	/** 
	 * DL zip path
	 */
	String dlzip_path = "/dlzip";

	
	/*
	 * enable access log 
	 */
	boolean accesslog = false;
	
	Map<String, Opt> m_opts;
	
	/**
	 * Instantiates a new web dav server, no arg constructor.	 * 
	 */
	public WebDavServer() {		
	}
		
	
	/**
	 * Runserver
	 * 
	 * This is the main method which starts the embedded Tomcat server and host the Webdav Servlet.<p>
	 * 
	 * Before calling this, first set the various parameters, e.g. port, host, path, basedir etc..
	 * Then call {@link #runserver()} which starts the embedded Tomcat server and host the Webdav Servlet.
	 * 
	 */
	public void runserver() {
		try {
			basedir = getDefbasedir(basedir);
			
			if(!quiet)
				log.info(String.format("tomcat basedir: %s", basedir));
			
			tomcat = new Tomcat();			
			tomcat.setBaseDir(basedir);
			long id = System.identityHashCode(tomcat);
			tomcat.getEngine().setName(tomcat.getEngine().getName()+"-"+id);
			tomcat.setSilent(quiet);
			if(keystorefile != null && keystorepasswd != null) { //enable SSL
				try {
					if(!Files.exists(Paths.get(basedir)))
						Files.createDirectory(Paths.get(basedir));
					Path tgt = Paths.get(basedir).resolve(keystorefile);
					Files.copy(Paths.get(keystorefile), tgt, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					log.error("cannot copy keystore file to tomcat basedir", e);
					System.exit(1);
				}				
				for(Connector c : tomcat.getService().findConnectors()) {
					tomcat.getService().removeConnector(c);
				}
				Connector c = new Connector();
			    c.setPort(port);
			    c.setSecure(true);
			    c.setScheme("https");
			    c.setProperty("keyAlias", "tomcat");
			    c.setProperty("keystorePass", keystorepasswd);
			    c.setProperty("keystoreType", "JKS");
			    c.setProperty("keystoreFile", keystorefile);
			    c.setProperty("clientAuth", "false");
			    c.setProperty("protocol", "HTTP/1.1");
			    c.setProperty("sslProtocol", "TLS");
			    c.setProperty("maxThreads", "10");
			    c.setProperty("protocol", "org.apache.coyote.http11.Http11NioProtocol");
			    c.setProperty("SSLEnabled", "true");
			    tomcat.getService().addConnector(c);			    
			} else {
				tomcat.setPort(port);
			}
			
			tomcat.setHostname(shost);
			

			if (shost.equals("0.0.0.0")) {
				if(!quiet)
					log.info("note host ".concat(shost).concat(" specified, listening on all interfaces"));
				
				tomcat.getHost().addAlias("localhost");
				if(!quiet)
					log.info("added host alias for: localhost");
				// add all the interface IP addresses
				
				try {
					Enumeration<NetworkInterface> interfaces; interfaces = NetworkInterface.getNetworkInterfaces();
					while(interfaces.hasMoreElements()) {
					    NetworkInterface networkInterface = interfaces.nextElement();
					    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
					    while(inetAddresses.hasMoreElements()) {
					    	InetAddress inetaddr = inetAddresses.nextElement();
					    	if(!quiet)
					    		log.info("added host alias for: ".concat(inetaddr.getHostAddress()));
					    	tomcat.getHost().addAlias(inetaddr.getHostAddress());
					    }
					}
				} catch (SocketException e) {
					log.warn(e);
				}
				
			}
			
						
			Thread hook = new Thread() {
				@Override
				public void run() {
					stopserver();
				}			
			};			
			Runtime.getRuntime().addShutdownHook(hook);
			
			Context context = tomcat.addContext("", path);
			tomcat.setAddDefaultWebXmlToWebapp(false);
			Tomcat.addDefaultMimeTypeMappings(context);		

			/*
	        if (context.getResources() == null) { 
	            if (log.isDebugEnabled()) {
	                log.debug("Configuring default Resources");
	            }

	            try {
	                context.setResources(new StandardRoot(context));
	                ((StandardContext) context).resourcesStart();
	            } catch (IllegalArgumentException e) {
	                log.error("standardContext.resourcesInit", e);
	            }
	        }
	        	        
			try {
				URL jarurl = Paths.get(App.getInstance().ownjarfile()).toUri().toURL();
				context.getResources().createWebResourceSet(ResourceSetType.RESOURCE_JAR, "/WEB-INF/ownjar",
					jarurl , "/");
			} catch (MalformedURLException e) {
				log.warn(e);
			}
			*/			
			
			if(!quiet) 
				log.info(String.format("serving path: %s", path));

			if (user != null) {
				Realm realmo = tomcat.getEngine().getRealm();
				if (digest) {
					MessageDigestCredentialHandler credhand = new MessageDigestCredentialHandler();
					credhand.setSaltLength(0);
					credhand.setEncoding("UTF-8");
					credhand.setIterations(1);
					try {
						credhand.setAlgorithm("MD5");
					} catch (NoSuchAlgorithmException e1) {
						log.error("unable to set cred handler algorithm", e1);
						System.exit(1);
					}
					realmo.setCredentialHandler(credhand);
					tomcat.getEngine().setRealm(realmo);
				}
				context.setRealm(realmo);
				if (digest) {
					try {
						DigestPWUtil pwutil = new DigestPWUtil();						
						passwd = pwutil.digestPw(realm, user, passwd);
						tomcat.addUser(user, passwd);
					} catch (NoSuchAlgorithmException e) {
						log.error("unable to encode digest passwd", e);
						System.exit(1);
					}					
				} else {
					tomcat.addUser(user, passwd);
				}
				tomcat.addRole(user, "user");
				LoginConfig lconf = new LoginConfig();
				lconf.setRealmName(realm);
				if(!quiet) log.info("Realm name: ".concat(realm));
				// NONE, BASIC, DIGEST, FORM, or CLIENT-CERT.
				if(digest) {
					lconf.setAuthMethod("DIGEST");					
				} else				
					lconf.setAuthMethod("BASIC");
				if(!quiet) log.info("Auth method: ".concat(lconf.getAuthMethod()));
				lconf.setCharset(Charset.forName("UTF-8"));
				context.setLoginConfig(lconf);
				if(digest) {
					DigestAuthenticator auth = new DigestAuthenticator();
					if(uploadservlet)
						auth.setAlwaysUseSession(true);
					context.getPipeline().addValve(auth);
				} else {
					BasicAuthenticator auth = new BasicAuthenticator();
					if(uploadservlet)
						auth.setAlwaysUseSession(true);
					context.getPipeline().addValve(auth);					
				}
				
				SecurityConstraint secconstr = new SecurityConstraint();
				secconstr.addAuthRole("user");
				secconstr.setAuthConstraint(true);
				SecurityCollection sc = new SecurityCollection();
				//sc.addPattern("/*");
				String pat = urlprefix;
				if(! pat.endsWith("/"))
					pat = pat.concat("/");
				pat = pat.concat("*");
				sc.addPattern(pat);
				if(!(null == dlzip_path || dlzip_path.equals("")))
					sc.addPattern(dlzip_path);				
				secconstr.addCollection(sc);
				context.addConstraint(secconstr);				
			}			
			
			if(accesslog) {
				AccessLogValve aclogv = new AccessLogValve();
				OptAccesslogDir optdir = (OptAccesslogDir) m_opts.get("accesslog.dir");
				if(optdir != null && optdir.isvalid(optdir.getValue()))
					aclogv.setDirectory((String) optdir.getValue());
				else
					aclogv.setDirectory(basedir);
				aclogv.setPattern("common");
				OptAccesslogRot optrot = (OptAccesslogRot) m_opts.get("accesslog.rot");
				if(optrot != null)
					aclogv.setRotatable((Boolean) optrot.getValue()); 
				OptAccesslogDays optdays = (OptAccesslogDays) m_opts.get("accesslog.days");
				if(optdays != null)
					aclogv.setMaxDays((Integer) optdays.getValue());;
				context.getPipeline().addValve(aclogv);
				if(!quiet) {
					log.info("access log dir:" + aclogv.getDirectory());
					log.info("access log rotatable:" + aclogv.isRotatable());
					log.info("access log max days:" + aclogv.getMaxDays());
				}
			}
			
			Servlet servlet;
			if(uploadservlet) {
				servlet = (Servlet) new WDavUploadServlet2();
				if(!quiet)
					log.info("running Upload servlet");
			} else
				servlet = (Servlet) new WebdavServlet();
			
			Wrapper ws = Tomcat.addServlet(context, "webdav", servlet);			
			ws.addInitParameter("debug", "0");
			ws.addInitParameter("listings", "true");
			ws.addInitParameter("sortListings", "true");
		    ws.addInitParameter("readonly", "false");
			ws.addInitParameter("allowSpecialPaths", "true");
			
			String urlprefix1;
			if (! urlprefix.endsWith("/"))
				urlprefix1 = urlprefix.concat("/*");
			else
				urlprefix1 = urlprefix.concat("*");
						
			context.addServletMappingDecoded(urlprefix1, "webdav");
			
			if(uploadservlet) { 
				//register the resource servlet
				servlet = new CLResourceServlet();
				ws = Tomcat.addServlet(context, "clres", servlet);
				
				context.addServletMappingDecoded("/res/*", "clres");
				
				/* if webdav is not at root '/' redirect to the webdav 
				 * url prefix 
				 */
				if (!(urlprefix.equals("") || urlprefix.equals("/"))) {
					servlet = new RedirServlet(urlprefix);
					Tomcat.addServlet(context, "redir", servlet);
					context.addServletMappingDecoded("/", "redir");					
				}
				
				servlet = new DLZipServlet();
				if(!quiet)
					log.info("dlzip_path:".concat(dlzip_path));
				Tomcat.addServlet(context, "dlzip", servlet);
				context.addServletMappingDecoded(dlzip_path, "dlzip");
				
			}
						
			context.setSessionTimeout(30);
			
			if(!quiet)
				log.info(String.format("Webdav servlet running at %s://%s:%s%s", 
					(keystorefile!=null && keystorepasswd != null) ? "https" : "http", 
					shost, Integer.toString(port), urlprefix.substring(0,urlprefix.length())));
			
			tomcat.start();
			
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			log.error(e.getMessage(), e);
		}		
	}
	    
    
    /**
     * Runs server in its own background thread.
     * 
     * if it is intrrupted, the server likely did not startup appropriately.
     *
     * @return the server thread
     * @throws InterruptedException the interrupted exception
     */
    public WebDAVServerThread runserverfork() throws InterruptedException {    	
    	
    	// Run the server in its own thread
    	WebDAVServerThread thread = new WebDAVServerThread("WebDAVServer", this);
    	thread.start();
    	
    	//let the server startup
    	Thread.sleep(20);
    	
    	//check that it is fully started
		while(!isRunning());
			Thread.sleep(1);
    	    	
    	return thread;
    }
	
	/**
	 * Gets default basedir, Tomcat's work folder 
	 *
	 * default [usr.dir]/tomcat.port
	 *
	 * @param basedir the current base dir
	 * @return the default base directory 
	 */
	public String getDefbasedir(String basedir) {
		if(basedir == null)
			basedir = Paths.get(System.getProperty("user.dir"), 
				"tomcat.".concat(Integer.toString(port))).toString();
		return basedir;
	}
	
	/**
	 * Checks if is running.
	 *
	 * @return true, if is running
	 */
	public boolean isRunning() {
		if(tomcat == null)
			return false;
		Server server = tomcat.getServer();
		if(server == null)
			return false;
		Service[] services = server.findServices();
		if(services == null || services.length == 0)
			return false;
		Service service = services[0];
		if (service == null)
			return false;
		LifecycleState state = service.getState();
		if(state == null)
			return false;
		
		return state.equals(LifecycleState.STARTED);
	}
	
	/**
	 * Stop server
	 */
	public void stopserver() {
		try {
			if(isRunning()) {
				tomcat.stop();
				tomcat.destroy();
			}
		} catch (LifecycleException e) {
			log.error(e.getMessage(), e);
		}
	}

	
	/**
	 * Load params from a Map of options parsed from command line and config files
	 *
	 * @param opts the opts
	 */
	public void loadparams(Map<String, Opt> opts) {
		
		this.shost = (String) opts.get("host").getValue();
		this.port = ((Integer) opts.get("port").getValue()).intValue();
		this.path =  (String) opts.get("path").getValue();
		this.basedir =  (String) opts.get("basedir").getValue();
		this.urlprefix = (String) opts.get("urlprefix").getValue();
		if(this.urlprefix == null)
			this.urlprefix = (String) opts.get("urlprefix").getDefaultval();
		this.realm = (String) opts.get("realm").getValue();
		this.user = (String) opts.get("user").getValue();
		this.passwd = (String) opts.get("password").getValue();
		this.digest = ((Boolean) opts.get("digest").getValue()).booleanValue();		
		
		this.keystorefile = (String) opts.get("keystorefile").getValue();
		this.keystorepasswd = (String) opts.get("keystorepasswd").getValue();
		this.quiet = ((Boolean) opts.get("quiet").getValue()).booleanValue();
		this.uploadservlet = ((Boolean) opts.get("uploadservlet").getValue()).booleanValue();
		String dlzippath = (String) opts.get("dlzip_path").getValue();
		if (null == dlzippath || dlzippath.equals(""))
			this.dlzip_path = (String) opts.get("dlzip_path").getDefaultval();
		else
			this.dlzip_path = dlzippath;
		this.accesslog = ((Boolean) opts.get("accesslog").getValue()).booleanValue();
		
		m_opts = opts;
				
	}
		
	/**
	 * Gets the port.
	 * 
	 * Default 8080
	 *
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 * 
	 * This takes a string for the hostname, accordingly, the embedded Tomcat server 
	 * can resolve this as a IP address string as well, default localhost
	 * 
	 * @param port the new port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Gets the host as a string.
	 *
	 * @return the host
	 */
	public String getHost() {
		return shost;
	}

	/**
	 * Sets the host.
	 * 
	 * This takes a string for the hostname, accordingly, the embedded Tomcat server 
	 * can resolve this as a IP address string as well, default localhost
	 * 
	 * @param shost the new shost
	 */
	public void setHost(String shost) {
		this.shost = shost;
	}

	/**
	 * Gets the path, this is the path which is served, default user.dir.
	 *  
	 * It is actually the context path. 
	 *
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the path, this is the path which is served, default user.dir.
	 * 
	 * It is actually the context path.
	 * 
	 * @param path the new path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Gets the basedir, used by tomcat.
	 * 
	 * This directory/folder is used by tomcat for (temporary) files.
	 * e.g. used by JSP container, places server keystore file for SSL
	 * certificates etc.<p> 
	 * 
	 * When runserver() starts, if this is null, it set it to the 
	 * default path [user.dir]/tomcat.port 
	 * 
	 * @return the basedir
	 */
	public String getBasedir() {
		return basedir;
	}

	/**
	 * Sets the basedir, used by tomcat.
	 * 
	 * This directory/folder is used by tomcat for (temporary) files.
	 * e.g. used by JSP container, places server keystore file for SSL
	 * certificates etc. <p> 
	 * 
	 * When runserver() starts, if this is null, it set it to the 
	 * default path [user.dir]/tomcat.port
	 *  
	 * @param basedir the new basedir
	 */
	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	/**
	 * Gets the username.
	 *
	 * If both username and passwords are specified, the server would authenticate
	 * with BASIC authentication. To use DIGEST authentication, set digest to true as well
	 *
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the username.
	 * 
	 * If both username and passwords are specified, the server would authenticate
	 * with BASIC authentication. To use DIGEST authentication, set digest to true as well
	 *
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Gets the password.
	 * 
	 * For DIGEST authentication it can be stored in the format:<br>
	 * digest(MD5(user:realm:password))<br>
	 * where MD5(user:realm:password) a hex string representing the hashed value of 
	 * MD5(user:realm:password).<p>
	 * 
	 * The wrapping syntax digest(xxxx) is used by runserver to determine that xxxx is encoded 
	 * and hence will not apply further encoding/hashing on it
	 * 
	 * @return the password
	 */
	public String getPasswd() {
		return passwd;
	}

	/**
	 * Sets the password.
	 * 
	 * For DIGEST authentication it can be stored in the format:<br>
	 * digest(MD5(user:realm:password))<br>
	 * where MD5(user:realm:password) a hex string representing the hashed value of 
	 * MD5(user:realm:password).<p>
	 * 
	 * The wrapping syntax digest(xxxx) is used by runserver to determine that xxxx is encoded 
	 * and hence will not apply further encoding/hashing on it
	 *
	 * @param passwd the new passwd
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	/**
	 * Checks if is quiet.
	 * 
	 * reduce the amount of logs/info messages on running the embedded Tomcat Server
	 *
	 * @return true, if is quiet
	 */
	public boolean isQuiet() {
		return quiet;
	}

	/**
	 * Sets the quiet flag.
	 * 
	 * reduce the amount of logs/info messages on running the embedded Tomcat Server
	 *
	 * @param quiet quiet flag
	 */
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}
	
	/**
	 * Gets the keystorefile, used for SSL.
	 *
	 * @return the keystorefile
	 */
	public String getKeystorefile() {
		return keystorefile;
	}

	/**
	 * Sets the keystorefile, used for SSL.
	 *
	 * @param keystorefile the new keystorefile
	 */
	public void setKeystorefile(String keystorefile) {
		this.keystorefile = keystorefile;
	}

	/**
	 * Gets the keystorepasswd, used for SSL.
	 *
	 * @return the keystorepasswd
	 */
	public String getKeystorepasswd() {
		return keystorepasswd;
	}

	/**
	 * Sets the keystorepasswd, used for SSL.
	 *
	 * @param keystorepasswd the new keystorepasswd
	 */
	public void setKeystorepasswd(String keystorepasswd) {
		this.keystorepasswd = keystorepasswd;
	}
	
	/**
	 * Checks if is digest.
	 *
	 * Flag to set if DIGEST authentication is selected.
	 * If it is false, it defaults to BASIC authentication
	 * 
	 * @return true, if is digest
	 */
	public boolean isDigest() {
		return digest;
	}

	/**
	 * Sets the digest flag
	 * 
	 * Flag to set if DIGEST authentication is selected.
	 * If it is false, it defaults to BASIC authentication
	 *
	 * @param digest the new digest flag
	 */
	public void setDigest(boolean digest) {
		this.digest = digest;
	}

	
	/**
	 * Gets the authentication realm, for BASIC/DIGEST authentication
	 *
	 * @return the realm
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * Sets the authentication realm, for BASIC/DIGEST authentication.
	 *
	 * @param realm the new realm
	 */
	public void setRealm(String realm) {
		this.realm = realm;
	}

	/**
	 * Gets the urlprefix.
	 * 
	 * default "/webdav"
	 *  
	 * @return the urlprefix
	 */
	public String getUrlprefix() {
		return urlprefix;
	}

	/**
	 * Sets the urlprefix.
	 * 
	 * default "/webdav"
	 *
	 * @param urlprefix the new urlprefix
	 */
	public void setUrlprefix(String urlprefix) {
		this.urlprefix = urlprefix;
	}

	/**
	 * Checks if is uploadservlet.
	 *
	 * @return true, if is uploadservlet
	 */
	public boolean isUploadservlet() {
		return uploadservlet;
	}

	/**
	 * Sets uploadservlet, true = use upload servlet
	 *
	 * @param uploadservlet the new uploadservlet
	 */
	public void setUploadservlet(boolean uploadservlet) {
		this.uploadservlet = uploadservlet;
	}

	
		
	/**
	 * Checks if accesslog is enabled.
	 *
	 * @return true, if accesslog is enabled
	 */
	public boolean isAccesslog() {
		return accesslog;
	}


	/**
	 * Sets accesslog, true = accesslog enabled
	 *
	 * @param accesslog flag enable/disable accesslog
	 */

	public void setAccesslog(boolean accesslog) {
		this.accesslog = accesslog;
	}


	/**
	 * Gets the opts.
	 * 
	 * the opts is normally set by {@link loadparams}
	 * 
	 * @return the opts
	 */
	public Map<String, Opt> getOpts() {
		return m_opts;
	}


	/**
	 * Sets the opts.
	 *
	 * normally do not use this, the opts is set by {@link loadparams}
	 * 
	 * @param opts the opts
	 */
	public void setOpts(Map<String, Opt> opts) {
		this.m_opts = opts;
	}


	/**
     * Gets the embedded tomcat instance (Tomcat class)
     *
     * @return the tomcat
     */
    public Tomcat getTomcat() {
		return tomcat;
	}

	/**
	 * Sets the embedded tomcat instance (Tomcat class)
	 * 
	 * Normally the embedded Tomcat object should be setup by calling {@link #runserver()}.
	 * One should know what one is doing while calling this ;)
	 *
	 * @param tomcat the new tomcat
	 */
	public void setTomcat(Tomcat tomcat) {
		this.tomcat = tomcat;
	}
		


}
