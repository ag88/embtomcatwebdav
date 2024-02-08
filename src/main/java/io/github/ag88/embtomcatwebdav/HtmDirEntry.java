package io.github.ag88.embtomcatwebdav;

import org.apache.tomcat.util.http.FastHttpDateFormat;

public class HtmDirEntry {
	
	String filename;
	String path;
	boolean isdir;
	long size;
	long lastmodified;

	public HtmDirEntry() {
	}

	public HtmDirEntry(String filename, String path, boolean isdir, long size, long lastmodified) {
		super();
		this.filename = filename;
		this.path = path;
		this.isdir = isdir;
		this.size = size;
		this.lastmodified = lastmodified;
	}

	public String renderSize() {

		if (isdir) return "&nbsp;";
		
        long leftSide = size / 1024;
        long rightSide = (size % 1024) / 103;   // Makes 1 digit
        if ((leftSide == 0) && (rightSide == 0) && (size > 0)) {
            rightSide = 1;
        }

        return ("" + leftSide + "." + rightSide + " kb");
    }

	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isIsdir() {
		return isdir;
	}

	public void setIsdir(boolean isdir) {
		this.isdir = isdir;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getLastModifStr() { 
		return FastHttpDateFormat.formatDate(lastmodified);
	}

	public long getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(long lastmodified) {
		this.lastmodified = lastmodified;
	}
		
}
