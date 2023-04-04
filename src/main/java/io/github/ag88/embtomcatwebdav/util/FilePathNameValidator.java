package io.github.ag88.embtomcatwebdav.util;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogRecord;

public abstract class FilePathNameValidator {

	protected boolean readonly;
	
	public FilePathNameValidator() {
	}
	
	public FilePathNameValidator(boolean readonly, boolean exist) {
		this.readonly = readonly;
	}

	abstract public boolean isValidFilename(String filename, List<LogRecord> logrec);
	
	abstract public boolean isValidPathname(Path basepath, String filename, List<LogRecord> logrec);
	

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	
}
