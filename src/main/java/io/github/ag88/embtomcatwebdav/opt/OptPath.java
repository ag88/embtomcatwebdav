package io.github.ag88.embtomcatwebdav.opt;

public class OptPath extends Opt {

	public OptPath() {
		this.name = "path";
		this.description = "set path, default current working dir";
		this.defaultval = System.getProperty("user.dir");
		this.opt = "P";
		this.longopt = "path";
		this.argname = "path";
		this.valclazz = String.class;
		this.priority = 12;
	}


}
