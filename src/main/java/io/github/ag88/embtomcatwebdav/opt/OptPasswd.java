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
 * Conf for Passwd option.
 */
public class OptPasswd extends Opt {

	public OptPasswd() {
		this.name = "password";
		StringBuilder sb = new StringBuilder(100);
		sb.append("set password, on the command line you may omit this, it would prompt for it if -u is specified");
		sb.append(System.lineSeparator());
		sb.append("In the config file, if you leave it empty as \"password=\" it is actually a blank password.");
		sb.append(System.lineSeparator());
		sb.append("To make it prompt for password from the config file, comment it with a # in front.");
		this.description = sb.toString();
		this.defaultval = null;
		this.opt = "w";
		this.longopt = "passwd";
		this.argname = "password";
		this.passwd = true;
		this.valclazz = String.class;
		this.priority = 17;
	}

}
