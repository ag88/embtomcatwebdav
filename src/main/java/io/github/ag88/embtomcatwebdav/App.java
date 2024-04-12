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

import java.io.Console;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.opt.Opt;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;

/**
 * This is a WebDAV server based on Apache Tomcat's WebDAV servlet and embedded Tomcat server.<p>
 * 
 * The parameters required to start the Tomcat server and WebDAV servlet are maintained as 
 * instance variables in {@link WebDavServer} class.<p>
 * 
 * Normally, this class's {@link #main(String[])} is the main entry point of this App.<br>
 * {@link #main(String[])} in turns calls {@link #run(String[])} which in turns calls 
 * {@link #parseargs(String[])} to process command line parameters, 
 * after that call {@link WebDavServer#loadparams(Map)} to setup the instance variables 
 * and when done, calls {@link WebDavServer#runserver()} which starts the Tomcat
 * server/instance and host the WebDAV servlet.<p>
 * 
 * Note that this class initiates the {@link OptFactory} instance. 
 * In {@link #parseargs(String[])}, it parses the command line arguments as well as load
 * properties from a configfile (a java properties file).<br>
 * It loads a properties file if --conf configfile option is specified on the command line.<br>
 * This class also update the instance variables/objects in the {@link OptFactory} instance 
 * via {@link OptFactory#setApp(App)} and {@link OptFactory#setWebDAVserv(WebDavServer)} 
 *  
 */
public class App {
	
	private Log log = LogFactory.getLog(App.class);
	
	WebDavServer wdav;

	private static App m_instance;
	
	public App() {
		if(m_instance == null)
			m_instance = this;

		wdav = new WebDavServer();
		OptFactory.getInstance().registeropts();
		OptFactory.getInstance().setWebDAVserv(wdav);
		OptFactory.getInstance().setApp(this);		
	}
	
	public static App getInstance() {
		return m_instance;			
	}
	
	
	/**
	 * Run
	 * 
	 * This method is actually called by {@link #main(String[])}.
	 * It calls a method to parse the command line variables and call {@link WebDavServer#loadparams(Map)}
	 * to setup the instance variables. It then calls {@link WebDavServer#runserver()} 
	 * to start the embedded Tomcat server
	 *
	 * @param args the args
	 */
	public void run(String[] args) {

		GitCheckUpdates.getInstance().hasUpdates();

		parseargs(args);
		
		//OptFactory.getInstance().printOpts();
		wdav.loadparams(OptFactory.getInstance().getOpts());						
		wdav.runserver();
	}

	/**
	 * Parse args.
	 *
	 * @param args command line args passed to {@link #main(String[])}
	 */
	public void parseargs(String[] args) {
		
		Options options = new Options();
		OptFactory.getInstance().genoptions(options);		
				
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			Iterator<Opt> iter = OptFactory.getInstance().iterator();
			
			while (iter.hasNext()) {
				Opt opt = iter.next();
				if (opt.getType().equals(Opt.PropType.Norm) || opt.getType().equals(Opt.PropType.CLI)) {

					if (cmd.hasOption(opt.getLongopt())) {
						if (opt.isCmdproc()) {
							// passes process() 3 objects
							// Object[0] this App object
							// Object[1] the linked	WebDavServer object
							// Object[2] Options object for cmdline
							opt.process(cmd, this, wdav, options);
						} else {
							if (opt.isHasarg()) {
								String sval = cmd.getOptionValue(opt.getLongopt());
								
								if (opt.isValidate()) {
									if(!opt.isvalid(sval)) {
										if(opt.isReplace()) {
											Object value = opt.replace(sval);
											log.error(String.format("opt: %s, invalid value: %s, replaced with: %s",
													opt.getName(), sval, value));
											opt.setValue(value);
											continue;
										} else {
											log.error(String.format("opt: %s, invalid value: %s, skipped",
													opt.getName(), sval));
											continue;
										}
									} // else valid fall through 						
								}
								
								Object value;
								if (opt.getValclass().equals(Integer.class)) {
									try {
										value = Integer.parseInt(sval);
									} catch (NumberFormatException e) {
										log.warn(String.format("opt: %s, invalid value: %s, using default: %s",
												opt.getName(), sval, opt.getDefaultval()));
										value = opt.getDefaultval();
									}
								} else if (opt.getValclass().equals(Boolean.class)) {
									try {
										value = Boolean.parseBoolean(sval);
									} catch (Exception e) {
										log.warn(String.format("opt: %s, invalid value: %s, using default: %s",
												opt.getName(), sval, opt.getDefaultval()));
										value = opt.getDefaultval();
									}
								} else // string
									value = cmd.getOptionValue(opt.getLongopt());

								opt.setValue(value);
							} else { // no arg, a flag
								if(opt.getValclass().equals(Boolean.class))
									if(opt.getValue() == null)
										opt.setValue(Boolean.valueOf(true));
									else {
										opt.setValue(!((Boolean) opt.getValue()));
									}
							}
						}
					}
				}
			}
			
			/* 
			 * prompt for missing passwords
			 */
			Scanner scanner = new Scanner(new FilterInputStream(System.in) {
			    @Override
			    public void close() throws IOException {
			        //don't close System.in! 
			    }
			});
			Console console = System.console();
			
			String user = (String) OptFactory.getInstance().getOpt("user").getValue();
			String passwd = (String) OptFactory.getInstance().getOpt("password").getValue();
			if (user != null && passwd == null ) {
				log.info(String.format("enter password for %s:", user));
				if(console != null)
					passwd = new String(console.readPassword());
				else
					passwd = scanner.nextLine();
				OptFactory.getInstance().getOpt("password").setValue(passwd);
			}
			
			String keystorefile = (String) OptFactory.getInstance().getOpt("keystorefile").getValue();
			String keystorepasswd = (String) OptFactory.getInstance().getOpt("keystorepasswd").getValue();
			
			if(keystorepasswd != null && keystorefile.equals("")) {
				OptFactory.getInstance().getOpt("keystorefile").setValue(null);
				keystorefile = null;
			}
			
			if(keystorepasswd != null && keystorepasswd.equals("")) {
				OptFactory.getInstance().getOpt("keystorepasswd").setValue(null);
				keystorepasswd = null;
			}
			
			if(keystorefile != null && keystorepasswd == null) {
				log.info("Enter password for keystore:");
				if(console != null)
					keystorepasswd = new String(console.readPassword());
				else
					keystorepasswd = scanner.nextLine();
				
				OptFactory.getInstance().getOpt("keystorepasswd").setValue(keystorepasswd);
			}
			
			if(keystorefile != null && !Files.exists(Paths.get(keystorefile))) {
				log.error("keystore file not found!");
				System.exit(0);
			}
			
			scanner.close();
									
		} catch (ParseException e) {
			log.error(e.getMessage(),e);
		}
		
	}	

	/**
	 * Read manifest.
	 *
	 * @return the map
	 */
	public Map<String, String> readManifest() {
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

	
	public String ownjarfile() {
		try {
			return new File(App.class.getProtectionDomain().getCodeSource().getLocation()
				    .toURI()).getPath();
		} catch (URISyntaxException e) {
			log.error(e);
			return null;
		}
	}
	
	
	/**
	 * Gets the log.
	 *
	 * @return the log
	 */
	public Log getLog() {
		return log;
	}

	
	/**
     * The main method, starting point of this app.
     * 
     * As this is mainly an App, this is the main entry point to start the App.
     *
     * @param args the arguments
     */
    public static void main(String[] args)  {
        App app = new App();
        app.run(args);
    }


}
