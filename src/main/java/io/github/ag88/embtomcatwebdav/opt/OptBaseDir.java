/*
 Copyright 2023 Andrew Goh http://github.com/ag88
 
 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package io.github.ag88.embtomcatwebdav.opt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Conf for BaseDir option.
 */
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
		Object value = OptFactory.getInstance().getOpt("port").getValue();
		int port = 8080;
		if(value != null && value instanceof Integer)
			port = ((Integer) value).intValue();

		return Paths.get(System.getProperty("user.dir"),
				"tomcat.".concat(Integer.toString(port))).toString();
	}

}
