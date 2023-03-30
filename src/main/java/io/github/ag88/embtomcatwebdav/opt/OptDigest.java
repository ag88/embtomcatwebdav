package io.github.ag88.embtomcatwebdav.opt;

public class OptDigest extends Opt {

	public OptDigest() {
		this.name = "digest";
		this.description = "use digest authentication";
		this.defaultval = Boolean.FALSE;
		this.opt = "D";
		this.longopt = "digest";
		this.argname = "";
		this.valclazz = Boolean.class;
		this.priority = 18;
	}


}
