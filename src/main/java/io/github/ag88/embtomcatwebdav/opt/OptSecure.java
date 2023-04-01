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
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import io.github.ag88.embtomcatwebdav.App;

/**
 * Conf for Secure option.
 */
public class OptSecure extends Opt {

	public OptSecure() {
		this.name = "secure";
		this.description = "enable SSL, you need to supply a keystore file and keystore passwd, " +
			"if passwd is omitted it'd be prompted.";
		this.defaultval = Boolean.FALSE;
		this.opt = "S";
		this.longopt = "secure";
		this.argname = "keystore,passwd";
		this.cmdproc = true; //process command
		this.type = PropType.CLI;
		this.valclazz = Boolean.class;
		this.priority = 22;
	}
	
	@Override
	public Option getOption() {
		return Option.builder(opt).longOpt(longopt).desc(description).
				hasArg().argName(argname).build();
	}

	@Override
	public void process(CommandLine cmd, Object... objects) {
		App app = (App) objects[0];
		String keystorefile = null, keystorepasswd = null;
		String arg = cmd.getOptionValue("secure");
		if (arg.contains(",")) {
			String[] f = arg.split(",");
			keystorefile = f[0];
			keystorepasswd = f[1];
		} else {
			keystorefile = arg;
		}
		
		if(keystorefile != null && !Files.exists(Paths.get(keystorefile))) {
			app.getLog().error(String.format("keystore file %s not found!", keystorefile));
			System.exit(0);
		}
		
		OptFactory.getInstance().getOpt("keystorefile").setValue(keystorefile);
		
		if(keystorepasswd != null )
			OptFactory.getInstance().getOpt("keystorepasswd").setValue(keystorepasswd);

	}

}
