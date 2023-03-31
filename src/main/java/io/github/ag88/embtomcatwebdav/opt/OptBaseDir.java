package io.github.ag88.embtomcatwebdav.opt;

import java.nio.file.Paths;

public class OptBaseDir extends Opt {

	public OptBaseDir() {
		
		/* 
		 * due to dependency on port, PropPort needs to be registered before this.
		 */
		Object value = OptFactory.getInstance().getOpt("port").getDefaultval();
		int port = 8080;
		if(value != null && value instanceof Integer)
			port = ((Integer) value).intValue();
		
		this.name = "basedir";
		this.description = "set basedir, a work folder for tomcat, default [current working dir]/tomcat.port";
		this.defaultval = Paths.get(System.getProperty("user.dir"),
				"tomcat.".concat(Integer.toString(port))).toString();
		this.value = null;
		this.opt = "b";
		this.longopt = "basedir";
		this.argname = "basedir";
		this.valclazz = String.class;
		this.priority = 13;
	}


}
