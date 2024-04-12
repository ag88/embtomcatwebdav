package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.GitCheckUpdates.Version;

/**
 * Check new versions in repository.
 * 
 * 
 */
public class GitCheckUpdates {

	private Log log = LogFactory.getLog(GitCheckUpdates.class);

	private static final String REMOTE_URL = "https://github.com/ag88/embtomcatwebdav.git/info/refs?service=git-upload-pack";

	/**
	 * nested Class Version.
	 */
	class Version implements Comparable<Version> {
				
		/** The major. */
		public int major;
		
		/** The minor. */
		public int minor;
		
		/** The patch. */
		public int patch;
		
		/** The subpatch. */
		public String subpatch;
		
		/**
		 * Instantiates a new version.
		 */
		public Version() {
		}
		
		/**
		 * Instantiates a new version.
		 *
		 * @param major the major
		 * @param minor the minor
		 * @param patch the patch
		 * @param subpatch the subpatch
		 */
		public Version(int major, int minor, int patch, String subpatch) {
			this.major = major;
			this.minor = minor;
			this.patch = patch;
			this.subpatch = subpatch;
		}
		
		/**
		 * Compare to.
		 *
		 * @param o the o
		 * @return the int
		 */
		@Override
		public int compareTo(Version o) {
			if (this.major > o.major)
				return 1;
			else if (this.major < o.major)
				return -1;
			
			if (this.minor > o.minor)
				return 1;
			else if (this.minor < o.minor)
				return -1;
			
			if (this.patch > o.patch)
				return 1;
			else if (this.patch < o.patch)
				return -1;

			int subp = this.subpatch.compareTo(o.subpatch); 
			if ( subp > 0 || subp < 0)
				return subp;
			
			return 0;
		}
		
		/**
		 * To string.
		 *
		 * @return the string
		 */
		@Override
		public String toString() {
			return Integer.toString(major) + "." + Integer.toString(minor) + "." + Integer.toString(patch) + subpatch;
		}
		
		
		/**
		 * From string.
		 *
		 * @param string the string
		 */
		public void fromString(String string) {
			Pattern p = Pattern.compile("[a-zA-Z]*(\\d*)\\.(\\d*)\\.(\\d*)([a-zA-Z]*)");
			Matcher m = p.matcher(string);
			if(m.matches()) {
				if(m.groupCount() > 0)
					this.major = Integer.parseInt(m.group(1));
				if(m.groupCount() > 1)
					this.minor = Integer.parseInt(m.group(2));
				if(m.groupCount() > 2)
					this.patch = Integer.parseInt(m.group(3));
				if(m.groupCount() > 3)
					subpatch = m.group(4);
			}				

		}
	}
	
	/** The versions. */
	ArrayList<Version> versions;
	
	/** The instance. */
	private static GitCheckUpdates m_instance;

	/**
	 * constructor (private)
	 */
	private GitCheckUpdates() {
		versions = new ArrayList<GitCheckUpdates.Version>(10);
		getversions();
	}

	/**
	 * Gets the single instance of GitCheckUpdates.
	 *
	 * @return single instance of GitCheckUpdates
	 */
	public static GitCheckUpdates getInstance() {
		// Double lock for thread safety.
		if (m_instance == null) {
			synchronized (GitCheckUpdates.class) {
				if (m_instance == null) {
					m_instance = new GitCheckUpdates();
				}
			}
		}
		return m_instance;
	}
	
	
	/**
	 * Checks for updates.
	 *
	 * @return true, if successful
	 */
	public boolean hasUpdates() {
		App app = App.getInstance();
		Map<String, String> mkv = app.readManifest();
		String vs = mkv.get("version");

		if(vs == null) return false;
		
		Version thisv = new Version();
		thisv.fromString(vs);
		
		if (versions == null) {
			versions = new ArrayList<GitCheckUpdates.Version>(10);
			getversions();
		}
		
		if (versions.size() == 0) return false;
		
		Version latest = versions.get(versions.size()-1);
		
		log.debug("latest " + latest);
		log.debug("curr " + thisv);
		
		if (latest.compareTo(thisv) > 0)
			return true;
		
		return false;
	}
	
	/**
	 * Gets the versions, update instance arraylist of versions
	 *
	 */
	public void getversions() {		
		
		List<String> lines = dohttpget();
		if(lines != null) {
			for(String l: lines) {
				log.debug(l);
				Version v = parseline(l);
				if (v != null)
					versions.add(v);							
			}
			Collections.sort(versions);
			/* debug 
			StringBuilder sb = new StringBuilder(100);			
			for(Version v: versions) {
				sb.append(v);
				sb.append(System.lineSeparator());
			}
			log.info(sb);
			*/			
		}
	}
	
	/**
	 * Latestversion.
	 *
	 * @return the version
	 */
	public Version latestversion() {
		if (versions == null) {
			versions = new ArrayList<GitCheckUpdates.Version>(10);
			getversions();
		}
		
		if(versions.size()>0)
			return versions.get(versions.size()-1);
		else
			return null;
	}
	
	/**
	 * Parseline.
	 *
	 * @param line the line
	 * @return the version
	 */
	public Version parseline(String line) {
		
		String[] tok = line.split(" ");
		if (tok.length > 1) {
			String t = tok[1];
			if(t.startsWith("refs/tags/")) { 
				t = t.substring(10);
				Version v = new Version();
				v.fromString(t);
				return v;
			}
		}
		
		return null;
	}
	
	/**
	 * Dohttpget.
	 *
	 * @return the list
	 */
	public List<String> dohttpget() {

		try {
			URL url = new URL(REMOTE_URL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			int status = con.getResponseCode();			

			ArrayList<String> lines = new ArrayList<String>(20);
			if(status == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line;
				while ((line = in.readLine()) != null) {
					lines.add(line);				
				}
				in.close();			
				con.disconnect();
				return lines;
			} else {
				log.info("status:" + Integer.toString(status));
				return null;
			}						

		} catch (MalformedURLException e) {
			log.warn(e);
		} catch (IOException e) {
			log.warn(e);
		}
		
		return null;

	}

	/**
	 * Gets the versions.
	 *
	 * @return the versions
	 */
	public ArrayList<Version> getVersions() {
		return versions;
	}

	/**
	 * Sets the versions.
	 *
	 * @param versions the new versions
	 */
	public void setVersions(ArrayList<Version> versions) {
		this.versions = versions;
	}
	
	
}
