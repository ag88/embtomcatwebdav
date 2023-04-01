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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import io.github.ag88.embtomcatwebdav.App;

/**
 * Conf for Help option.
 */
public class OptHelp extends Opt {	

	public OptHelp() {
		this.name = "help";
		this.description = "help";
		this.defaultval = null;
		this.opt = "h";
		this.longopt = "help";
		this.argname = null;
		this.cmdproc = true; //process command
		this.type = PropType.CLI;
		this.hasarg = false;
		this.priority = 1;
	}
	
	@Override
	public Option getOption() {
		return Option.builder("h").longOpt("help").desc("help").build();
	}

	@Override
	public void process(CommandLine cmd, Object... objects) {
		
		App app = (App) objects[0];
		//WebDavServer wdav = (WebDavServer) objects[1];
		Options options = (Options) objects[2];
				
		HelpFormatter formatter = new HelpFormatter();
		StringBuilder sb = new StringBuilder(100);
		sb.append(System.lineSeparator());
		sb.append("Copyright (C) 2023 Andrew Goh");
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("Licensed under the Apache License, Version 2.0");
		sb.append(System.lineSeparator());
		sb.append("http://www.apache.org/licenses/LICENSE-2.0");
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("Project web: https://github.com/ag88/embtomcatwebdav");		
		
		Map<String, String> mkv = app.readManifest();
		String name;
		if(mkv.get("artifactId") != null) {
			name = mkv.get("artifactId").concat("-").concat(mkv.get("version"));			
		} else {
			name = "embtomcatwebdav";	
		}		
		formatter.printHelp(name, "", options, sb.toString());
			
        try {
            Class.forName("org.junit.jupiter.api.Test");
        } catch (ClassNotFoundException e) {
        	System.exit(0);
        }
		
		
	}

}
