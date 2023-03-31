package io.github.ag88.embtomcatwebdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.http.util.ByteArrayBuffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import io.github.ag88.embtomcatwebdav.opt.Opt;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.LoggingListener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;


import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This test runs the server
 */
@TestMethodOrder(OrderAnnotation.class)
public class OptTest {

	Logger log = Logger.getLogger(OptTest.class.getName());		
	
	App app;
	
	Path tempDir;
	
	boolean verbose = true;
	
	public OptTest() throws IOException {
    	String builddir = System.getProperty("mvn.project.build.dir");
    	if(builddir == null)
    		builddir = System.getProperty("user.dir");
    	log.info(builddir);
    	
    	Path tempdir = Paths.get(builddir, "unittest");     	
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	tempdir = tempdir.resolve(OptTest.class.getSimpleName());
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	setTempDir(tempdir);
	}
    
	@BeforeAll
	static public void beforeall(TestReporter reporter) throws Exception {
		
	}
    
	private void spoolopts(TestReporter reporter) {
		StringBuilder sb = new StringBuilder(1024);
		Iterator<Opt> iter = OptFactory.getInstance().iterator();
		while (iter.hasNext()) {
			Opt opt = iter.next();
			sb.append(opt.toString());
			sb.append(System.lineSeparator());
		}
		reporter.publishEntry("opts", sb.toString());

	}
	
    @Test
    @Order(1)
    public void testDupOpt(TestReporter reporter) throws Exception {
    	OptFactory optfactory = OptFactory.getInstance();
    	optfactory.setChkdup(true);
    	
    	// there should be no exceptions registering opts
    	optfactory.registeropts();
    	
		if (verbose) 
			spoolopts(reporter);

		//test dup
    	Opt opt = new Opt("dup", "dup", "h", "help", null,
    			String.class, null, null, false, Opt.PropType.Norm, false, false, 50);
    	
    	try {
    		optfactory.addOpt(opt);
    		fail("there should be illegal arg exception");
    	} catch (IllegalArgumentException e) {
    		// ok no problem
    	}
    	
    }
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream orgOut = System.out;
    private final PrintStream orgErr = System.err;
    
    @Test
    //@Order(2)
    public void testHelpOpt(TestReporter reporter) throws Exception {
    	OptFactory.getInstance().setChkdup(false);
    	
    	this.app = new App();
    	
    	System.setOut(new PrintStream(outContent));
    	System.setErr(new PrintStream(errContent));
    	
    	String[] args = { "-h" };
    	app.parseargs(args);

    	outContent.flush();
    	errContent.flush();
    	
    	System.setOut(orgOut);
    	System.setErr(orgErr);
    	
    	String helptxt = outContent.toString();
    	
    	if(verbose) {
    		reporter.publishEntry("args", Arrays.toString(args));
    		reporter.publishEntry("out", helptxt);
    	}
    	
    	outContent.reset();
    	errContent.reset();
    	
    	assertThat(helptxt.contains("usage:"),is(true));
    	
    }
    
        
    @Test
    //@Order(2)
    public void testCmdline1(TestReporter reporter) throws Exception {
    	OptFactory.getInstance().setChkdup(false);
    	
    	this.app = new App();
    	
    	
    	String[] args = { "-p", "1111", "-H", "host1", "-x", "prefix", 
    			"-u", "user1", "-w", "pass1", "--realm", "realm1", "-D", "--quiet"};
    	app.parseargs(args);
    	
    	OptFactory factory = OptFactory.getInstance();
		if (verbose) {			
			StringBuilder sb = new StringBuilder(1024);
			sb.append(factory.getOpt("host").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("port").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("urlprefix").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("user").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("realm").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("password").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("digest").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("quiet").getValue());
			sb.append(System.lineSeparator());
			reporter.publishEntry("test", Arrays.toString(args));
			reporter.publishEntry("test", sb.toString());
		}
    	
    	assertThat(factory.getOpt("host").getValue(), equalTo("host1"));
    	assertThat(factory.getOpt("port").getValue().toString(), equalTo("1111"));
    	assertThat(factory.getOpt("urlprefix").getValue(), equalTo("prefix"));
    	assertThat(factory.getOpt("user").getValue(), equalTo("user1"));
    	assertThat(factory.getOpt("realm").getValue(), equalTo("realm1"));
    	assertThat(factory.getOpt("password").getValue(), equalTo("pass1"));
    	assertThat(factory.getOpt("digest").getValue(), is(true));
    	assertThat(factory.getOpt("quiet").getValue(), is(true));
    }

    @Test
    //@Order(3)
    public void testConfOpt(TestReporter reporter) throws Exception {
    	OptFactory.getInstance().setChkdup(false);
    	
    	this.app = new App();
    	
    	String configfile = Paths.get("src","test","resources","wdav.ini").toString();
    	reporter.publishEntry("config file", configfile);
    	
    	
    	String[] args = { "-c", configfile };
    	app.parseargs(args);
    	
    	OptFactory factory = OptFactory.getInstance();
		if (verbose) {			
			StringBuilder sb = new StringBuilder(1024);
			sb.append(factory.getOpt("host").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("port").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("urlprefix").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("user").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("realm").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("password").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("digest").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("quiet").getValue());
			sb.append(System.lineSeparator());
			reporter.publishEntry("test", Arrays.toString(args));
			reporter.publishEntry("test", sb.toString());
		}
    	
    	assertThat(factory.getOpt("host").getValue(), equalTo("host1"));
    	assertThat(factory.getOpt("port").getValue().toString(), equalTo("2222"));
    	assertThat(factory.getOpt("urlprefix").getValue(), equalTo("/prefix1"));
    	assertThat(factory.getOpt("user").getValue(), equalTo("user1"));
    	assertThat(factory.getOpt("realm").getValue(), equalTo("realm1"));
    	assertThat(factory.getOpt("password").getValue(), equalTo("pass1"));
    	assertThat(factory.getOpt("digest").getValue(), is(true));
    	assertThat(factory.getOpt("quiet").getValue(), is(false));

    }
    

    @Test
    //@Order(3)
    public void testConfOveride(TestReporter reporter) throws Exception {
    	OptFactory.getInstance().setChkdup(false);
    	
    	this.app = new App();
    	reporter.publishEntry("user.dir", System.getProperty("user.dir"));
    	
    	String configfile = Paths.get(System.getProperty("user.dir"),"src","test","resources","wdav.ini").toString();
    	reporter.publishEntry("config file", configfile);
    	
    	
    	String[] args = { "-c", configfile, "-p", "3333", "-H", "host3", "-x", "/prefix3", 
    			"--user", "user3", "--passwd", "pass3", "-R", "realm3", "--digest", "-q"};
    	app.parseargs(args);
    	
    	OptFactory factory = OptFactory.getInstance();
		if (verbose) {			
			StringBuilder sb = new StringBuilder(1024);
			sb.append(factory.getOpt("host").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("port").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("urlprefix").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("user").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("realm").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("password").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("digest").getValue());
			sb.append(System.lineSeparator());
			sb.append(factory.getOpt("quiet").getValue());
			sb.append(System.lineSeparator());
			reporter.publishEntry("test", Arrays.toString(args));
			reporter.publishEntry("test", sb.toString());
		}
    	
    	assertThat(factory.getOpt("host").getValue(), equalTo("host3"));
    	assertThat(factory.getOpt("port").getValue().toString(), equalTo("3333"));
    	assertThat(factory.getOpt("urlprefix").getValue(), equalTo("/prefix3"));
    	assertThat(factory.getOpt("user").getValue(), equalTo("user3"));
    	assertThat(factory.getOpt("realm").getValue(), equalTo("realm3"));
    	assertThat(factory.getOpt("password").getValue(), equalTo("pass3"));
    	assertThat(factory.getOpt("digest").getValue(), is(true));
    	assertThat(factory.getOpt("quiet").getValue(), is(true));

    }

    @AfterAll
	static public void aftereall(TestReporter reporter) throws Exception {
		
	}


	public App getApp() {
		return app;
	}


	public void setApp(App app) {
		this.app = app;
	}


	public Path getTempDir() {
		return tempDir;
	}


	public void setTempDir(Path tempDir) {
		this.tempDir = tempDir;
	}

    
        
}
