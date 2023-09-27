A WebDAV server based on Apache Tomcat's Webdav servlet and embedded Tomcat server
---

This is a WebDAV server based on Apache Tomcat's WebDAV servlet and embedded Tomcat server

Current status: alpha/test

## Feature list
- It runs on http://localhost:8080/webdav
- It serves the current working directory in which the app is started
- Added (BASIC) authentication
- Added (DIGEST) authentication - v0.3.0, v0.2.1
- Added command line aguments so that various parameters can be changed:
- Added SSL (i.e. https://) - v0.2.0
- Added option to change urlprefix '/webdav' - v0.3.1
- Added loading of options from properties file, and option to generate a default config file - v0.3.2
- Added a dialog to generate DIGEST passwords that can be used in the config file. - v0.3.2
- v0.3.3 is a rather major bugfix release, this release is released to maven central
- v0.3.3 this app can be embedded, see the junit test cases to see how that is done
- v0.3.3 added runserverfork() method which lets apps embedding this to run the server in a separate thread.
- v0.3.4 fixed a bug related to race conditions in isRunning() method and runserverfork()
- v0.4.1 is a a rather major refactored release, this release is released to maven central
- v0.4.1 added a refactored command line options and config properties processing engine, this makes it feasible 
  for apps linking the library to add command line options and config properties in the same app.
- v0.4.1 use console.readPassword() to avoid displaying the password in the console during entry. 
  This is more secure in case there is a crowd nearby ;)  
  Note that System.console() isn't available in all situations, e.g. in various IDEs,
  in that case it falls back to reading from System.in, in which case character echo can't be avoided.
- v0.5.0 is a rather major feature release
- v0.5.0 added an Upload servlet that includes a form based file upload in the directory list.
  This makes it possible to upload files without a WebDAV client. In addition, it is styled with
  responsive html and css so that it is more readable on small (mobile) devices.
- It requires a folder 'tomcat.port' for the embedded Tomcat instance, if the folder isn't present,
it is created.
- v0.5.1 is a bugfix release for v0.5.0, Upload servlet, some refactoring: 
  enabled alwaysUseSession for authentication, some mobile devices do not cache authentication and keeps prompting 
  authentication every refresh and page. This is still as secure (managed by a session) and avoided the annoying 
  auth prompts every screen. Login only at the start, and for cookie tests (needed for jesssion), only checks in 
  doPost() where it is needed and only if it is a new (invalid) session.
- v0.5.2 added(fixed) sorting in Upload servlet
- v0.6.0 added access log

![screenshot in a browser](web/screenshot.jpg "Screen shot")

## Build
```
mvn package
```
## Run

```
java -jar embtomcatwebdav-0.6.0.jar
```
Note that if you build from source the file name is embtomcatwebdav-0.6.0-jar-with-dependencies.jar, in target/ folder.

## usage 

```
java -jar embtomcatwebdav-0.6.0.jar -h

usage: embtomcatwebdav-0.6.0
    --accesslog                  enable access log
 -b,--basedir <path>             set basedir, a work folder for tomcat,
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
 -U,--uploadservlet              use upload servlet 
 -w,--passwd <password>          set password, you may omit this, it would
                                 prompt for it if -u is specified
 -x,--urlprefix <urlprefix>      set urlprefix, default /webdav
```

note that the app can be run without specifying arguments.

requires JDK >= 1.8

Note that on starting, it creates a folder "tomcat.port" in the current directory. This is needed for the embedded Tomcat instance to operate.
And it serves the current directory on webdav at http://localhost:8080/webdav

On running, point the web browser to http://localhost:8080/webdav, you should see the directory listing of your current work directory. For more functionality, it requires a WebDAV client to interact with the WebDAV server

To enable authentication, specify a userid using -u option and password would be prompted if the -w option is not specified.

Version 0.2.1 added DIGEST authentication, to use DIGEST authentication, pass the -D option in addition to specifying the user with -u option. Without SSL, DIGEST authentication is slightly more secure than BASIC authentication in that passwords is not transmited in plain text. However, it requires that the client supports DIGEST authentication.

Version 0.3.2, you can maintain the options/settings in a properties text file. First generate a template using
the ``--genconf configfile``  option, this would generate a config text file template with default entries. You can then edit the entries as desired. Then while running it use the ``-c configfile`` option to load the configs from the properties file.

Version 0.3.2 also added a ``--genpass`` dialog feature, this lets you generate a DIGEST password to be used with DIGEST authentication.  This generated hashed password, including the 'digest(xxx)' wrapper text, can be maintained
in the password field in the properties config file. Set digest to true as it otherwise defaults to BASIC 
authentication. Note that this DIGEST hashed password won't work with BASIC authentication as that requires a clear
text password. The benefit here is that it is possible to store the hashed password rather than clear text password
in the config file if you use DIGEST authentication. This is more secure than storing plaintext passwords in
config files.

![Generate digest password](web/digestpwdlg.png "Generate digest password")

If you are bothered with illegal reflective access operation warnings, you can use the batch file run.bat or run.sh
replace the "target/webdav-0.x.x-jar-with-dependencies.jar" with the appropriate release jar, that should mute most of those illegal reflective access warnings. Tomcat needs those reflective access which accounts for its versetile flexibility features. 

## Upload servlet

v0.5.0 is a rather major feature release. v0.5.0 added an Upload servlet that includes a form based file upload 
in the directory list.  This makes it possible to upload files without a WebDAV client. In addition, it is 
styled with responsive html and css so that it is more readable on small (mobile) devices.

v0.5.1 is a bugfix release for v0.5.0, Upload servlet, some refactoring: 
enabled alwaysUseSession for authentication, some mobile devices do not cache authentication and keeps prompting 
authentication every refresh and page. This is still as secure (managed by a session) and avoided the annoying 
auth prompts every screen. Login only at the start, and for cookie tests (needed for jesssion), only checks in 
doPost() whre it is needed and only if it is a new (invalid) session.

v0.5.2 added(fixed) sorting in Upload servlet

To use the Upload servlet, add the -U (case sensitive) or --uploadservlet option on the command line.
It can also be maintained in the config (properties text) file using the ``uploadservlet=true`` parameter.

The Upload servlet is derived from Apache Tomcat's WebDAV servlet and DefaultServlet. It is customised to handle
a file upload form at the bottom of the directory list. Note that this is not intend to handle large files say more 
than a couple of megabytes and there may be issues with very large files. It works well for ordinary file sizes from say
10s of kbytes to a few megs. There could also be issues uploading too many files in a single upload.

It is advisable to use authentication e.g. specify a username and password and/or with the --digest option,
as otherwise any user with access to the upload servlet website can upload files.
This upload servlet is intended to be used in an internal network e.g. a home network or a small office network,
and is possibly insufficiently secure to be placed on the open internet.
A convenient use case could be to run the app as and when you need it say to upload some files from a mobile phone
and shutdown the app/server after that.

To access the servlet on your PC / desktop / laptop etc, use the -H or --host option and specify the local IP address 
of your host (PC/desktop/laptop). Otherwise, Apache Tomcat inteprets that as a virtual host and there could be issues
if the url don't match the virtual hostname. To get your ip address on Windows, a command is commonly
 `ipconfig` and on Linux `ip -4 add`.

![screenshot in a browser](web/UploadServlet.png "Upload servlet")  
Upload servlet on a desktop web browser

![screenshot in a browser](web/UploadServPhone.jpg "Upload on a phone")  
Upload servlet on a phone web browser

## Accesslog

v0.6.0 added access log

Accesslog can be enabled via the --accesslog option (or accesslog=true in the config file). 
By default, the accesslog is saved in the tomcat work directory "tomcat.port". 
Some configurations as described in the 
[Access_Log_Valve](https://tomcat.apache.org/tomcat-8.5-doc/config/valve.html#Access_Log_Valve) 
docs. Three of the configuration options can be specified in the config file.
```
# the access log dir this defaults to the tomcat work dir if not specified e.g.
accesslog.dir: c:\embtomcatwebdav
# this defaults to -1 which does not rotate by default
# other values is per Apache Tomcat's Access_Log_Valve
accesslog.days=-1
# this defaults to rotate, if set to false the behavior is per Apache Tomcat's Access_Log_Valve
accesslog.rot=true
```

## SSL

Note that using SSL is deemed an advanced topic. While I've done some tests with this, there is no assurance if this would work in any serious context, i.e. No Warranty.

To use SSL, you need to generate a certificate in a  keystore file as follows

Windows
```
"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keyalg RSA
  -keystore keystorefile.jkf
```

Unix:
```
$JAVA_HOME/bin/keytool -genkey -alias tomcat -keyalg RSA
  -keystore keystorefile.jkf
```
Note that keytool is normally bundled with JDK distributions, it is not part of this App.

Next run the app with -S option and the keystore file. If the keystore password is not
specified on the command line, it would prompt for it. e.g.
```
java -jar embtomcatwebdav-0.6.0.jar -p 8443 -S keystorefile.jkf 
```
Note that when you run the app with the -S keystorefile.jkf option, it copies the keystore file into the 'tomcat.port' work folder, this is needed for the app to access the keystore file.

Next in the web browser you can goto https://localhost:8443/webdav  
Note that the web browser will complain that the web site is not secure even if it is successful as this is a self signed certificate. But accordingly, there is likely some form of encryption.

The complete procedure would involve installing a certificate from a CA which is beyond the scope of covering about it for this little app. For more details, review the Tomcat SSL howto  
https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html  
https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html#Installing_a_Certificate_from_a_Certificate_Authority

Generally, for temporary use it'd be costly/overwhelming to get a true CA signed cert. In addition, browsers and apps may validate the cert against DNS data that the domain names matches the certs etc, this could make it practically unfeasible to run https:// SSL on an arbitrary device/system.

There are rather tricky ways to be your own CA, make certs. But it may involve installing your own CA root certs manually on the client devices/browsers etc. This again is beyond scope of covering it here, but a web (e.g. Google) search may get you some leads on how to do that.

## Development/Embedding

This release is released to maven central
https://central.sonatype.com/artifact/io.github.ag88/embtomcatwebdav/0.6.0
```
<dependency>
    <groupId>io.github.ag88</groupId>
    <artifactId>embtomcatwebdav</artifactId>
    <version>0.6.0</version>
</dependency>
```
v0.3.3 added runserverfork() method which lets apps embedding this to run the server in a standalone thread.
By default, runserver() method blocks, apps embedding this can call runserverfork() instead.

v0.3.4 fixed a bug related to race conditions in isRunning() method and runserverfork().
use this or later releases to avoid the runserverfork() bug.

This app can be embedded, to see how this can be done, take a look at the unit tests in
[src/test/java](src/test/java/io/github/ag88/embtomcatwebdav/WebDavServerTest.java).
Basically, create a new [WebDavServer](src/main/java/io/github/ag88/embtomcatwebdav/WebDavServer.java)
object, setup all the instance variables as desired using the getters and setters methods and call [runserver()](https://github.com/ag88/embtomcatwebdav/blob/99163bd8e5a90691da2f51351068a3677e32f102/src/main/java/io/github/ag88/embtomcatwebdav/WebDavServer.java#L185) or
[runserverfork()](https://github.com/ag88/embtomcatwebdav/blob/99163bd8e5a90691da2f51351068a3677e32f102/src/main/java/io/github/ag88/embtomcatwebdav/WebDavServer.java#L336) method.

``runserverfork()`` method returns a [WebDAVServerThread](src/main/java/io/github/ag88/embtomcatwebdav/WebDAVServerThread.java) object. This is the thread the server is running on. You may want to keep it in an instance variable for shutdown later, e.g.
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
// shutdown the server
thread.getServer().stopserver();
thread.interrupt(); 
```

more details in the [Wiki](https://github.com/ag88/embtomcatwebdav/wiki/Development-Embedding)

## Attributions

The server is really [Apache Tomcat https://tomcat.apache.org/](https://tomcat.apache.org/).
One of the most widely used open-source implementation of the Jakarta Servlet, JSP technologies.

## No warranty

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Origin

github repository: 
[https://github.com/ag88/embtomcatwebdav](https://github.com/ag88/embtomcatwebdav)

I've made efforts to tailor the WebDAV servlet so that it works as a standalone app. 
Developing this has taken quite some effort mainly in adding features, getting it to work and rather extensive tests. if you use this app and found it useful, i'd really appreciate it if you could support my efforts [![Donate](web/donorbox.png)](https://donorbox.org/embedded-tomcat-webdav-server).
you could also help simply starring this repository ;)
