/*
 * Attribution to https://tomcat.apache.org/
 * 
 * portions adapted and modified Andrew Goh http://github.com/ag88
 *  
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package io.github.ag88.embtomcatwebdav.util;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Class FilePathNameValidator.<p>
 * 
 * This abstract class implements a filename validator and pathname validator
 *  
 */
public abstract class FilePathNameValidator {

	protected boolean readonly;
	
	public FilePathNameValidator() {
	}
	
	public FilePathNameValidator(boolean readonly, boolean exist) {
		this.readonly = readonly;
	}

	/**
	 * Checks if is valid filename.
	 *
	 * @param filename the filename
	 * @param logrec the logrec
	 * @return true, if is valid filename
	 */
	abstract public boolean isValidFilename(String filename, List<LogRecord> logrec);
	
	/**
	 * Checks if is valid pathname.
	 *
	 * @param basepath the basepath
	 * @param filename the filename
	 * @param logrec the logrec
	 * @return true, if is valid pathname
	 */
	abstract public boolean isValidPathname(Path basepath, String filename, List<LogRecord> logrec);
	

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	
}
