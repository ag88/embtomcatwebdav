package io.github.ag88.embtomcatwebdav.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Class FilenameGlob.
 * 
 * Create an object passing the pattern in the glob string.
 * The glob pattern match can then be checked using {@link matches}
 * this does substring search, e.g. matches anywhere in the input (e.g. filename)
 * The glob pattern string can have wildcards like 'first*last'.
 * 
 */
public class FilenameGlob {

	private Log log = LogFactory.getLog(FilenameGlob.class);
	
	Pattern p;

	/**
	 * Instantiates a new filename glob.
	 * 
	 * The glob pattern string and can have wildcards like '*'.
	 *
	 * @param glob the pattern string
	 */
	public FilenameGlob(String glob) {
		this.p = Pattern.compile(convertGlobToRegEx(glob));
	}

	/**
	 * Matches.
	 *
	 * return true glob pattern matches.
	 * this does substring search, e.g. matches anywhere in the input (e.g. filename)
	 * 
	 * @param input the input
	 * @return true, if successful
	 */
	public boolean matches(String input) {
		Matcher m = p.matcher(input);
		return m.find();
	}
	
	/**
	 * Convert glob to reg ex.
	 *
	 * credits to 
	 * <a href="https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns">
	 * https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns</a>
	 * 
	 * @param line the line
	 * @return the string
	 */
	protected String convertGlobToRegEx(String line) {
		log.debug("line:".concat(line));
		line = line.trim();		
		int strLen = line.length();
		StringBuilder sb = new StringBuilder(strLen);
		// Remove beginning and ending * globs because they're useless
		if (line.startsWith("*")) {
			line = line.substring(1);
			strLen--;
		}
		if (line.endsWith("*")) {
			line = line.substring(0, strLen - 1);
			strLen--;
		}
		boolean escaping = false;
		int inCurlies = 0;
		for (char currentChar : line.toCharArray()) {
			switch (currentChar) {
			case '*':
				if (escaping)
					sb.append("\\*");
				else
					sb.append(".*");
				escaping = false;
				break;
			case '?':
				if (escaping)
					sb.append("\\?");
				else
					sb.append('.');
				escaping = false;
				break;
			case '.':
			case '(':
			case ')':
			case '+':
			case '|':
			case '^':
			case '$':
			case '@':
			case '%':
				sb.append('\\');
				sb.append(currentChar);
				escaping = false;
				break;
			case '\\':
				if (escaping) {
					sb.append("\\\\");
					escaping = false;
				} else
					escaping = true;
				break;
			case '{':
				if (escaping) {
					sb.append("\\{");
				} else {
					sb.append('(');
					inCurlies++;
				}
				escaping = false;
				break;
			case '}':
				if (inCurlies > 0 && !escaping) {
					sb.append(')');
					inCurlies--;
				} else if (escaping)
					sb.append("\\}");
				else
					sb.append("}");
				escaping = false;
				break;
			case ',':
				if (inCurlies > 0 && !escaping) {
					sb.append('|');
				} else if (escaping)
					sb.append("\\,");
				else
					sb.append(",");
				break;
			default:
				escaping = false;
				sb.append(currentChar);
			}
		}
		log.debug("regex:".concat(sb.toString()));
		return sb.toString();
	}
}
