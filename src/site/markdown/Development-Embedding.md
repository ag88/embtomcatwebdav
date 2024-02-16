# Development/Embedding

## Maven coordinates

This is released to maven central.
https://central.sonatype.com/artifact/io.github.ag88/embtomcatwebdav/0.8.0
```
<dependency>
    <groupId>io.github.ag88</groupId>
    <artifactId>embtomcatwebdav</artifactId>
    <version>0.8.0</version>
</dependency>
```
- v0.3.3 added runserverfork() method which lets apps embedding this to run the server in a standalone thread. By default, runserver() method blocks, apps embedding this can call runserverfork() instead.
- v0.3.4 fixed a bug related to race conditions in isRunning() method and runserverfork()
- v0.4.1 is a a rather major refactored release, this release is released to maven central
- v0.4.1 added a refactored command line options and config properties processing engine, this makes it feasible 
  for apps linking the library to add command line options and config properties in the same app.
- v0.5.0 is a rather major feature release
- v0.5.0 added an Upload servlet that includes a form based file upload in the directory list.
  This makes it possible to upload files without a WebDAV client. In addition, it is styled with
  responsive html and css so that it is more readable on small (mobile) devices.
- v0.5.1 is a bugfix release for v0.5.0, Upload servlet, some refactoring: 
  enabled alwaysUseSession for authentication, some mobile devices do not cache authentication and keeps prompting 
  authentication every refresh and page. This is still as secure (managed by a session) and avoided the annoying 
  auth prompts every screen. Login only at the start, and for cookie tests (needed for jesssion), only checks in 
  doPost() where it is needed and only if it is a new (invalid) session.
- v0.5.2 added(fixed) sorting in Upload servlet
- v0.6.0 added access log
- v0.6.1 usability updates for upload servlet:
  - limit authentication access to configured url prefix instead of /*,
    this helps to reduce authentication challenges for resources icons,
     e.g. favicon.ico etc for some browsers.
  - added upload feedback on the page for large/long uploads,
    add upload feedback for the upload servlet, so that
    it shows 'uploading...' once upload is clicked.  
- v0.6.2 usability updates for upload servlet:
  - added a link to upload section at the top, this help with long directory lists
    relieves from long scrolls to the bottom just for uploads

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

### How to access properties from the config file specified by the -c configfile option

With version v0.4.1, a refactored command line options and config properties processing engine is added, this makes it feasible for apps linking the library to add command line options and config properties in the same app.

An example is as follows:
```
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import io.github.ag88.embtomcatwebdav.WebDavServer;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;

public class App {

  public App() {
  }

  public void run(String[] args) {
    io.github.ag88.embtomcatwebdav.App embtcapp = new io.github.ag88.embtomcatwebdav.App();
    WebDavServer dav = OptFactory.getInstance().getWebDAVserv();

    embtcapp.parseargs(args);
    dav.loadparams(OptFactory.getInstance().getOpts());
    dav.runserver();
  }

  public static void main(String[] args) {
    App app = new App();
    app.run(args);
  }
}
```
With the above codes and say that your app is packaged with 
[Maven Assembly Plugin](https://maven.apache.org/plugins/maven-assembly-plugin/)
, that the above is also the main entry point for your app. After building the assembled jar archive, you could possibly run e.g. 
```
java -jar myapp-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h

usage: myapp-0.0.1-SNAPSHOT
 -b,--basedir <basedir>          set basedir, a work folder for tomcat,
                                 default [current working dir]/tomcat.port
 -c,--conf <configfile>          load properties config file
 -D,--digest                     use digest authentication
    --genconf <configfile>       generate properties config file
    --genpass                    dialog to generate digest password
 -h,--help                       help
 -H,--host <hostname>            set host
 -p,--port <port>                set port
 -P,--path <path>                set path, default current working dir
 -q,--quiet                      mute (most) logs
 -R,--realm <realmname>          set realm name, default 'Simple'
 -S,--secure <keystore,passwd>   enable SSL, you need to supply a keystore
                                 file and keystore passwd, if passwd is
                                 omitted it'd be prompted.
 -u,--user <username>            set user
 -w,--passwd <password>          set password, you may omit this, it would
                                 prompt for it if -u is specified
 -x,--urlprefix <urlprefix>      set urlprefix, default /webdav
```
With a few lines of codes above, you get a *full* app as like the distributed release jars.

The important parts of the code are in the 
```
  public void run(String[] args) {
    io.github.ag88.embtomcatwebdav.App embtcapp = new io.github.ag88.embtomcatwebdav.App();
    WebDavServer dav = OptFactory.getInstance().getWebDAVserv();

    embtcapp.parseargs(args);
    dav.loadparams(OptFactory.getInstance().getOpts());
    dav.runserver();
  }
```
These few calls needs to be run in that order to get the WebDAV server started up properly, 
and with the parameters properly loaded. 


The [parseargs(String[] args)](https://github.com/ag88/embtomcatwebdav/blob/4f1190a06dcb5e1e226c21ebc2d3f63e2151ffe8/src/main/java/io/github/ag88/embtomcatwebdav/App.java#L101) parses the command line using [commons cli](https://commons.apache.org/proper/commons-cli/)
. If the -c configfile option is specified, the configfile is loaded as well using java [Properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html).

The data is registered with the singleton
[OptFactory](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java)
 object.

The [OptFactory](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java) is a singleton object should be accessed with OptFactory.getInstance() method. This is *only* works properly if the main 
[io.github.ag88.embtomcatwebdav.App](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/App.java) object is used.

The [Properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) are available after the
[parseargs(String[] args)](https://github.com/ag88/embtomcatwebdav/blob/4f1190a06dcb5e1e226c21ebc2d3f63e2151ffe8/src/main/java/io/github/ag88/embtomcatwebdav/App.java#L101)
call.  
The [Properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) are accessible in the 
[OptFactory](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java) instance.


In the above example, edit ``public void run(String[] args) {`` as follows:
```
public void run(String[] args) {
  io.github.ag88.embtomcatwebdav.App embtcapp = new io.github.ag88.embtomcatwebdav.App();
  WebDavServer dav = OptFactory.getInstance().getWebDAVserv();

  embtcapp.parseargs(args);

  // this part is added
  Properties properties = OptFactory.getInstance().getProperties();
  if (properties != null) {
    PrintWriter writer = new PrintWriter(new FilterOutputStream(System.out) {@Override
      public void close() throws IOException {
        //don't close System.out
      }
    });
    properties.list(writer);
    writer.flush();
    writer.close();
  }
  // 

  dav.loadparams(OptFactory.getInstance().getOpts());
  dav.runserver();

}
```
sample run
```
java -jar myapp-0.0.1-SNAPSHOT-jar-with-dependencies.jar -c configfile.ini

Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.opt.OptFactory loadproperties
SEVERE: opt: path, invalid value: , replaced with: /path
Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.opt.OptFactory loadproperties
SEVERE: opt: basedir, invalid value: , replaced with: /path/tomcat.8082
-- listing properties --
basedir=
path=
password=pass
keystorepasswd=
urlprefix=/webdav
port=8082
host=localhost
digest=true
realm=myrealm
quiet=false
keystorefile=
user=user
Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: tomcat basedir: /path/tomcat.8082
Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: serving path: /path
Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: Realm name: myrealm
Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: Auth method: DIGEST
Apr 01, 2023 8:36:05 PM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: Webdav servlet running at http://localhost:8082/webdav/
...
```

### How to add a new command line parameter/variable in the app

With version v0.4.1, a refactored command line options and config properties processing engine is added, this makes it feasible for apps linking the library to add command line options and config properties in the same app.

This section is an addition to the above 
[How to access properties from the config file specified by the -c configfile option](Development-Embedding#how-to-access-properties-from-the-config-file-specified-by-the--c-configfile-option)
section. An example as follows:

Create a class that extends ``io.github.ag88.embtomcatwebdav.opt.Opt``.

```
import io.github.ag88.embtomcatwebdav.opt.Opt;
class MyOpt extends Opt {
  public MyOpt() {
    this.name = "myopt";
    this.description = "set myopt";
    this.defaultval = null;
    this.opt = "M";
    this.longopt = "myopt";
    this.argname = "value";
    // only 3 classes supported: String, Integer, Boolean
    this.valclazz = String.class;
  }
}
```
The option is identified by the name, the rest of the instance variables are inputs to 
[Commons CLI](https://commons.apache.org/proper/commons-cli/usage.html) to build an 
[Option](https://commons.apache.org/proper/commons-cli/apidocs/org/apache/commons/cli/Option.html).

The run() method as like the 
[previous section's](Development-Embedding#how-to-access-properties-from-the-config-file-specified-by-the--c-configfile-option)
 example is modified as follows:
```
public void run(String[] args) {
  io.github.ag88.embtomcatwebdav.App embtcapp = new io.github.ag88.embtomcatwebdav.App();
  WebDavServer dav = OptFactory.getInstance().getWebDAVserv();

  MyOpt myopt = new MyOpt();
  OptFactory.getInstance().addOpt(myopt);

  embtcapp.parseargs(args);

  if (OptFactory.getInstance().getOpt("myopt").getValue() != null) {
    System.out.println(String.format("opt: %s, value: %s", myopt.getName(),
      OptFactory.getInstance().getOpt("myopt").getValue().toString()
      ));
  }

  dav.loadparams(OptFactory.getInstance().getOpts());
  dav.runserver();		
}
```
The key is make a new object of ``MyOpt`` and register it with the [OptFactory](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java) singleton object,
using the [addOpt()](https://github.com/ag88/embtomcatwebdav/blob/4f1190a06dcb5e1e226c21ebc2d3f63e2151ffe8/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java#L331) method.
[OptFactory](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java)
should be accessed with OptFactory.getInstance() method. 
This is *only* works properly if the main [io.github.ag88.embtomcatwebdav.App](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/App.java) object is used.

Next call 
[parseargs(String[] args)](https://github.com/ag88/embtomcatwebdav/blob/4f1190a06dcb5e1e226c21ebc2d3f63e2151ffe8/src/main/java/io/github/ag88/embtomcatwebdav/App.java#L101) to parse the command line.
If the -c configfile option is specified, the configfile is loaded as well using java [Properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html) as well.

A sample run as is as follows
```
java -jar myapp-0.0.1-SNAPSHOT-jar-with-dependencies.jar -h

usage: myapp-0.0.1-SNAPSHOT
 -b,--basedir <basedir>          set basedir, a work folder for tomcat,
                                 default [current working dir]/tomcat.port
...
 -M,--myopt <value>              set myopt
...
```

```
java -jar myapp-0.0.1-SNAPSHOT-jar-with-dependencies.jar -M hello_world

opt: myopt, value: hello_world
...
```

The evaluated command line parameter values or property values is accessible in the [OptFactory](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java) singleton object, 
using the [getOpt()](https://github.com/ag88/embtomcatwebdav/blob/4f1190a06dcb5e1e226c21ebc2d3f63e2151ffe8/src/main/java/io/github/ag88/embtomcatwebdav/opt/OptFactory.java#L359) method, after [parseargs(String[] args)](https://github.com/ag88/embtomcatwebdav/blob/4f1190a06dcb5e1e226c21ebc2d3f63e2151ffe8/src/main/java/io/github/ag88/embtomcatwebdav/App.java#L101) call.  
The key used for retreval is the *name* instance variable as defined in the [Opt](https://github.com/ag88/embtomcatwebdav/blob/main/src/main/java/io/github/ag88/embtomcatwebdav/opt/Opt.java) object.
e.g. 
```
OptFactory.getInstance().getOpt("myopt").getValue().toString()
```

