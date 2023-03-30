package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptKeystorePasswd extends Opt {

	public OptKeystorePasswd() {
		this.name = "keystorepasswd";
		this.description = "keystorepasswd, for SSL";
		this.defaultval = null;
		this.opt = null;
		this.longopt = null;
		this.argname = null;
		this.cmdproc = false; //process command
		this.notarget = false;
		this.passwd = true;
		this.type = PropType.Prop;
		this.valclazz = String.class;
		this.priority = 21;
	}
	
	@Override
	public Option getOption() {
		return null;
	}

	@Override
	public void process(CommandLine cmd) {
	}

}
