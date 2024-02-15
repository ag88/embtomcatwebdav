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
public class OptDLZipPath extends Opt {

	public OptDLZipPath() {

		/* defaulting base dir done in code */
		this.type = PropType.Prop;
		
		this.name = "dlzip_path";
		this.description = "set the url path for download multi files as zip";
		this.defaultval = "/dlzip";
		this.value = null;
		this.opt = null;
		this.hasarg = true;
		this.longopt = null;
		this.argname = null;
		this.valclazz = String.class;
		this.priority = 31;
		this.validate = true;
		this.replace = false;
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
		Object value = OptFactory.getInstance().getOpt("basedir").getValue();

		return value;
	}

}
