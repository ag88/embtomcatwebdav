# Extended README

## Command line CLI details, usage notes

Embtomcatwebdav v1.0.0 and newer provides a QR Code Gui. The command like CLI is still available,
details as follows:

```
java -jar embtomcatwebdav-1.0.0.jar -h

usage: embtomcatwebdav-1.0.0
    --accesslog                  enable access log
    --allowlinking               allow linking (e.g. follow symbolic
                                 links, warn: links can go outside working
                                 dir/path, only works in upload servlet)
 -b,--basedir <basedir>          set basedir, a work folder for tomcat,
                                 default [current working dir]/tomcat.port
 -c,--conf <configfile>          load properties config file
 -D,--digest                     use digest authentication
    --genconf <configfile>       generate properties config file
    --genpass                    dialog to generate digest password
 -h,--help                       help
 -H,--host <hostname>            set host. Default localhost. If you set
                                 the host to 0.0.0.0 it will listen on all
                                 interfaces
                                 however, you still need to specify the IP
                                 address of this host PC from your web
                                 browser
                                 to access the webdav or upload servlet
                                 web page
 -p,--port <port>                set port. Default 8080
 -P,--path <path>                set path, default: current working dir.
                                 This is the root directory which is
                                 accessible in the webdav or upload
                                 servlet
 -q,--quiet                      mute (most) logs
 -R,--realm <realmname>          set realm name, default 'Simple'
 -S,--secure <keystore,passwd>   enable SSL, you need to supply a keystore
                                 file and keystore passwd, if passwd is
                                 omitted it'd be prompted.
 -u,--user <username>            set user. If the user is specified, the
                                 app will prompt for authentication
 -U,--uploadservlet              use upload servlet. Default: true.
                                 specifying on command line, toggles it
                                 off
                                 in config file, specify it directly false
                                 or true to use the upload servlet
 -w,--passwd <password>          set password, on the command line you may
                                 omit this, it would prompt for it if -u
                                 is specified
                                 In the config file, if you leave it empty
                                 as "password=" it is actually a blank
                                 password.
                                 To make it prompt for password from the
                                 config file, comment it with a # in
                                 front.
 -x,--urlprefix <urlprefix>      set urlprefix, default /webdav
 ```
requires JDK >= 1.8

 
note that the app can be run without specifying arguments. From embtomcatwebdav v1.0.0 and newer, it would create and
use a default config file and present a setup Wizard the first time you run it.

For older releases than v1.0.0

Note that on starting, it creates a folder "tomcat.port" in the current directory. This is needed for the embedded 
Tomcat instance to operate. And it serves the current directory on webdav at http://localhost:8080/webdav. 
In 1.0.0 and later, the path and work folder is configured in a setup wizard dialog.

On running, point the web browser to http://localhost:8080/webdav, you should see the directory listing of your 
current work directory. For more functionality, it requires a WebDAV client to interact with the WebDAV server

In v1.0.0 and later this step is unnecessary, it uses a default config file.
Prior v1.0.0, you can maintain the options/settings in a text (config) file. To use a config file, first generate
a template/config file using the ``--genconf configfile`` option, e.g.:

```
java -jar embtomcatwebdav-1.0.0.jar --genconf wdav.ini
```

You would get a config file as like that in the [run section](#run) above.

Thereafter, running the app with the config file e.g. wdav.ini is
```
java -jar embtomcatwebdav-1.0.0.jar -c wdav.ini
```

To enable authentication, specify a userid using -u option and password would be prompted if the -w option is not specified.
Authentication can be enabled in the config file by specifying the ``user=`` and ``password=`` option, note that if you 
leave the value blank, it is actually an empty user and/or password. Do specify a valid user and password if you intended
authentication for your webdav or upload web site. To do away with authentication, you can comment them with a `#` in the front.

Since v0.2.1 DIGEST authentication is supported, to use DIGEST authentication, pass the -D option in addition to specifying the user with -u option. Without SSL, DIGEST authentication is slightly more secure than BASIC authentication in that passwords is not transmited in plain text. However, it requires that the client supports DIGEST authentication. 

Since v0.3.2 also added a ``--genpass`` dialog feature, this lets you generate a DIGEST password to be used with DIGEST authentication. 

![Generate digest password](web/digestpwdlg.png "Generate digest password")

To use DIGEST authentication in the config file, generate a hashed password using the ``--genpass`` option, this generated hashed password, including the 'digest(xxx)' wrapper text, needs to be maintained in the config file as
``password=digest(xxxxxx)``. 

And in addition, set digest to true as it otherwise defaults to BASIC authentication. 

Note that this DIGEST hashed password won't work with BASIC authentication as BASIC auth requires a clear text password.
The benefit here is that it is possible to store the hashed password rather than clear text password
in the config file if you use DIGEST authentication. This is more secure than storing plaintext passwords in
config files and that passwords are not transmitted as plaintext across the network.

If you are bothered with illegal reflective access operation warnings, you can use the batch file run.bat or run.sh
replace the "target/embtomcatwebdav-1.0.0.jar" with the appropriate release jar path, that should mute most of those illegal reflective access warnings. Tomcat needs those reflective access which accounts for its versetile flexibility features. 

## Upload servlet

v0.8.2 The upload servlet is now the default in the app when you use it.
There is an option on the command line ``-U`` for upload servlet. If you specify ``-U`` on the command line, it would
toggle it off instead.

v0.8.0 is a major feature release:
  - new upload servlet:
  - migrated to Apache Velocity templates.
  - added download of multiple files as zip **new**
  - added filename filters **new**

  Download multiple files as zip and filename filters significantly improves usability of the upload servlet

In older releases:

v0.5.0 is a rather major feature release. v0.5.0 added an Upload servlet that includes a form based file upload 
in the directory list.  This makes it possible to upload files without a WebDAV client. In addition, it is 
styled with responsive html and css so that it is more readable on small (mobile) devices.

Since v0.5.1 is a bugfix release for v0.5.0, Upload servlet, some refactoring: 
enabled alwaysUseSession for authentication, some mobile devices do not cache authentication and keeps prompting 
authentication every refresh and page. This is still as secure (managed by a session) and avoided the annoying 
auth prompts every screen. Login only at the start, and for cookie tests (needed for jesssion), only checks in 
doPost() whre it is needed and only if it is a new (invalid) session.

v0.5.2 added(fixed) sorting in Upload servlet

The older release features are all there in the latest release (e.g. v.1.0.0)

In the config (properties text) file, the option ``uploadservlet=true`` parameter should be set to ``true``.

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

![screenshot in a browser](web/UploadServlet.png "Upload servlet")  
Upload servlet on a desktop web browser showing the new upload servlet in download multiple files as zip view 

![screenshot in a browser](web/UploadServPhone.jpg "Upload on a phone")  
Upload servlet on a phone web browser

## Running embtomcatwebdav on an actual network interface

In v1.0.0, embtomcatwebdav presents a setup wizard, this is configured in the host/port dialog.

![host and port setup](web/hostportdlg.png "host and port setup")  
host and port setup.

You can simply select 'dynamic' DHCP (IP address 0.0.0.0) or select 'fixed' and provide an IP address and port.
Prior to v1.0.0, embtomcatwebdav runs on ``http://localhost:8080/webdav`` by default, 
this is normally only accessible on the local computer.

v0.8.1 added a rather important feature. (only v0.8.1 and above)
To access the servlet on your PC / desktop / laptop etc on an actual network interface, use the -H or --host
option and specify the ip address **0.0.0.0** (*zero.zero.zero.zero*) as the hostname.
The IP address **0.0.0.0** causes tomcat to listen on *all interfaces* on the host/PC.
When you start *embtomcatwebdav* with ``-H 0.0.0.0`` (or configure that in the config file as host=0.0.0.0)
*embtomcatwebdav* would enumerate the IP address of the interfaces on your host/PC in the console log like:
```
...
INFO: note host 0.0.0.0 specified, listening on all interfaces
Feb 20, 2024 12:49:46 AM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: added host alias for: localhost
Feb 20, 2024 12:49:46 AM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: added host alias for: 2400:d803:e64:46bc:81b8:a5c7:c34:5e01
Feb 20, 2024 12:49:46 AM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: added host alias for: fe80:0:0:0:33a5:e5da:f6e4:c1a2%eth0
Feb 20, 2024 12:49:46 AM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: added host alias for: 192.168.1.171 <--- this would be your IP (v4) address
Feb 20, 2024 12:49:46 AM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: added host alias for: 0:0:0:0:0:0:0:1%lo
Feb 20, 2024 12:49:46 AM io.github.ag88.embtomcatwebdav.WebDavServer runserver
INFO: added host alias for: 127.0.0.1
...
```
you should see the *IP address* of your host/PC being listed.
You can verify that  ip address in Windows, by running `ipconfig` and in Linux (and perhaps other 
Unix like OS) `ip -4 add`. 

For example, if you start *embtomcatwebdav* with  

```java -jar embtomcatwebdav-1.0.0.jar -H 0.0.0.0```

and if 192.168.1.171 is your PC's IP address, you can then point your web browser on your remote device (e.g. phone, e.g. Android, iPhone, etc) to http://ip_address:8080/webdav (e.g. http://192.168.1.171:8080/webdav in the example above), which should present a web page of the Upload servlet.

The special IP address **0.0.0.0** is intended to solve problems as most PCs/workstations/notebook PCs uses dynamic IP addresses. Using **0.0.0.0** means that you can place ``host=0.0.0.0`` in the config file and 
*embtomcatwebdav* would listen on all network interfaces. However, the IP address you access from your remote device, has to be the actual IP address for your host/PC. 

Alternatively specify the local IP address of your host (PC/desktop/laptop) in the 
``-H hostname`` parameter (or as host parameter in config file). For example, if the *IP address*
 is e.g. 192.168.1.171, specifiy that as the hostname e.g. ``-H 192.168.1.171``. This is actually more secure than using *0.0.0.0*,
 but that it could be troublesome if you are using dynamic IP address (e.g. DHCP) those that are assigned from the network/router etc.

For those who may be wondering if it is possible to use a hostname, the trouble is that the hostname
has to be a valid DNS domain name, so that when the web browser access embtomcatwebdav, it would pass 
the hostname literally. And that it need to match config e.g. that using -H hostname, otherwise you'd get a
404 not found error instead. This complication is simply because Apache Tomcat does virtual hosting, 
so that a single IP address can host a multitude of domain (host) names.

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
java -jar embtomcatwebdav-1.0.0.jar -p 8443 -S keystorefile.jkf 
```
Note that when you run the app with the -S keystorefile.jkf option, it copies the keystore file into the 'tomcat.port' work folder, this is needed for the app to access the keystore file.

Next in the web browser you can goto https://localhost:8443/webdav  
Note that the web browser will complain that the web site is not secure even if it is successful as this is a self signed certificate. But accordingly, there is likely some form of encryption.

The complete procedure would involve installing a certificate from a CA which is beyond the scope of covering about it for this little app. For more details, review the Tomcat SSL howto  
https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html  
https://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html#Installing_a_Certificate_from_a_Certificate_Authority

Generally, for temporary use it'd be costly/overwhelming to get a true CA signed cert. In addition, browsers and apps may validate the cert against DNS data that the domain names matches the certs etc, this could make it practically unfeasible to run https:// SSL on an arbitrary device/system.

There are rather tricky ways to be your own CA, make certs. But it may involve installing your own CA root certs manually on the client devices/browsers etc. This again is beyond scope of covering it here, but a web (e.g. Google) search may get you some leads on how to do that.

## Build
```
mvn package
```

Note that if you build from source the jar file is embtomcatwebdav-1.0.0-jar-with-dependencies.jar, in target/ folder.

## Development/Embedding

This release is released to maven central
https://central.sonatype.com/artifact/io.github.ag88/embtomcatwebdav/1.0.0
```
<dependency>
    <groupId>io.github.ag88</groupId>
    <artifactId>embtomcatwebdav</artifactId>
    <version>1.0.0</version>
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
