package io.github.ag88.embtomcatwebdav.util;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.realm.MessageDigestCredentialHandler;

/**
 * The Class DigestPWUtil.
 * 
 * Utility class for digest auth passwords
 * 
 */
public class DigestPWUtil {

	public DigestPWUtil() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Generates encoded password for DIGEST authentication.
	 *
	 * @param realm the realm
	 * @param username the username
	 * @param password plaintext password
	 * @return encoded password for DIGEST authentication
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String digestEncodePasswd(String realm, String username, String password) 
			throws NoSuchAlgorithmException {
		
			String credentials = username.concat(":").concat(realm).concat(":").concat(password);
			MessageDigestCredentialHandler credhand = new MessageDigestCredentialHandler();
			credhand.setEncoding(StandardCharsets.UTF_8.name());
			credhand.setAlgorithm("MD5");
			credhand.setIterations(1);
			credhand.setSaltLength(0);
			return credhand.mutate(credentials);		
	}
	
	/**
	 *  
	 * returns an encoded (hashed) password for digest auth for storage<p>
	 * 
	 * not safe, but hashed so as to obfuscate the original password.
	 *
	 * @param realm Authentication Realm (for BASIC/DIGEST Authentication)
	 * @param username username
	 * @param password password
	 * @return encoded password for text storage
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String digestEncodeStoredPw(String realm, String username, String password)
			throws NoSuchAlgorithmException {
		String epw = digestEncodePasswd(realm, username, password);
		return "digest(".concat(epw).concat(")");
	}
	
	
	/**
	 * returns password encoded for digest authentication as a hex string<br>
	 * i.e. MD5(username:realm:password)<p>
	 * 
	 * if password is in format "digest(hexstring)", it is deemed pre-encoded and returned
	 *
	 * @param realm Authentication Realm (for BASIC/DIGEST Authentication)
	 * @param username username
	 * @param password password
	 * @return encoded password i.e. MD5(username:realm:password) as a HexString
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	public String digestPw(String realm, String username, String password) throws NoSuchAlgorithmException {
		String dpw = null;
		Pattern p = Pattern.compile("digest\\((.*)\\)");
		Matcher m = p.matcher(password); 
		if(m.matches()) {
			dpw = m.group(1);
		} else 
			dpw = digestEncodePasswd(realm, username, password);
		return dpw;
	}
	

}
