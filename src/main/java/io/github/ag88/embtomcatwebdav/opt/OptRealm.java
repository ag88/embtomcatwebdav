package io.github.ag88.embtomcatwebdav.opt;

public class OptRealm extends Opt {

	public OptRealm() {
		this.name = "realm";
		this.description = "set realm name, default 'Simple'";
		this.defaultval = "Simple";
		this.opt = "R";
		this.longopt = "realm";
		this.argname = "realmname";
		this.valclazz = String.class;
		this.priority = 15;
	}


}
