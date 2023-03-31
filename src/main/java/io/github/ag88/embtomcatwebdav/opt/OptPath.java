package io.github.ag88.embtomcatwebdav.opt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OptPath extends Opt {

	public OptPath() {
		this.name = "path";
		this.description = "set path, default current working dir";
		this.defaultval = System.getProperty("user.dir");
		this.value = defaultval;
		this.opt = "P";
		this.longopt = "path";
		this.argname = "path";
		this.valclazz = String.class;
		this.priority = 12;
		this.validate = true;
		this.replace = true;
	}

	@Override
	public boolean isvalid(Object object) {
		if(! (object instanceof String)) return false;
		
		String paths = (String) object;
		if(paths.equals(""))
			return false;
		
		Path p = Paths.get(paths);
		
		if(Files.exists(p) && Files.isDirectory(p))
			return true;
		else
			return false;
	}
	
	@Override
	public Object replace(Object object) {
		return System.getProperty("user.dir");
	}

}
