package io.github.ag88.embtomcatwebdav.model;

import org.apache.tomcat.util.http.FastHttpDateFormat;

public class HtmDirEntry {
	
	String filename;
	String path;
	boolean dir;
	long size;
	long lastmodified;
	String selname;

	public HtmDirEntry() {
	}

	public HtmDirEntry(String filename, String path, boolean dir, long size, long lastmodified, String selname) {
		super();
		this.filename = filename;
		this.path = path;
		this.dir = dir;
		this.size = size;
		this.lastmodified = lastmodified;
		this.selname = selname;
	}

	public String renderSize() {

		if (dir) return "&nbsp;";
		
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

	public boolean isDir() {
		return dir;
	}

	public void setDir(boolean dir) {
		this.dir = dir;
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

	public String getSelname() {
		return selname;
	}

	public void setSelname(String selname) {
		this.selname = selname;
	}

		
}
