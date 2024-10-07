package io.github.ag88.embtomcatwebdav.util;

import java.io.IOException;
import java.util.Properties;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelUtil {

	Log log = LogFactory.getLog(VelUtil.class);
	
	private static VelUtil m_instance;

	private VelUtil() {
		Properties p = new Properties();
		p.put("resource.loaders", "class");
		p.put("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		Velocity.init(p);
	}

	public static VelUtil getInstance() {
		if (m_instance == null) {
			synchronized (VelUtil.class) {
				if (m_instance == null) {
					m_instance = new VelUtil();
				}
			}
		}
		return m_instance;
	}

	public Template loadvmtemplate(String filename) throws IOException {
		
		Template template = null;
		try {
			template = Velocity.getTemplate(filename);
			return template;
		} catch (ResourceNotFoundException rnfe) {
			// couldn't find the template
			log.error("cannot find template ".concat(filename));
			log.error(rnfe);
			throw new IOException(rnfe);
		} catch (ParseErrorException pee) {
			// syntax error: problem parsing the template
			log.error("template parse error ".concat(filename));
			log.error(pee);
			throw new IOException(pee);
		} catch (MethodInvocationException mie) {
			// something invoked in the template
			// threw an exception
			throw new IOException(mie);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
