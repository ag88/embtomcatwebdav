package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This test runs the server
 */
@TestMethodOrder(OrderAnnotation.class)
public class WebDavServerNoAuthTest {

	Logger log = Logger.getLogger(WebDavServer.class.getName());
	
	private WebDAVServerThread mthread;	
	
	private String host = "localhost";
	
	//change this port if it is in use
	private int port = 8083;
	
	private String path;
	
	private String basedir;
	
	private String urlprefix = "/webdav";
	
	private boolean quiet = false;
		
	private static WebDavServerNoAuthTest instance;
	
	private boolean verbose = true;
	
	/**
	 * clean up tempdir after tests
	 */
	private boolean cleartmp = true;

	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     * @throws IOException 
     */
    public WebDavServerNoAuthTest() throws IOException {
    	String builddir = System.getProperty("mvn.project.build.dir");
    	if(builddir == null)
    		builddir = System.getProperty("user.dir");
    	log.info(builddir);
    	Path tempdir = Paths.get(builddir, "unittest");     	
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	tempdir = tempdir.resolve(WebDavServerNoAuthTest.class.getSimpleName());
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	path = tempdir.toString();
    	basedir = path;
    }
    
    public static WebDavServerNoAuthTest getInstance() throws IOException {
    	if(instance == null)
    		instance = new WebDavServerNoAuthTest();
    	return instance;
    }
    
    
    @BeforeAll
    static void startnoauth(TestReporter reporter) throws InterruptedException, IOException {
    	WebDavServerNoAuthTest test = getInstance();
    	test.init(reporter);
    }
    
    private void init(TestReporter reporter) throws InterruptedException {
    	// this example also shows how to run the Tomcat server with WebDAV servlet 
    	// as an embedded app

    	// initialise the WebDAV server
    	WebDavServer server = new WebDavServer();
    	server.setPath(path); // this is the path served
    	server.setBasedir(basedir); // this is the comcat work folder
    	server.setHost(host);
    	server.setPort(port);
    	server.setUrlprefix(urlprefix);
    	server.setQuiet(quiet);
    	
    	// Run the server in its own thread
    	WebDAVServerThread thread = server.runserverfork();
    	
    	// save the thread in an instance variable for later use, e.g. shutdown
    	setServerThread(thread);
		reporter.publishEntry("state", "server started");    		
    }
    
    
    /**
     * Test connect with no authentication
     *
     */
    @Test
    @Order(1)
    public void testConnectnoauth(TestReporter reporter) throws ClientProtocolException, IOException {
    	
    	URI uri;
		try {
			uri = new URI("http", null, host, port, urlprefix, null, null);
			reporter.publishEntry("uri", uri.toString());
			
	    	CloseableHttpClient httpclient = HttpClients.createDefault();
	    	HttpGet httpget = new HttpGet(uri);
	    	CloseableHttpResponse response = httpclient.execute(httpget);
	    	boolean found = false;
	    	try {	    		
	    		StringBuilder sb = new StringBuilder(1024);
	    		BufferedReader reader = new BufferedReader(new InputStreamReader(
	    				response.getEntity().getContent()));
	    		String line = null;
	    		while((line = reader.readLine()) != null) {
	    			
	    			if(line.contains("Directory Listing For"))
	    				found = true;	    			
	    			sb.append(line);
	    			sb.append(System.lineSeparator());	    			
	    		}
	    		reader.close();
	    		assertThat(found, is(true));
	    			
	    		if(verbose)
	    			reporter.publishEntry("response",sb.toString());
	    	} finally {
	    		response.close();
	    	}
	    	
	    	
		} catch (URISyntaxException e) {
			fail(e.getMessage().concat(e.getStackTrace().toString()));			
		}    	
    }    
    
    
    @Test
    @Order(2)
    public void testShutdownRestart(TestReporter reporter) throws InterruptedException, IOException {
    	WebDavServerNoAuthTest test = getInstance();
    	test.getServerThread().getServer().stopserver();
    	test.getServerThread().interrupt();
    	
    	reporter.publishEntry("state", "server stopped");
    	
    	test.init(reporter);
    	
    }
    
    @AfterAll
    public static void stopWebDAVserver() throws IOException {
    	WebDavServerNoAuthTest test = WebDavServerNoAuthTest.getInstance();
    	Path tempdir = Paths.get(test.getPath());
    	test.getServerThread().getServer().stopserver();
    	test.getServerThread().interrupt();
    	
    	//clear up tempdir
    	if(test.isCleartmp())
    		Files.walkFileTree(tempdir, 
      	      new SimpleFileVisitor<Path>() {
      	        @Override
      	        public FileVisitResult postVisitDirectory(
      	          Path dir, IOException exc) throws IOException {
      	            Files.delete(dir);
      	            return FileVisitResult.CONTINUE;
      	        }
      	        
      	        @Override
      	        public FileVisitResult visitFile(
      	          Path file, BasicFileAttributes attrs) 
      	          throws IOException {
      	            Files.delete(file);
      	            return FileVisitResult.CONTINUE;
      	        }
      	    });

    }
          

	public WebDAVServerThread getServerThread() {
		return mthread;
	}

	public void setServerThread(WebDAVServerThread mthread) {
		this.mthread = mthread;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isCleartmp() {
		return cleartmp;
	}

	public void setCleartmp(boolean cleartmp) {
		this.cleartmp = cleartmp;
	}
        
}
