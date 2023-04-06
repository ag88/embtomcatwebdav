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

package io.github.ag88.embtomcatwebdav.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.plaf.basic.BasicComboPopup;

import org.apache.tomcat.util.compat.JrePlatform;

/**
 * Class DefFilePathNameValidator.<p>
 * 
 * This is an implementation of {@link FilePathNameValidator}.
 * 
 */
public class DefFilePathNameValidator extends FilePathNameValidator {


	/**
	 * Checks if is valid filename.
	 *
	 * @param filename the filename
	 * @param logrec the logrec
	 * @return true, if is valid filename
	 */
	@Override
	public boolean isValidFilename(String filename, List<LogRecord> logrec) {

		if(filename == null) {
	        LogRecord lr = new LogRecord(Level.SEVERE, "Illegal filename, null: ");
	        logrec.add(lr);
	        return false;
		}
			
		if (filename.length() == 0) {
        	LogRecord lr = new LogRecord(Level.SEVERE, "Illegal filename, zero length: ".concat(filename));
        	logrec.add(lr);
            return false;
		}
			
		
        if (filename.equals("/")) {
        	LogRecord lr = new LogRecord(Level.SEVERE, "Illegal filename, root folder not allowed: ".concat(filename));
        	logrec.add(lr);
            return false;
        }

        // If the requested names ends in '/', the Java File API will return a
        // matching file if one exists. This isn't what we want as it is not
        // consistent with the Servlet spec rules for request mapping.
        if (filename.contains("/")) {
        	LogRecord lr = new LogRecord(Level.SEVERE, "Illegal filename, contains '/': ".concat(filename));
        	logrec.add(lr);
            return false;
        }


        // Additional Windows specific checks to handle known problems with
        // File.getCanonicalPath()
        if (JrePlatform.IS_WINDOWS && ! isValidWindowsFilename(filename, logrec)) {
            return false;
        }
		
		
		
		return true;
	}

    private boolean isValidWindowsFilename(String filename, List<LogRecord> logrec) {
        final int len = filename.length();
        if (len == 0) {
        	LogRecord lr = new LogRecord(Level.SEVERE, "Illegal filename, zero length: ".concat(filename));
        	logrec.add(lr);
            return false;
        }
        
        /**
         * invalid windows chars
         * 
         * https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file
         */        
        for (int i = 0; i < len; i++) {
            char c = filename.charAt(i);
            if (c == '<' || c == '>' ||  c == ':' || c == '\"' || c == '/' || 
            		c == '\\' || c == '|' || c == '?' || c == '*') {
                // These characters are disallowed in Windows file names
            	LogRecord lr = new LogRecord(Level.SEVERE, 
            			"Illegal filename, has invalid windows chars {<,>,:,\", /, \\, |, ?, *}: ".concat(filename));
            	logrec.add(lr);
                return false;
            }
        }
        // Windows does not allow file names to end in ' ' unless specific low
        // level APIs are used to create the files that bypass various checks.
        // File names that end in ' ' are known to cause problems when using
        // File#getCanonicalPath().
        if (filename.charAt(len - 1) == ' ') {
        	LogRecord lr = new LogRecord(Level.SEVERE, 
        			"Illegal filename, ends with space: ".concat(filename));
        	logrec.add(lr);
            return false;
        }
        
        return true;
    }
	
	/**
	 * Checks if is valid pathname.
	 *
	 * @param basepath the basepath
	 * @param filename the filename
	 * @param logrec the logrec
	 * @return true, if is valid pathname
	 */
	@Override
	public boolean isValidPathname(Path basepath, String filename, List<LogRecord> logrec) {

		if(! Files.exists(basepath)) {
			LogRecord lr = new LogRecord(Level.SEVERE, "directory did not exist: ".concat(basepath.toString()));
			logrec.add(lr);			
			return false;
		}


		if( ! Files.isReadable(basepath)) {
			LogRecord lr = new LogRecord(Level.SEVERE, "directory is not accessible: ".concat(basepath.toString()));
			logrec.add(lr);			
			return false;			
		}
		
		if( !readonly && ! Files.isWritable(basepath)) {
			LogRecord lr = new LogRecord(Level.SEVERE, "directory is not writable: ".concat(basepath.toString()));
			logrec.add(lr);			
			return false;			
		}
				
		Path filepath = null;
		try {
			filepath = basepath.resolve(filename);
		} catch (Exception e1) {		
			LogRecord lr = new LogRecord(Level.SEVERE, errormsg(basepath.toString(), filename, null, e1));
			logrec.add(lr);
			return false;		
		}
		
		
        // Check that this file is located under the WebResourceSet's base
        Path canpath = null;
        try {
            canpath = filepath.normalize();
        } catch (Exception e) {
			LogRecord lr = new LogRecord(Level.SEVERE, 
				errormsg(basepath.toString(), filename, null, e));
			logrec.add(lr);
			return false;
        }
        if (canpath == null || ! canpath.startsWith(basepath)) {
        	LogRecord lr = new LogRecord(Level.SEVERE, 
        			"Illegal, file is outside basedir: "
        			.concat(basepath.toString())
        			.concat(", ")
        			.concat(filename));
        	logrec.add(lr);
        	return false;        	
        }

        // Ensure that the file is not outside the fileBase. This should not be
        // possible for standard requests (the request is normalized early in
        // the request processing) but might be possible for some access via the
        // Servlet API (RequestDispatcher, HTTP/2 push etc.) therefore these
        // checks are retained as an additional safety measure
        // absoluteBase has been normalized so absPath needs to be normalized as
        // well.
        Path abspath = null;
        try {
			abspath = filepath.normalize().toAbsolutePath();
		} catch (Exception e) {
			LogRecord lr = new LogRecord(Level.SEVERE, errormsg(basepath.toString(), filename, null, e));
			logrec.add(lr);
			return false;			
		}
        
        if (abspath == null || basepath.getNameCount() > abspath.getNameCount()) {
        	LogRecord lr = new LogRecord(Level.SEVERE, 
        			"Illegal, file is outside basedir: "
        			.concat(basepath.toString())
        			.concat(", ")
        			.concat(filename));
        	logrec.add(lr);
        	return false;
        }

        // Remove the fileBase location from the start of the paths since that
        // was not part of the requested path and the remaining check only
        // applies to the request path
        //absPath = absPath.substring(absoluteBase.length());
        //canPath = canPath.substring(canonicalBase.length());
        abspath = basepath.relativize(abspath);
        canpath = basepath.relativize(canpath);

        // Case sensitivity check
        // The normalized requested path should be an exact match the equivalent
        // canonical path. If it is not, possible reasons include:
        // - case differences on case insensitive file systems
        // - Windows removing a trailing ' ' or '.' from the file name
        //
        // In all cases, a mismatch here results in the resource not being
        // found
        //
        // absPath is normalized so canPath needs to be normalized as well
        // Can't normalize canPath earlier as canonicalBase is not normalized
        //if (canPath.length() > 0) {
        //    canPath = normalize(canPath);
        //}
        if (!canpath.equals(abspath)) {
            if (!canpath.toString().toLowerCase().equals(
            	abspath.toString().toLowerCase())) {
                // Typically means symlinks are in use but being ignored. Given
                // the symlink was likely created for a reason, log a warning
                // that it was ignored.
                LogRecord lr = new LogRecord(Level.SEVERE, 
                		String.format("symlink ignored %s, %s, %s", 
                		abspath.toString(), canpath.toString()));
                logrec.add(lr);
                return false;
            }
        }

		return true;
	}

	
	private String errormsg(String basepath, String filename, String message, Exception e) {
		StringBuilder sb = new StringBuilder(100);
		sb.append("invalid file path: ");
		sb.append(basepath);
		sb.append(", ");
		sb.append(filename);
		sb.append(System.lineSeparator());
		if(message != null) {
			sb.append(message);
			sb.append(System.lineSeparator());
		}
		sb.append(e.getMessage());
		sb.append(System.lineSeparator());
		sb.append(e.getStackTrace());
		return sb.toString();
	}
	
}
