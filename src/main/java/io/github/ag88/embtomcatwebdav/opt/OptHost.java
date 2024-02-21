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
 * Conf for Host option.
 */
public class OptHost extends Opt {

	public OptHost() {
		this.name = "host";
		StringBuilder sb = new StringBuilder(100);
		sb.append("set host. Default localhost. If you set the host to 0.0.0.0 it will listen on all interfaces");
		sb.append(System.lineSeparator());
		sb.append("however, you still need to specify the IP address of this host PC from your web browser");
		sb.append(System.lineSeparator());
		sb.append("to access the webdav or upload servlet web page");
		this.description = sb.toString();
		this.defaultval = "localhost";
		this.value = defaultval;
		this.opt = "H";
		this.longopt = "host";
		this.argname = "hostname";
		this.valclazz = String.class;
		this.priority = 10;
	}

}
