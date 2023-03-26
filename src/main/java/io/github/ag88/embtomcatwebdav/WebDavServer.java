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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.BasicAuthenticator;
import org.apache.catalina.authenticator.DigestAuthenticator;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
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


/**
 * This is a WebDAV server based on Apache Tomcat's WebDAV servlet and embedded Tomcat server.<p>
 * 
 * The parameters required to start the Tomcat server and WebDAV servlet are maintained as 
 * instance variables in this class.<p>
 * 
 * Normally, this class's {@link #main(String[])} is the main entry point of this App.<br>
 * {@link #main(String[])} in turns calls {@link #run(String[])} which in turns calls a private method
 * to process command line parameters, and when done, calls {@link #runserver()} which starts the Tomcat
 * server/instance and host the WebDAV servlet.<p>
 * 
 * To use this class as an embedded application, first call the various getter/setter methods to setup
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
	 * Instantiates a new web dav server, no arg constructor.
	 */
	public WebDavServer() {
	}
		
	/**
	 * Run
	 * 
	 * This method is actually called by {@link #main(String[])}.
	 * It calls a method to parse the command line variables and setup the instance variables.
	 * It then calls {@link #runserver()} to start the embedded Tomcat server
	 *
	 * @param args the args
	 */
	public void run(String[] args) {
		parseargs(args);
		runserver();
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
						passwd = digestPw(realm, user, passwd);
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
				if(digest) 
					context.getPipeline().addValve(new DigestAuthenticator());
				else				
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

			if (! urlprefix.endsWith("/"))
				urlprefix = urlprefix.concat("/*");
			else
				urlprefix = urlprefix.concat("*");
			
			context.addServletMappingDecoded(urlprefix, "webdav");
			context.setSessionTimeout(30);
			
			if(!quiet)
				log.info(String.format("Webdav servlet running at %s://%s:%s%s", 
					(keystorefile!=null && keystorepasswd != null) ? "https" : "http", 
					shost, Integer.toString(port), urlprefix.substring(0,urlprefix.length()-1)));

			tomcat.start();
			tomcat.getServer().await();
		} catch (LifecycleException e) {
			log.error(e.getMessage(), e);
		}		
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
	 * Parseargs.
	 *
	 * @param args command line args passed to {@link #main(String[])}
	 */
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
		options.addOption(Option.builder("x").longOpt("urlprefix")
				.desc("set urlprefix, default /webdav")
				.hasArg().argName("urlprefix").build());
		options.addOption(Option.builder("b").longOpt("basedir")
				.desc("set basedir, a work folder for tomcat, default [current working dir]/tomcat.port")
				.hasArg().argName("path").build());
		options.addOption(Option.builder("u").longOpt("user")
				.desc("set user")
				.hasArg().argName("username").build());
		options.addOption(Option.builder("w").longOpt("passwd")
				.desc("set password, you may omit this, it would prompt for it if -u is specified")
				.hasArg().argName("password").build());
		options.addOption(Option.builder("R").longOpt("realm")
				.desc("set realm name, default 'Simple'")
				.hasArg().argName("realmname").build());
		options.addOption(Option.builder("q").longOpt("quiet").desc("mute (most) logs").build());
		options.addOption(Option.builder("D").longOpt("digest").desc("use digest authentication").build());
		options.addOption(Option.builder("S").longOpt("secure")
				.desc("enable SSL, you need to supply a keystore file and keystore passwd, " +
		          "if passwd is omitted it'd be prompted.")
				.hasArg().argName("keystore,passwd").build());
		options.addOption(Option.builder("c").longOpt("conf")
				.desc("load properties config file")
				.hasArg().argName("configfile").build());
		options.addOption(Option.builder().longOpt("genconf")
				.desc("generate properties config file")
				.hasArg().argName("configfile").build());
		options.addOption(Option.builder().longOpt("genpass")
				.desc("dialog to generate digest password").build());
		
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			Scanner scanner = new Scanner(System.in);

			if(cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				Map<String, String> mkv = readManifest();
				String name = mkv.get("artifactId")
						.concat("-").concat(mkv.get("version"));
				formatter.printHelp(name, options);
				System.exit(0);
			}

			if (cmd.hasOption("conf")) {
				String configfile = cmd.getOptionValue("conf");
				loadconfigprop(configfile);
			} 
			
			if (cmd.hasOption("genconf")) {
				String configfile = cmd.getOptionValue("genconf");
				genconfigprop(configfile);
			} 
			
			if (cmd.hasOption("genpass")) {
				DigestPWGenDlg dlg = new DigestPWGenDlg(this);
				dlg.pack();
				dlg.setLocationRelativeTo(null);
				dlg.setVisible(true);
				System.exit(0);
			} 
			
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
			
			if (cmd.hasOption("urlprefix")) {
				urlprefix = cmd.getOptionValue("urlprefix");
			}
			
			if (cmd.hasOption("realm")) {
				realm = cmd.getOptionValue("realm");
			} 
			
			if (cmd.hasOption("user")) {
				user = cmd.getOptionValue("user");
			} 
			
			if (cmd.hasOption("passwd")) {
				passwd = cmd.getOptionValue("passwd");
			}

			if (user != null && passwd == null) {
				log.info(String.format("enter password for %s:", user));
				passwd = scanner.nextLine();
			}
			
			if (cmd.hasOption("secure")) {
				String arg = cmd.getOptionValue("secure");
				if (arg.contains(",")) {
					String[] f = arg.split(",");
					keystorefile = f[0];
					keystorepasswd = f[1];
				} else {
					keystorefile = arg;
				}
			}
			
			if(keystorefile != null && keystorepasswd == null) {
				log.info("Enter password for keystore:");
				keystorepasswd = scanner.nextLine();
			}
			
			if(keystorefile != null && !Files.exists(Paths.get(keystorefile))) {
				log.error("keystore file not found!");
				System.exit(0);
			}
			
			if(cmd.hasOption("digest")) {
				digest = true;
			}
			
			if(cmd.hasOption("quiet")) {
				quiet = true;
			}
			
			scanner.close();
						
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
		
	}	
	
	/**
	 * Load config options from properties file.
	 *
	 * @param configfile this should be a properties text file in the appropriate format
	 * e.g. generated using {@link #genconfigprop(String)}
	 */
	public void loadconfigprop(String configfile) {				
		try {
			Properties p = new Properties();
			BufferedReader reader = new BufferedReader(new FileReader(configfile));
			p.load(reader);
			
			shost = p.getProperty("host", shost);
			port = Integer.parseInt(p.getProperty("port", "8080"));
			urlprefix = p.getProperty("urlprefix", "/webdav");
			path = p.getProperty("path", System.getProperty("user.dir"));
			basedir = p.getProperty("basedir", getDefbasedir(basedir));
			digest = Boolean.parseBoolean(p.getProperty("digest", "false"));
			realm = p.getProperty("realm", "Simple");
			user = p.getProperty("user", "");
			if (user.equals("")) user = null;
			passwd = p.getProperty("password", "");
			if (passwd.equals("")) passwd = null;
			quiet = Boolean.parseBoolean(p.getProperty("quiet", "false"));
			
			keystorefile = p.getProperty("keystorefile", "");
			if (keystorefile.equals("")) keystorefile = null;
			keystorepasswd = p.getProperty("keystorepasswd", "");
			if (keystorepasswd.equals("")) keystorepasswd = null;
			
		} catch (FileNotFoundException e) {
			log.error(String.format("config file %s not found %s", configfile), e);
			System.exit(1);
		} catch (IOException e) {
			log.error(String.format("config file %s not found %s", configfile), e);
			System.exit(1);
		}		
	}
	
	/**
	 * Generate config options properties file template.
	 * 
	 * It would fill up some default values
	 *
	 * @param configfile new properties config file name.
	 */
	public void genconfigprop(String configfile) {
		if(Files.exists(Paths.get(configfile))) {
			log.error("file exists, not overwriting, specify a new name");
			System.exit(1);
		}
		
		basedir = getDefbasedir(basedir);
		
		Properties p = new Properties();
		p.setProperty("host", shost);
		p.setProperty("port", Integer.toString(port));
		p.setProperty("urlprefix", urlprefix);
		p.setProperty("path", path == null ? "" : path);
		p.setProperty("basedir", basedir == null ? "" : basedir);
		p.setProperty("digest", Boolean.toString(digest));
		p.setProperty("realm", realm);
		p.setProperty("user", user == null ? "" : user);
		p.setProperty("password", passwd == null ? "" : passwd);
		p.setProperty("quiet", Boolean.toString(quiet));		
		p.setProperty("keystorefile", keystorefile == null ? "" : keystorefile);
		p.setProperty("keystorepasswd", keystorepasswd == null ? "" : keystorepasswd);
		
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configfile));
			p.store(writer, "Embedded Tomcat Webdav server properties");
			writer.flush();
			writer.close();
			
			log.info("config file saveed to ".concat(configfile));
			System.exit(0);
		} catch (IOException e) {
			log.error(String.format("unable to write config file %s",configfile), e);
			System.exit(1);
		}		
		
	}


	/**
	 * Generates encoded password for DIGEST authentication.
	 *
	 * @param realm the realm
	 * @param username the username
	 * @param password plaintext password
	 * @return encoded password for DIGEST authentication
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String digestEncodePasswd(String realm, String username, String password) 
			throws NoSuchAlgorithmException {
		
			String credentials = username.concat(":").concat(realm).concat(":").concat(password);
			MessageDigestCredentialHandler credhand = new MessageDigestCredentialHandler();
			credhand.setEncoding(StandardCharsets.UTF_8.name());
			credhand.setAlgorithm("MD5");
			credhand.setIterations(1);
			credhand.setSaltLength(0);
			return credhand.mutate(credentials);		
	}
	
	/**
	 *  
	 * returns an encoded (hashed) password for digest auth for storage<p>
	 * 
	 * not safe, but hashed so as to obfuscate the original password.
	 *
	 * @param realm Authentication Realm (for BASIC/DIGEST Authentication)
	 * @param username username
	 * @param password password
	 * @return encoded password for text storage
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String digestEncodeStoredPw(String realm, String username, String password)
			throws NoSuchAlgorithmException {
		String epw = digestEncodePasswd(realm, username, password);
		return "digest(".concat(epw).concat(")");
	}
	
	
	/**
	 * returns password encoded for digest authentication as a hex string<br>
	 * i.e. MD5(username:realm:password)<p>
	 * 
	 * if password is in format "digest(hexstring)", it is deemed pre-encoded and returned
	 *
	 * @param realm Authentication Realm (for BASIC/DIGEST Authentication)
	 * @param username username
	 * @param password password
	 * @return encoded password i.e. MD5(username:realm:password) as a HexString
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String digestPw(String realm, String username, String password) throws NoSuchAlgorithmException {
		String dpw = null;
		Pattern p = Pattern.compile("digest\\((.*)\\)");
		Matcher m = p.matcher(password); 
		if(m.matches()) {
			dpw = m.group(1);
		} else 
			dpw = digestEncodePasswd(realm, username, password);
		return dpw;
	}
	
	/**
	 * Read manifest.
	 *
	 * @return the map
	 */
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
	public void setShost(String shost) {
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
     * The main method, starting point of this app.
     * 
     * As this is mainly an App, this is the main entry point to start the App.
     *
     * @param args the arguments
     */
    public static void main(String[] args)  {
        WebDavServer app = new WebDavServer();
        app.run(args);
    }


}
