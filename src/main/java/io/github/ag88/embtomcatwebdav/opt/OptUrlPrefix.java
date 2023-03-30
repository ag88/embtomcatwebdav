package io.github.ag88.embtomcatwebdav.opt;

public class OptUrlPrefix extends Opt {

	public OptUrlPrefix() {
		this.name = "urlprefix";
		this.description = "set urlprefix, default /webdav";
		this.defaultval = "/webdav";
		this.opt = "x";
		this.longopt = "urlprefix";
		this.argname = "urlprefix";
		this.valclazz = String.class;
		this.priority = 14;
	}


}
