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

/**
 * Conf for AllowLinking option.
 */
public class OptAllowLinking extends Opt {

	public OptAllowLinking() {
		this.name = "allowlinking";
		this.description = "allow linking (e.g. follow symbolic links, warn: links can go outside working dir/path, only works in upload servlet)";
		this.defaultval = Boolean.FALSE;
		this.value = Boolean.FALSE;
		this.opt = null;
		this.longopt = "allowlinking";
		this.argname = null;
		this.hasarg = false;
		this.valclazz = Boolean.class;
		this.priority = 46;
	}


}
