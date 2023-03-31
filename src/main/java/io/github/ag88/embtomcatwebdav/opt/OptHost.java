package io.github.ag88.embtomcatwebdav.opt;

public class OptHost extends Opt {

	public OptHost() {
		this.name = "host";
		this.description = "set host";
		this.defaultval = "localhost";
		this.value = defaultval;
		this.opt = "H";
		this.longopt = "host";
		this.argname = "hostname";
		this.valclazz = String.class;
		this.priority = 10;
	}

}
