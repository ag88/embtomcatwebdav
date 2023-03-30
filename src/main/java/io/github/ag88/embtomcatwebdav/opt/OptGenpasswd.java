package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptGenpasswd extends Opt {

	public OptGenpasswd() {
		this.name = "genpass";
		this.description = "dialog to generate digest password";
		this.defaultval = null;
		this.opt = null;
		this.longopt = "genpass";
		this.argname = null;
		this.cmdproc = true; //process command
		this.notarget = true;
		this.type = PropType.CLI;
		this.hasarg = false;
		this.priority = 25;
	}
	
	@Override
	public Option getOption() {
		return Option.builder().longOpt(longopt).desc(description).build();
	}

	@Override
	public void process(CommandLine cmd) {
	}

}
