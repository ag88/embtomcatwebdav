package io.github.ag88.embtomcatwebdav.opt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.App;
import io.github.ag88.embtomcatwebdav.WebDavServer;

public class OptFactory {

	Log log = LogFactory.getLog(OptFactory.class);

	TreeMap<String, Opt> opts = new TreeMap<String, Opt>();

	WebDavServer wdav;
	
	App app;
	
	Properties properties;
	
	private static OptFactory m_instance;
	
	private TreeSet<String> optc = new TreeSet<String>();
	private TreeSet<String> optl = new TreeSet<String>();
	
	private boolean chkdup = false;

	private OptFactory() {
	}

	/**
	 * Gets the single instance of OptFactory.
	 *
	 * @return single instance of OptFactory
	 */
	public static OptFactory getInstance() {
		// Double lock for thread safety.
		if (m_instance == null) {
			synchronized (OptFactory.class) {
				if (m_instance == null) {
					m_instance = new OptFactory();
				}
			}
		}
		return m_instance;
	}

	/**
	 * Registers the Opts in Opts Map
	 */
	public void registeropts() {
		addOpt(new OptHelp());
		addOpt(new OptConf());
		addOpt(new OptGenconf());
		addOpt(new OptHost());
		addOpt(new OptPort());
		addOpt(new OptPath());
		addOpt(new OptBaseDir());
		addOpt(new OptUrlPrefix());
		addOpt(new OptUser());
		addOpt(new OptRealm());
		addOpt(new OptPasswd());
		addOpt(new OptDigest());
		addOpt(new OptKeystoreFile());
		addOpt(new OptKeystorePasswd());
		addOpt(new OptSecure());
		addOpt(new OptQuiet());
		addOpt(new OptGenpasswd());
	}

	/**
	 * Generate options for commons-cli.
	 * 
	 * This takes the preconfigured Opts map and generate the options in options
	 *
	 * @param options the options
	 */
	public void genoptions(Options options) {
		Iterator<Opt> iter = iterator();

		while (iter.hasNext()) {
			Opt o = iter.next();
			if (o.getType().equals(Opt.PropType.Norm) || o.getType().equals(Opt.PropType.CLI)) {
				if (!o.isCmdproc()) {
					Option.Builder build;
					if (o.getOpt() == null)
						build = Option.builder();
					else
						build = Option.builder(o.getOpt());
					build = build.longOpt(o.getLongopt());
					build = build.desc(o.getDescription());
					if (o.hasarg) {
						build = build.hasArg().argName(o.getArgname());
						build = build.type(o.getValclass());
					}
					options.addOption(build.build());
				} else {
					options.addOption(o.getOption());
				}
			}
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
			Properties properties = new Properties();
			BufferedReader reader = new BufferedReader(new FileReader(configfile));
			properties.load(reader);
			
			this.properties = properties;
			
			OptFactory.getInstance().loadproperties(properties);
									
		} catch (FileNotFoundException e) {
			log.error(String.format("config file %s not found %s", configfile), e);
			System.exit(1);
		} catch (IOException e) {
			log.error(String.format("config file %s not found %s", configfile), e);
			System.exit(1);
		}		
	}

	/**
	 * Load properties into Opts
	 * 
	 * The properties should be loaded from a config file.
	 * This populates the configured Opts map with values from the properties.
	 *
	 * @param properties the properties
	 */
	public void loadproperties(Properties properties) {		
		
		Iterator<Opt> iter = iterator();
		while (iter.hasNext()) {
			Opt o = iter.next();
			if (o.getType().equals(Opt.PropType.Norm) || o.getType().equals(Opt.PropType.Prop) && o.isHasarg()) {
				Object val = o.getDefaultval();
				String defval = null;
				if (val != null)
					defval = val.toString();
				String sval = properties.getProperty(o.getName(), defval);
				if (o.getValclass().equals(Integer.class)) {
					try {
						int value = Integer.parseInt(sval);
						o.setValue(value);
					} catch (NumberFormatException e) {
						log.warn(String.format("opt: %s, invalid value: %s, using default %s", o.getName(), sval,
								o.getDefaultval().toString()));
						o.setValue(o.getDefaultval());
					}
				} else if (o.getValclass().equals(Boolean.class)) {

					try {
						boolean value = Boolean.parseBoolean(sval);
						o.setValue(value);
					} catch (Exception e) {
						log.warn(String.format("opt: %s, invalid value: %s, using default %s", o.getName(), sval,
								o.getDefaultval().toString()));
						o.setValue(o.getDefaultval());
					}
				} else
					o.setValue(sval);
			}
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
				
		Properties p = new Properties();
		OptFactory.getInstance().genproperties(p);

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
	 * Gen properties with default value from Opts Map in the passed properties
	 *
	 * @param properties the properties
	 */
	public void genproperties(Properties properties) {
		Iterator<Opt> iter = iterator();
		while (iter.hasNext()) {
			Opt o = iter.next();
			if (o.getType().equals(Opt.PropType.Norm) || o.getType().equals(Opt.PropType.Prop)) {
				Object value = null;
				value = o.getValue();
				if (value == null) {
					value = o.getDefaultval();
					if (value == null)
						properties.setProperty(o.getName(), "");
					else
						properties.setProperty(o.getName(), value.toString());
				} else
					properties.setProperty(o.getName(), value.toString());
			}
		}
	}

	
	/**
	 * Prints the opts.
	 */
	public void printOpts() {
		
		Iterator<Opt> iter = OptFactory.getInstance().iterator();
		StringBuilder sb = new StringBuilder(1024);
		while(iter.hasNext()) {
			Opt o = iter.next();
			sb.append(o.toString());
			sb.append(System.lineSeparator());				
		}
		log.info(sb.toString());

	}

	/**
	 * Adds the opt.
	 *
	 * @param opt the opt
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public void addOpt(Opt opt) throws IllegalArgumentException {
		
		if (chkdup) {
			if(opts.containsKey(opt.getName()))
				throw new IllegalArgumentException("duplicate opt");
		
			if(opt.getOpt() != null) 
			if(optc.contains(opt.getOpt())) {
				throw new IllegalArgumentException("duplicate short cmdline opt: ".concat(opt.getOpt()));
			} else
				optc.add(opt.getOpt());

			if(opt.getLongopt() != null)
			if(optl.contains(opt.getLongopt()))
				throw new IllegalArgumentException("duplicate long cmdline opt:".concat(opt.getLongopt()));
			else
				optl.add(opt.getLongopt());
		}
		
		opts.put(opt.getName(), opt);
	}

	/**
	 * Gets the opt.
	 *
	 * @param name the name
	 * @return the opt
	 */
	public Opt getOpt(String name) {
		return opts.get(name);
	}

	/**
	 * Del opt.
	 *
	 * @param name the name
	 */
	public void delOpt(String name) {
		opts.remove(name);
	}

	/**
	 * Iterator.
	 *
	 * @return the iterator
	 */
	public Iterator<Opt> iterator() {

		TreeSet<Opt> os = new TreeSet<>();
		os.addAll(opts.values());
		return os.iterator();
	}

	/**
	 * Gets the opts.
	 *
	 * @return the opts
	 */
	public TreeMap<String, Opt> getOpts() {
		return opts;
	}

	/**
	 * Sets the opts.
	 *
	 * @param props the props
	 */
	public void setOpts(TreeMap<String, Opt> props) {
		this.opts = props;
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * Sets the properties.
	 *
	 * @param properties the new properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Checks if is chkdup.
	 *
	 * @return true, if is chkdup
	 */
	public boolean isChkdup() {
		return chkdup;
	}

	/**
	 * Sets the chkdup.
	 *
	 * @param chkdup the new chkdup
	 */
	public void setChkdup(boolean chkdup) {
		this.chkdup = chkdup;
	}
	
	/**
	 * Gets the WebDAVserver.
	 *
	 * @return the wdav
	 */
	public WebDavServer getWdav() {
		return wdav;
	}

	/**
	 * Sets the WebDAVserver.
	 *
	 * @param wdav the new wdav
	 */
	public void setWdav(WebDavServer wdav) {
		this.wdav = wdav;
	}

	/**
	 * Gets the app.
	 *
	 * @return the app
	 */
	public App getApp() {
		return app;
	}

	/**
	 * Sets the app.
	 *
	 * @param app the new app
	 */
	public void setApp(App app) {
		this.app = app;
	}



}