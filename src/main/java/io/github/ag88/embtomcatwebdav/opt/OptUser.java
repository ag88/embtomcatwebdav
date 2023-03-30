package io.github.ag88.embtomcatwebdav.opt;

public class OptUser extends Opt {

	public OptUser() {
		this.name = "user";
		this.description = "set user";
		this.defaultval = null;
		this.opt = "u";
		this.longopt = "user";
		this.argname = "username";
		this.valclazz = String.class;
		this.priority = 16;
	}


}
