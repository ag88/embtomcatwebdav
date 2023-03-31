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
	public void process(CommandLine cmd, Object... objects ) {
		String configfile = cmd.getOptionValue("conf");
		OptFactory.getInstance().loadconfigprop(configfile);		
	}

}
