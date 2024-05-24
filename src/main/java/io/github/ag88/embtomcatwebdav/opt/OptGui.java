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

import io.github.ag88.embtomcatwebdav.opt.Opt.PropType;

/**
 * Conf for checkupdates option.
 */
public class OptGui extends Opt {

	public OptGui() {
		this.type = PropType.Prop;
		
		this.name = "gui";
		this.description = "enable QR Code Gui";
		this.defaultval = Boolean.TRUE;
		this.value = Boolean.TRUE;
		this.opt = null;
		this.hasarg = false;
		this.longopt = "gui";
		this.argname = null;
		this.valclazz = Boolean.class;
		this.priority = 46;
	}


}
