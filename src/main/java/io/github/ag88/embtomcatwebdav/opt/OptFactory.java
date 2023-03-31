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

	Options options;
	
	Properties properties;
	
	private static OptFactory m_instance;
	
	private TreeSet<String> optc = new TreeSet<String>();
	private TreeSet<String> optl = new TreeSet<String>();
	
	private boolean chkdup = false;

	private OptFactory() {
	}

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

	public void loadproperties(Properties properties) {
		this.properties = properties;
		
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

	public Opt getOpt(String name) {
		return opts.get(name);
	}

	public void delOpt(String name) {
		opts.remove(name);
	}

	public Iterator<Opt> iterator() {

		TreeSet<Opt> os = new TreeSet<>();
		os.addAll(opts.values());
		return os.iterator();
	}

	public TreeMap<String, Opt> getOpts() {
		return opts;
	}

	public void setOpts(TreeMap<String, Opt> props) {
		this.opts = props;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public boolean isChkdup() {
		return chkdup;
	}

	public void setChkdup(boolean chkdup) {
		this.chkdup = chkdup;
	}
	
	public WebDavServer getWdav() {
		return wdav;
	}

	public void setWdav(WebDavServer wdav) {
		this.wdav = wdav;
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}



}
