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
 * Conf for Createdirpath option.
 */
public class OptCreatedirPath extends Opt {

	public OptCreatedirPath() {

		/* defaulting base dir done in code */
		this.type = PropType.Prop;
		
		this.name = "createdir_path";
		this.description = "set the url path for createdir servlet";
		this.defaultval = "/createdir";
		this.value = null;
		this.opt = null;
		this.hasarg = true;
		this.longopt = null;
		this.argname = null;
		this.valclazz = String.class;
		this.priority = 32;
		this.validate = false;
		this.replace = false;
	}
	
}
