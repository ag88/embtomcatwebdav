package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptSecure extends Opt {

	public OptSecure() {
		this.name = "secure";
		this.description = "enable SSL, you need to supply a keystore file and keystore passwd, " +
			"if passwd is omitted it'd be prompted.";
		this.defaultval = Boolean.FALSE;
		this.opt = "S";
		this.longopt = "secure";
		this.argname = "keystore,passwd";
		this.cmdproc = true; //process command
		this.type = PropType.CLI;
		this.valclazz = Boolean.class;
		this.priority = 22;
	}
	
	@Override
	public Option getOption() {
		return Option.builder(opt).longOpt(longopt).desc(description).
				hasArg().argName(argname).build();
	}

	@Override
	public void process(CommandLine cmd, Object... objects) {
	}

}
