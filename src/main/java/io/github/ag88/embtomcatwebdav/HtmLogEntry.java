package io.github.ag88.embtomcatwebdav;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class HtmLogEntry {

	LogRecord logrec;
	
	public HtmLogEntry(LogRecord logrec) {
		this.logrec = logrec;
	}	
	
	public String getMsg() {
		return logrec.getMessage();
	}
	
	public String getStyle() {
		if (logrec.getLevel().equals(Level.SEVERE)) {
			return "color: red;";
		} else if (logrec.getLevel().equals(Level.WARNING)) {
			return "color: orange;";
		} else {
			return "color: black;";
		}
	}
	
}
