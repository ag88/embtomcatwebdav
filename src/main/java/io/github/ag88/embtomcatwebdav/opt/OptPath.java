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
 * Conf for Path option.
 */
public class OptPath extends Opt {

	public OptPath() {
		this.name = "path";
		StringBuilder sb = new StringBuilder(100);
		sb.append("set path, default: current working dir.");
		sb.append(System.lineSeparator());
		sb.append("This is the root directory which is accessible in the webdav or upload servlet");
		this.description = sb.toString();
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
