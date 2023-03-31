package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
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

public class App {
	
	private Log log = LogFactory.getLog(App.class);
	
	WebDavServer wdav;

	public App() {
		wdav = new WebDavServer();		
		OptFactory.getInstance().registeropts();
		OptFactory.getInstance().setWdav(wdav);
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
		OptFactory.getInstance().printOpts();
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
		
		/*
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
		*/
		
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
									opt.setValue(Boolean.valueOf(true));
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
			
			/*
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
									
			*/
						
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
