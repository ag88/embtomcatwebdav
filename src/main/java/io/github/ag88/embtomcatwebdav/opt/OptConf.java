package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import io.github.ag88.embtomcatwebdav.WebDavServer;

public class OptConf extends Opt {

	public OptConf() {
		this.name = "conf";
		this.description = "load properties config file";
		this.defaultval = null;
		this.opt = "c";
		this.longopt = "conf";
		this.argname = "configfile";
		this.cmdproc = true; //process command
		this.notarget = true;
		this.type = PropType.CLI;
		this.valclazz = String.class;
		this.priority = 2;
	}
	
	@Override
	public Option getOption() {
		return Option.builder(opt).longOpt(longopt).desc(description).
				hasArg().argName(argname).build();
	}

	@Override
	public void process(CommandLine cmd) {
		String configfile = cmd.getOptionValue("conf");
		WebDavServer wdav = OptFactory.getInstance().getWdav();
		wdav.loadconfigprop(configfile);
	}

}
