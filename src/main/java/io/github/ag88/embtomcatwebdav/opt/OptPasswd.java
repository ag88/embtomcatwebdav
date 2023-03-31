package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptPasswd extends Opt {

	public OptPasswd() {
		this.name = "password";
		this.description = "set password, you may omit this, it would prompt for it if -u is specified";
		this.defaultval = null;
		this.opt = "w";
		this.longopt = "passwd";
		this.argname = "password";
		this.passwd = true;
		this.valclazz = String.class;
		this.priority = 17;
	}

}
