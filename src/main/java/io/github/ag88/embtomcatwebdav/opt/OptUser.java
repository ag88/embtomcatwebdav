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
 * Conf for User option.
 */
public class OptUser extends Opt {

	public OptUser() {
		this.name = "user";
		this.description = "set user";
		this.defaultval = null;
		this.opt = "u";
		this.longopt = "user";
		this.argname = "username";
		this.valclazz = String.class;
		this.priority = 16;
	}


}
