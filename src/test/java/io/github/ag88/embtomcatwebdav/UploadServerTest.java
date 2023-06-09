package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

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
public class UploadServerTest {

	Logger log = Logger.getLogger(UploadServerTest.class.getName());
	
	private WebDAVServerThread thread;	
	
	private String host = "localhost";
	
	//change this port if it is in use
	private int port = 8085;
	
	private String path;
	
	private String basedir;
	
	private String user = "user";

	private String password = "password";
	
	private String urlprefix = "/webdav";
	
	private boolean quiet = false;
		
	private static UploadServerTest instance;
	
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
    public UploadServerTest() throws IOException {
    	String builddir = System.getProperty("mvn.project.build.dir");
    	if(builddir == null)
    		builddir = System.getProperty("user.dir");
    	log.info(builddir);
    	
    	Path tempdir = Paths.get(builddir, "unittest");     	
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	tempdir = tempdir.resolve(UploadServerTest.class.getSimpleName());
    	if(!Files.exists(tempdir))
    		Files.createDirectories(tempdir);
    	path = tempdir.toString();
    	basedir = path;
    }
    
    public static UploadServerTest getInstance() throws IOException {
    	if(instance == null)
    		instance = new UploadServerTest();
    	return instance;
    }
    
    
    /**
     * start server with basic auth
     * @throws IOException 
     *
     */
    @BeforeAll
    static void startauth(TestReporter reporter) throws InterruptedException, IOException {
    	UploadServerTest test = getInstance();
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
    	server.setUploadservlet(true);

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
    public void testUploadServlet(TestReporter reporter) throws ClientProtocolException, IOException, URISyntaxException {
    	    	
			URI uri = makeURI("");	
			reporter.publishEntry("uri", uri.toString());
			
	    	RequestConfig globalConfig = RequestConfig.custom()
	    	        .setCookieSpec(CookieSpecs.DEFAULT)
	    	        .build();
	    	
	    	CookieStore cookieStore = new BasicCookieStore();
	    	// Set the store
	    	CloseableHttpClient httpclient = HttpClients.custom()
	    	        .setDefaultCookieStore(cookieStore)
	    	        .setDefaultRequestConfig(globalConfig)
	    	        .build();
	    	
	    	boolean status = connectAuth(reporter, httpclient, uri);
	    	assertThat(status, is(true));
	    	
	    	boolean found = false;
	    	for(Cookie c : cookieStore.getCookies()) {
				if (verbose) {
					StringBuilder sb = new StringBuilder(100);
					sb.append(c.getName());
					sb.append(", ");
					sb.append(c.getDomain());
					sb.append(", ");
					sb.append(c.getPath());
					sb.append(", ");
					sb.append(c.getValue());
					sb.append(", ");
					sb.append(c.getExpiryDate());
					reporter.publishEntry("cookie", sb.toString());
				}
				if(c.getName().equals("JSESSIONID")) {
					found = true;
					break;
				}					
	    	}
	    	assertThat(found, is(true));
	    	
	    	status = postmultipartupload(reporter, httpclient, uri);
	    	assertThat(status, is(true));
    }
    
    private boolean connectAuth(TestReporter reporter, CloseableHttpClient httpclient,  URI uri) 
    		throws ClientProtocolException, IOException {
    	
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
    	return found;
    }
    
    private boolean postmultipartupload(TestReporter reporter, CloseableHttpClient httpclient,  URI uri) 
    		throws ClientProtocolException, IOException, URISyntaxException {    	
    	UploadServerTest test = getInstance();
    	Path tempdir = Paths.get(test.getPath());

    	Path local = tempdir.resolve("local");
    	if(!Files.exists(local))
    		Files.createDirectory(local);
    	Path localfile = local.resolve("post.txt"); 
    	makefile(localfile, "post upload");
    	Path remote = tempdir.resolve("remote");
    	if(!Files.exists(remote))
    		Files.createDirectory(remote);
    	
    	boolean runok = false;
        try {
        	
        	uri = new URI(uri.getScheme(),uri.getUserInfo(),uri.getHost(),uri.getPort(),
        			uri.getPath().concat("/remote/"),null,null);
        	reporter.publishEntry("post uri", uri.toString());
        	
            HttpPost httppost = new HttpPost(uri); 
            
            FileBody bin = new FileBody(localfile.toFile());
            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
            
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("bin", bin)
                    .addPart("comment", comment)
                    .build();

            httppost.setEntity(reqEntity);

            reporter.publishEntry("run", "executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {                
                reporter.publishEntry("status", response.getStatusLine().toString());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    reporter.publishEntry("Response content length: ", Long.toString(resEntity.getContentLength()));
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
        
        runok = containstext(remote.resolve("post.txt"), "post upload");
        assertThat(runok,is(true));
        
        return runok;
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
    	UploadServerTest test = getInstance();
    	Path tempdir = Paths.get(test.getPath());

    	Path local = tempdir.resolve("local");
    	if(!Files.exists(local))
    		Files.createDirectory(local);
    	Path remote = tempdir.resolve("remote");
    	if(!Files.exists(remote))
    		Files.createDirectory(remote);

    	makefile(local.resolve("a.txt"),"aaa");
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
    	UploadServerTest test = getInstance();
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
    	UploadServerTest test = UploadServerTest.getInstance();    	
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
