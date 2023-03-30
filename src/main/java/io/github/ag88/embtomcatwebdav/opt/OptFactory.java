package io.github.ag88.embtomcatwebdav.opt;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.WebDavServer;

public class OptFactory {

	Log log = LogFactory.getLog(OptFactory.class);

	TreeMap<String, Opt> opts = new TreeMap<String, Opt>();

	WebDavServer wdav;

	Options options;

	private static OptFactory m_instance;

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

		Iterator<Opt> iter = iterator();
		StringBuilder sb = new StringBuilder(1024);
		while (iter.hasNext()) {
			Opt o = iter.next();
			sb.append(o.toString());
			sb.append(System.lineSeparator());
		}
		log.info(sb.toString());
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
		this.options = options;
	}

	public void updateBean(WebDavServer wdav) {

		Iterator<Opt> iter = iterator();
		while (iter.hasNext()) {
			Opt o = iter.next();

			try {
				if (!o.notarget)
					PropertyUtils.setSimpleProperty(wdav, o.getName(), o.getValue());
			} catch (IllegalArgumentException e) {
				log.error("property: ".concat(o.getName()), e);
			} catch (IllegalAccessException e) {
				log.error("property: ".concat(o.getName()), e);
			} catch (InvocationTargetException e) {
				log.error("property: ".concat(o.getName()), e);
			} catch (NoSuchMethodException e) {
				log.error("property: ".concat(o.getName()), e);
			}
		}

	}

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

	public void addOpt(Opt prop) {
		opts.put(prop.getName(), prop);
	}

	public Opt getOpt(String name) {
		return opts.get(name);
	}

	public void delOpt(String name) {
		opts.remove(name);
	}

	public Iterator<Opt> iterator() {
		// return opts.values().iterator();
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

	public WebDavServer getWdav() {
		return wdav;
	}

	public void setWdav(WebDavServer wdav) {
		this.wdav = wdav;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

}
