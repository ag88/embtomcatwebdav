A WebDAV server based on Apache Tomcat's Webdav servlet and embedded Tomcat server
---

This is a WebDAV / web server based on Apache Tomcat's WebDAV servlet and embedded Tomcat server.  
It serves the current directory as a web site. 
That makes it convenient to upload/download files to/from your PC/notebook PC from a
remote device say a mobile phone (Android, iPhone etc) simply using the web browser.  
Running it with the upload servlet makes it possible to upload files without a WebDAV client.

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
- ***new** v0.8.0 major feature release: 
  - new upload servlet:
  - migrated to Apache Velocity templates.
  - added download of multiple files as zip ***new**
  - added filename filters ***new**
  
  Download multiple files as zip and filename filters significantly improves usability of the upload servlet
  
- ***new** v0.8.1  add support for ip 0.0.0.0 listen on all interfaces 
  - host aliases will be added for all interfaces if 0.0.0.0 is specified as the host name
  
  This solves problems related to dynamic IP addresses
  
- ***new** v0.8.2 improvements, fixes for config file generation. 
  - improvements, fixes for config file generation
    config file generation now adds description entries and is sorted
  - improved help documentation / description
  - upload servlet is now default from this release
  
- ***new** v0.9.0 checks for updates from this repository
  - checks for updates from this repository
  - added option to enable/disable check for updates
  - display server info: Embtomcatwebdav and version in page for upload servlet

- ***new** v0.9.1 adds allowlinking option
  - allow linking (e.g. follow symbolic links, warn: links can go outside working
    dir/path, only works in upload servlet)

- ***new** v0.9.2 make filter case insensitive
  - in prior release the filter in upload servlet is case sensitive, this is troublesome
    searching for files/patterns. This minor update makes it case insensitive, word
    searches matches regardless of case

- ****new** v1.0.0 major feature release new QR Code Gui, scan QR code and connect !
  - scan the QR Code with your mobile phone and connect to upload servlet !
  - Added a 'setup wizard' that helps to configure embtomcatwebdav and get it up and running easily
  - Gui configuration panels for the various server configuration
  - it now uses a default configuration file so that you can simply launch it and start uploading / downloading

Note that the above are cumulative updates, the latest version e.g. v${project.version} contains all
the updates/features in the lower/prior versions.

status: beta

![screenshot in a browser showing the upload servlet](web/screenshot.png "Screen shot showing the upload servlet")  
Upload servlet on a desktop web browser showing the new upload servlet

![Scan QR code using your phone and connect to upload servlet](web/example.png "Scan QR code using your phone and connect to upload servlet")  
***new in v1.0.0 Scan QR code with your mobile phone and connect to upload servlet**

## Download

It is recommended to use the recent/latest **[release jars](https://github.com/ag88/embtomcatwebdav/releases/latest)** .
In the releases section of this repository.

## Prerequisites

Java / JDK >= 1.8 is required, 
e.g. from 
- https://adoptium.net 
- https://openjdk.org/
- https://www.oracle.com/java/technologies/downloads/

## <a id="run">Run</a>

In some operating systems, it may be possible to run it by simply double clicking on the jar file after Java/JDK is installed.
e.g. In Windows, you can normally run it by simply double clicking the jar file in windows explorer.

## QR Code Gui (from v1.0.0)

For embtomcatwebdav 1.0.0, after the first few initial setup dialogs, it would present a QR Code Gui.
If you have selected DHCP (dynamic) ip address 0.0.0.0, you would see a Gui as follows:

![QR Code Gui](web/gui.png "QR code Gui")

- Select the IP address of the network facing network interface. Select its IPV4 address if there are other addresses available.  

- Scan the QR Code using your mobile phone (e.g. some mobile phones has those features, or use an app as like 
[QRDroid](https://play.google.com/store/apps/details?id=la.droid.qr) etc.)

- You mobile phone should present a web browser with the scanned URL open.  
  if it doesn't connect check that you mobile phone/device are on the same WiFi LAN network as your host(pc/notebook) running embtomcatwebdav
  e.g.

![screenshot in a browser](web/UploadServPhone.jpg "Upload on a phone")  
Upload servlet on a phone web browser

- If you close the QR Code Gui window, you could open that window again, by double clicking the icon from the system tray

- To stop/shutdown embtomcatwebdav, simply click the 'stop' icon on the toolbar, or select 'File > stop server and exit' from the menu.


If you are running this in Windows, it is recommended to use version 1.0.0 and newer
https://github.com/ag88/embtomcatwebdav/releases/latest  
versions less than 1.0.0 do not have a Gui, in Windows to stop it after you simply 'double click' that, 
launch the task manager, right click on the task bar to find it.
In Task manager look for a process java binary or some such app matching it that is running and kill it.

## Setup wizard v1.0.0

Version 1.0.0 and newer presents a setup wizard the first time you run embtomcatwebdav. This helps you configure the web server and gets up 
and running easily/quickly.

![host and port setup](web/hostportdlg.png "host and port setup")  
host and port setup

![authentication setup](web/authdlg.png "authentication setup")  
authentication setup.

note that the user and password prompted is not your OS user and password, this user and password is only used to connect to embtomcatwebdav
server, and it can be any user or password that you prefer.

With DIGEST authentication, if you change the realm or user, it would clear the password as the hash will change.
DIGEST authentication is secure as only a hash is stored and not plaintext passwords. But that the system do not know your password,
hence, if you change the realm or user, you would need to set the password again as the hash is no longer valid.

![setup paths](web/paths.png "setup paths")  
setup paths.

Select the path that you want to serve in embtomcatwebdav. This would be the root folder that is served.

It is recommended to leave the tomcat work directory as the default. Apache Tomcat requires a work directory while it is running.


![configs display](web/configs.png "configs display")  
It displays the configs before saving and launch. For other configs and if any changes are needed, they can be updated from the QR Code Gui under the Setup menu.


From v1.0.0, embtomcatwebdav uses a default config file which is displayed in the line below. The configs are stored in that config file.


## Command line CLI 

```
java -jar ${project.artifactId}-${project.version}.jar
```

To run with parameters e.g. changing the port to 8081
```
java -jar ${project.artifactId}-${project.version}.jar -p 8081
```

If you have various configuration parameters, it is recommended to use a config file.
From v1.0.0, embtomcatwebdav uses a default config file. Hence, this step is unnecessary,
but that the option is still available.

A template can be generated as such
```
java -jar ${project.artifactId}-${project.version}.jar --genconf wdav.ini
```

The above would generate a config file (e.g. wdav.ini), and may look like such.
```
# Embedded Tomcat Webdav server properties
# generated: 2024-02-22T21:09:33.723599956
# set host. Default localhost. If you set the host to 0.0.0.0 it will listen on all interfaces
# however, you still need to specify the IP address of this host PC from your web browser
# to access the webdav or upload servlet web page
host=localhost
# set port. Default 8080
port=8080
# set path, default: current working dir.
# This is the root directory which is accessible in the webdav or upload servlet
path=/home/user
# set basedir, a work folder for tomcat, default [current working dir]/tomcat.port
basedir=/home/user/tomcat.8080
# set urlprefix, default /webdav
urlprefix=/webdav
# set realm name, default 'Simple'
realm=Simple
# set user. If the user is specified, the app will prompt for authentication
user=
# set password, on the command line you may omit this, it would prompt for it if -u is specified
# In the config file, if you leave it empty as "password=" it is actually a blank password.
# To make it prompt for password from the config file, comment it with a # in front.
password=
...
```

Thereafter, running with the config file e.g. wdav.ini is
```
java -jar ${project.artifactId}-${project.version}.jar -c wdav.ini
```
You may like to adapt the batch files e.g. run.bat, run.sh as examples to run it as such.
In that way it is also possible to set up auto start by simply running the batch file.

## Extended details

Extended [README](README-ext.md) for a non-TLDR version ;)

## Attributions

The server is really [Apache Tomcat https://tomcat.apache.org/](https://tomcat.apache.org/).
One of the most widely used open-source implementation of the Jakarta Servlet, JSP technologies.

In version 0.8.0, the directory listing view is migrated to [Apache Velocity https://velocity.apache.org/](https://velocity.apache.org/) templates.

In version 1.0.0, QR Code generator library io.nayuki.qrcodegen project url: 
[https://www.nayuki.io/page/qr-code-generator-library](https://www.nayuki.io/page/qr-code-generator-library)

Various others e.g.

[AppDirs](https://github.com/harawata/appdirs)

Full [Attributions](https://htmlpreview.github.io/?https://github.com/ag88/embtomcatwebdav/blob/main/web/attrib/attribution.html)

## No warranty

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Origin

github repository: 
[https://github.com/ag88/embtomcatwebdav](https://github.com/ag88/embtomcatwebdav)

I've made efforts to tailor the WebDAV servlet so that it works as a standalone app. 
Developing this has taken quite some effort mainly in adding features, getting it to work and rather extensive tests. if you use this app and found it useful, i'd really appreciate it if you could support my efforts [![Donate](web/donorbox.png)](https://donorbox.org/embedded-tomcat-webdav-server).
you could also help simply starring this repository ;)
