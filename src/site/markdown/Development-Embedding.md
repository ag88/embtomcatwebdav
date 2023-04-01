# Development/Embedding

## Maven coordinates

This is released to maven central.
https://central.sonatype.com/artifact/io.github.ag88/embtomcatwebdav/0.4.0
```
<dependency>
    <groupId>io.github.ag88</groupId>
    <artifactId>embtomcatwebdav</artifactId>
    <version>0.4.0</version>
</dependency>
```
- v0.3.3 added runserverfork() method which lets apps embedding this to run the server in a standalone thread. By default, runserver() method blocks, apps embedding this can call runserverfork() instead.
- v0.3.4 fixed a bug related to race conditions in isRunning() method and runserverfork()
- v0.4.0 is a a rather major refactored release, this release is released to maven central
- v0.4.0 added a refactored command line options and config properties processing engine, this makes it feasible 
  for apps linking the library to add command line options and config properties in the same app.


## Embedding

This app can be embedded, to see how this can be done, take a look at the unit tests in
[src/test/java](https://github.com/ag88/embtomcatwebdav/blob/main/src/test/java/io/github/ag88/embtomcatwebdav/WebDavServerTest.java).
Basically, create a new [WebDavServer](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/WebDAVServerThread.java)
object, setup all the instance variables as desired using the getters and setters methods and call [runserver()](https://github.com/ag88/embtomcatwebdav/blob/99163bd8e5a90691da2f51351068a3677e32f102/src/main/java/io/github/ag88/embtomcatwebdav/WebDavServer.java#L185) or
[runserverfork()](https://github.com/ag88/embtomcatwebdav/blob/99163bd8e5a90691da2f51351068a3677e32f102/src/main/java/io/github/ag88/embtomcatwebdav/WebDavServer.java#L336) method.

v0.3.4, v0.3.3 added runserverfork() method which lets apps embedding this to run the server in a standalone thread.
By default, runserver() method blocks, apps embedding this can call runserverfork() instead.
``runserverfork()`` method returns a [WebDAVServerThread](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/WebDAVServerThread.java)
 object. This is the thread the server is running on. You may want to keep it in an instance variable for shutdown later, e.g.
```
WebDavServer webdavserver = new WebDavServer();
// this is the path/directory/folder served
webdavserver.setPath(System.getProperty("user.dir")); 
// this is the Tomcat work folder, this must be a valid directory
// if this is null it would create a folder [user.dir]/tomcat.port 
// and use that as Tomcat's work folder
webdavserver.setBasedir(System.getProperty("user.dir")); 
webdavserver.setHost("localhost");
webdavserver.setPort(8080);
webdavserver.setUser("user");
webdavserver.setPasswd("password");
webdavserver.setUrlprefix("/webdav");
// the above setup the WebDAV server running at http://user:password@localhost:8080/webdav

// note that runserverfork() actually start and run the server in its own thread
WebDAVServerThread thread = webdavserver.runserverfork();
... do other things
// shutdown the server, this is optional, you may omit this
// runserver() and runserverfork() sets up a shutdown hook that runs stopserver() if 
// the process is interrupted. This is only needed if you want to shutdown the server
// from your app.
thread.getServer().stopserver();
thread.interrupt(); 
```
There are some race conditions as Tomcat takes some time to startup (possibly a few seconds).
Hence, for more stable embedded operations, after calling runserverfork(), use e.g. Thread.sleep(2000),
to pause for a while before doing other operations that interact with the server.

### How do I add my own servlets in the embedded app

With version v0.3.4, currently additional servlets needs to be added after the server is running, 
i.e. after runserver() or runserverfork(). An example is as follows:
```
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import io.github.ag88.embtomcatwebdav.WebDAVServerThread;
import io.github.ag88.embtomcatwebdav.WebDavServer;

public class App 
{	
  public App() {		
  }
	
  public void run(String[] args) {
		
    WebDavServer webdavserver = new WebDavServer();
    // this is the path/directory/folder served
    webdavserver.setPath(System.getProperty("user.dir")); 
    // this is the Tomcat work folder, this must be a valid directory
    // if this is null it would create a folder [user.dir]/tomcat.port 
    // and use that as Tomcat's work folder
    webdavserver.setBasedir(System.getProperty("user.dir")); 
    webdavserver.setHost("localhost");
    webdavserver.setPort(8080);
    webdavserver.setUser("user");
    webdavserver.setPasswd("pass");
    webdavserver.setUrlprefix("/webdav");
    // the above setup the WebDAV server running at http://user:pass@localhost:8080/webdav
    		
    Servlet myservlet = new HttpServlet() {
      @Override
      protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
          resp.setContentType("text/html;charset=UTF-8");
    								
         PrintWriter out = resp.getWriter();				
         try {
           out.println("<html><head><title>hello world</title><head><body>");
           out.println("<h1>hello world</h1>");
           out.println("</body></html>");
           out.flush();
         } finally {					
           out.close();
         }				
      }			
    };
    		
    try {
      // note that runserverfork() actually start and run the server in its own thread
      WebDAVServerThread thread = webdavserver.runserverfork();
    			
      Tomcat tomcat = thread.getServer().getTomcat();
      Container container = tomcat.getHost().findChild(""); //get the root context
      if(container instanceof Context) {
        Context context = (Context) container;
        //this 'hello' is the name of your servlet
        Tomcat.addServlet(context, "hello", myservlet);
        //the first parameter is the http://prefix where you want to patch 
        //your servlet, the 2nd is the name of the above servlet
        String prefix = "/hello/*";
        context.addServletMappingDecoded(prefix, "hello");
      }
        
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }
	
  public static void main( String[] args )
  {
    App app = new App();
    app.run(args);
  }
}	
```
With the above codes, the wabdav servlet runs at http://localhost:8080/webdav, and your servlet runs at http://localhost:8080/hello , in the same app.

Note that in the interest of leaner binaries, WebDavServer drop the JSP container when setting up Tomcat. In addition, various Servlet API 3.0+ java annotations may not be scanned. Hence, mainly POJO java servlets are supported.
