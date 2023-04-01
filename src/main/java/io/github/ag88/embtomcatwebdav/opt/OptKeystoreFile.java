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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * Conf for KeystoreFile option.
 */
public class OptKeystoreFile extends Opt {

	public OptKeystoreFile() {
		this.name = "keystorefile";
		this.description = "keystore file, for SSL";
		this.defaultval = null;
		this.opt = null;
		this.longopt = null;
		this.argname = null;
		this.cmdproc = false; //process command
		this.type = PropType.Prop;
		this.valclazz = String.class;
		this.priority = 20;
	}
	

}
