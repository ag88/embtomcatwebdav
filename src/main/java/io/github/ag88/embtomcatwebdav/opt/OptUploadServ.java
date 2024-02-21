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
 * Conf for Digest option.
 */
public class OptUploadServ extends Opt {

	public OptUploadServ() {
		this.name = "uploadservlet";
		StringBuilder sb = new StringBuilder(100);
		sb.append("use upload servlet. Default: true.");
		sb.append(System.lineSeparator());
		sb.append("specifying on command line, toggles it off");
		sb.append(System.lineSeparator());
		sb.append("in config file, specify it directly false or true to use the upload servlet");
		this.description = sb.toString();
		this.defaultval = Boolean.TRUE;
		this.value = Boolean.TRUE;
		this.opt = "U";
		this.longopt = "uploadservlet";
		this.argname = "";
		this.hasarg = false;
		this.valclazz = Boolean.class;
		this.priority = 30;
	}


}
