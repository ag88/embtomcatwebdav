package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptGenconf extends Opt {

	public OptGenconf() {
		this.name = "genconf";
		this.description = "generate properties config file";
		this.defaultval = null;
		this.opt = null;
		this.longopt = "genconf";
		this.argname = "configfile";
		this.cmdproc = true; //process command
		this.type = PropType.CLI;
		this.valclazz = String.class;
		this.priority = 3;
	}
	
	@Override
	public Option getOption() {
		return Option.builder().longOpt(longopt).desc(description).
			hasArg().argName(argname).build();
	}

	@Override
	public void process(CommandLine cmd, Object... objects) {
		String configfile = cmd.getOptionValue("genconf");
		OptFactory.getInstance().genconfigprop(configfile);
	}

}
