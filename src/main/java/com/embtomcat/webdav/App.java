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


package com.embtomcat.webdav;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;
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

public class App 
{
	
	Log log = LogFactory.getLog(App.class);
	
	Tomcat tomcat; 
	
	String keystorefile = null;
	String keystorepasswd = null;
	
	int port = 8080;
	String shost = "localhost";
	String path = System.getProperty("user.dir");
	String basedir = null;
	boolean digest = false;
	String realm = "Simple";
	String user = null;
	String passwd = null;
	boolean quiet = false;
	
	public App() {
	}
		
	public void run(String[] args) {
		parseargs(args);
		runserver();
	}
	
	public void runserver() {
		try {
			if(basedir == null)
				basedir = Paths.get(System.getProperty("user.dir"), 
					"tomcat.".concat(Integer.toString(port))).toString();
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
		options.addOption(Option.builder("R").longOpt("realm")
				.desc("set realm name, default 'Simple'")
				.hasArg().argName("realmname").build());
		options.addOption(Option.builder("q").longOpt("quiet").desc("mute (most) logs").build());
		options.addOption(Option.builder("D").longOpt("digest").desc("use digest authentication").build());
		options.addOption(Option.builder("S").longOpt("secure")
				.desc("enable SSL, you need to supply a keystore file and keystore passwd, " +
		          "if passwd is omitted it'd be prompted.")
				.hasArg().argName("keystore,passwd").build());

		
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);

			if(cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				Map<String, String> mkv = readManifest();
				String name = mkv.get("artifactId")
						.concat("-").concat(mkv.get("version"));
				formatter.printHelp(name, options);
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
				Scanner input = new Scanner(System.in);
				passwd = input.nextLine();
				input.close();
			}
			
			if (cmd.hasOption("secure")) {
				String arg = cmd.getOptionValue("secure");
				if (arg.contains(",")) {
					String[] f = arg.split(",");
					keystorefile = f[0];
					keystorepasswd = f[1];
				} else {
					keystorefile = arg;
					log.info("Enter password for keystore:");
					Scanner s = new Scanner(System.in);
					keystorepasswd = s.nextLine();
					s.close();
				}
				if(!Files.exists(Paths.get(keystorefile))) {
					log.error("keystore file not found!");
					System.exit(0);
				}
			}

			if(cmd.hasOption("digest")) {
				digest = true;
			}
			
			if(cmd.hasOption("quiet")) {
				quiet = true;
			}
						
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
		
	}	
	
	
	/**
	 * Generates enncoded password for DIGEST authentication
	 * 
	 * @param realm
	 * @param username
	 * @param password plaintext password
	 * @return encoded password for DIGEST authentication
	 * @throws NoSuchAlgorithmException 
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
	 * returns an encoded (hashed) password for digest auth for storage
	 * 
	 * not safe, but hashed so as to obfuscate the original password
	 * 
	 * @param realm
	 * @param username
	 * @param password
	 * @return encoded password for text storage
	 * @throws NoSuchAlgorithmException 
	 */
	public String digestEncodeStoredPw(String realm, String username, String password)
			throws NoSuchAlgorithmException {
		String epw = digestEncodePasswd(realm, username, password);
		return "digest(".concat(epw).concat(")");
	}
	
	
	/**
	 * returns password encoding for digest authentication
	 * i.e. MD5(username:realm:password)
	 * 
	 * if password is in format "digest(hexstring)", it is deemed pre-encoded and returned
	 * 
	 * @param realm
	 * @param username
	 * @param password
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	public String digestPw(String realm, String username, String password) throws NoSuchAlgorithmException {
		String dpw = null;
		Pattern p = Pattern.compile("digest\\(.*\\)");
		Matcher m = p.matcher(password); 
		if(m.matches()) {
			dpw = m.group(1);
		} else 
			dpw = digestEncodePasswd(realm, username, password);
		return dpw;
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
		
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getShost() {
		return shost;
	}

	public void setShost(String shost) {
		this.shost = shost;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}
	
	public String getKeystorefile() {
		return keystorefile;
	}

	public void setKeystorefile(String keystorefile) {
		this.keystorefile = keystorefile;
	}

	public String getKeystorepasswd() {
		return keystorepasswd;
	}

	public void setKeystorepasswd(String keystorepasswd) {
		this.keystorepasswd = keystorepasswd;
	}
	
	public boolean isDigest() {
		return digest;
	}

	public void setDigest(boolean digest) {
		this.digest = digest;
	}

	
	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

    public static void main(String[] args)  {
        App app = new App();
        app.run(args);
    }

}
