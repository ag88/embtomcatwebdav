package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.namespace.QName;

import org.apache.catalina.LifecycleState;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.model.Allprop;
import com.github.sardine.model.Propfind;
import com.github.sardine.util.SardineUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This test runs the server
 */
@TestMethodOrder(OrderAnnotation.class)
public class WebDavServerTest {

	Logger log = Logger.getLogger(WebDavServerTest.class.getName());
	
	private WebDAVServerThread thread;	
	
	private String host = "localhost";
	
	//change this port if it is in use
	private int port = 8082;
	
	private String path;
	
	private String basedir;
	
	private String user = "user";

	private String password = "password";
	
	private String urlprefix = "/files";
	
	private boolean quiet = false;
		
	private static WebDavServerTest instance;
	
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
    public WebDavServerTest() throws IOException {
    	String builddir = System.getProperty("mvn.project.build.dir");
    	if(builddir == null)
    		builddir = System.getProperty("user.dir");
    	log.info(builddir);
    	
    	Path tempdir = Paths.get(builddir, "unittest");     	
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	tempdir = tempdir.resolve(WebDavServerTest.class.getSimpleName());
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	path = tempdir.toString();
    	basedir = path;
    }
    
    public static WebDavServerTest getInstance() throws IOException {
    	if(instance == null)
    		instance = new WebDavServerTest();
    	return instance;
    }
    
    
    /**
     * start server with basic auth
     * @throws IOException 
     *
     */
    @BeforeAll
    static void startauth(TestReporter reporter) throws InterruptedException, IOException {
    	WebDavServerTest test = getInstance();
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
    	server.setUser(user);
    	server.setPasswd(password);
    	server.setUrlprefix(urlprefix);
    	server.setQuiet(quiet);    	

    	// Run the server in its own thread
    	WebDAVServerThread thread = server.runserverfork();
    	
    	// save the thread in an instance variable for later use, e.g. shutdown 
    	setServerThread(thread);
		reporter.publishEntry("state", "server started"); 
    	
    }
    
    private URI makeURI(String path) throws URISyntaxException {
    	return new URI("http", user.concat(":").concat(password), host, port, urlprefix.concat(path), null, null);
    }
    
    @Test
    @Order(1)
    public void testConnectAuth(TestReporter reporter) throws ClientProtocolException, IOException {
    	
    	
		try {
			URI uri = makeURI("");	
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
    
    /**
     * WebDAV tests, upload, download, delete, rename/move, createdir
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException 
     */
    @Test
    @Order(2)
    public void testfilesupdowndelmovemd(TestReporter reporter) throws IOException, URISyntaxException {
    	WebDavServerTest test = getInstance();
    	Path tempdir = Paths.get(test.getPath());
    	Path local = tempdir.resolve("local");
    	Files.createDirectory(local);
    	makefile(local.resolve("a.txt"),"aaa");
    	Path remote = tempdir.resolve("remote");
    	Files.createDirectory(remote);
    	makefile(remote.resolve("b.txt"), "bbb");
    	makefile(remote.resolve("c.txt"), "ccc");
    	
    	Sardine sardine = SardineFactory.begin(user, password);    	    	
    	
    	reporter.publishEntry("local a.txt path", local.resolve("a.txt").toString());
    	
    	URI uriA = makeURI("/remote/a.txt");
    	URI uriB = makeURI("/remote/b.txt");
    	URI uriC = makeURI("/remote/c.txt");
    	URI uriD = makeURI("/remote/d.txt");
    	    	
    	reporter.publishEntry("remote b.txt uri", uriB.toString());
    	reporter.publishEntry("remote c.txt uri", uriC.toString());
    	
    	// webdav tests
    	if(verbose)
    	reporter.publishEntry("download", String.format("%s -> %s", uriB.toString(), local.resolve("b.txt").toString()));
    	download(uriB.toString(), local.resolve("b.txt"), sardine);
    	if(verbose)
    	reporter.publishEntry("download", String.format("%s -> %s", uriC.toString(), local.resolve("c.txt").toString()));
    	download(uriC.toString(), local.resolve("c.txt"), sardine);
    	if(verbose)
    	reporter.publishEntry("upload", String.format("%s -> %s", local.resolve("a.txt").toString(), uriA.toString()));
    	upload(local.resolve("a.txt"), uriA.toString(), sardine);
    	if(verbose)
    	reporter.publishEntry("delete", String.format("remote %s", uriC.toString()));
    	
    	assertThat(sardine.exists(uriC.toString()), is(true));
    	sardine.delete(uriC.toString());
    	
    	URI uriDir = makeURI("/remote/newdir");
    	if(verbose)
    	reporter.publishEntry("create dir", String.format("remote %s", uriDir.toString()));
    	sardine.createDirectory(uriDir.toString());
    	assertThat(sardine.exists(uriDir.toString()), is(true));
    	
    	if(verbose)
    	reporter.publishEntry("rename/move", String.format("remote %s -> %s", uriB.toString(), uriD.toString()));
    	sardine.move(uriB.toString(), uriD.toString());
    	
    	//validate files
    	assertThat(containstext(remote.resolve("a.txt"), "aaa"),is(true));
    	
    	assertThat(containstext(remote.resolve("d.txt"), "bbb"),is(true));
    	
    	assertThat(containstext(local.resolve("b.txt"), "bbb"),is(true));
    	
    	assertThat(containstext(local.resolve("c.txt"), "ccc"),is(true));
    }
    
    private boolean containstext(Path path, String text) throws IOException {
    	List<String> lines =  Files.readAllLines(path);
    	for(String l : lines) {
    		if(l.contains(text)) return true;
    	}
    	return false;
    }
    		
    
    private void makefile(Path path, String text) throws IOException {
    	Writer writer = Files.newBufferedWriter(path);
    	writer.write(text);
    	writer.write(System.lineSeparator());
    	writer.flush();
    	writer.close();        	
    }
    
    private void download(String url, Path local, Sardine sardine) throws IOException {
    	BufferedReader reader = new BufferedReader( new InputStreamReader(sardine.get(url)));
    	BufferedWriter writer = Files.newBufferedWriter(local);
    	String line = null;
    	while((line = reader.readLine())!= null) {
    		writer.write(line);
    		writer.append(System.lineSeparator());    		
    	}
    	writer.flush();
    	writer.close();    	
    }
    
    /**
     * Testdirlisting.
     *
     */
    @Test
    @Order(3)
    public void testdirlisting(TestReporter reporter) throws IOException, URISyntaxException {
    	WebDavServerTest test = getInstance();
    	Path tempdir = Paths.get(test.getPath());

    	Sardine sardine = SardineFactory.begin(user, password);
    	URI uri = makeURI("/remote");
    	
    	if(verbose)
    	reporter.publishEntry("listdir", uri.toString());
    	List<DavResource> dirlist = sardine.list(uri.toString(), 1, true);
    	StringBuilder sb = new StringBuilder(1024);
    	TreeSet<String> names = new TreeSet<>();    	
    	for(DavResource r : dirlist) {
    		switch(r.getName()) {
    		case "a.txt":
    		case "d.txt":
    		case "newdir":
    			names.add(r.getName());
    			break;
    		default:
    			break;
    		}
    		sb.append(r.getName());
    		sb.append("\t");
    		sb.append(r.getContentLength());
    		sb.append("\t");
    		LocalDate date = r.getModified().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    		sb.append(date.toString());
    		sb.append("\t");
    		sb.append(r.isDirectory());
    		sb.append(System.lineSeparator());
    	}
    	if(verbose)
    	reporter.publishEntry("dir list", sb.toString());
    	
    	assertThat(names.size(), equalTo(3));
    	
    }
    
    
    private void upload(Path local, String url, Sardine sardine) throws IOException {
    	sardine.put(url,local.toFile(),"text/plain");
    }
    
    @AfterAll
    public static void stopWebDAVserver() throws IOException {
    	WebDavServerTest test = WebDavServerTest.getInstance();    	
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
		return thread;
	}

	public void setServerThread(WebDAVServerThread serverThread) {
		this.thread = serverThread;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public boolean isCleartmp() {
		return cleartmp;
	}

	public void setCleartmp(boolean cleartmp) {
		this.cleartmp = cleartmp;
	}

        
}
