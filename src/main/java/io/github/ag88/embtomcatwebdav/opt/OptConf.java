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

import io.github.ag88.embtomcatwebdav.WebDavServer;

/**
 * Conf for Conf option.
 */
public class OptConf extends Opt {

	public OptConf() {
		this.name = "conf";
		this.description = "load properties config file";
		this.defaultval = null;
		this.opt = "c";
		this.longopt = "conf";
		this.argname = "configfile";
		this.cmdproc = true; //process command
		this.type = PropType.CLI;
		this.valclazz = String.class;
		this.priority = 2;
	}
	
	@Override
	public Option getOption() {
		return Option.builder(opt).longOpt(longopt).desc(description).
				hasArg().argName(argname).build();
	}

	
	@Override
	public void process(CommandLine cmd, Object... objects ) {
		String configfile = cmd.getOptionValue("conf");
		OptFactory.getInstance().loadconfigprop(configfile);		
	}

}
