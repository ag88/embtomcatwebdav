package io.github.ag88.embtomcatwebdav.opt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public class OptKeystoreFile extends Opt {

	public OptKeystoreFile() {
		this.name = "keystorefile";
		this.description = "keystore file, for SSL";
		this.defaultval = null;
		this.opt = null;
		this.longopt = null;
		this.argname = null;
		this.cmdproc = false; //process command
		this.type = PropType.Prop;
		this.valclazz = String.class;
		this.priority = 20;
	}
	

}
