package io.github.ag88.embtomcatwebdav.opt;

public class OptPort extends Opt {

	public OptPort() {
		this.name = "port";
		this.description = "set port";
		this.defaultval = Integer.valueOf(8080);
		this.value = Integer.valueOf(8080);
		this.opt = "p";
		this.longopt = "port";
		this.argname = "port";
		this.valclazz = Integer.class;
		this.priority = 11;
	}


}
