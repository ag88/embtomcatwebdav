package io.github.ag88.embtomcatwebdav.opt;

public class OptQuiet extends Opt {

	public OptQuiet() {
		this.name = "quiet";
		this.description = "mute (most) logs";
		this.defaultval = Boolean.FALSE;
		this.opt = "q";
		this.longopt = "quiet";
		this.argname = "";
		this.valclazz = Boolean.class;
		this.priority = 19;
	}


}
