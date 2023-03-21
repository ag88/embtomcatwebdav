A WebDAV server based on Apache Tomcat's Webdav servlet and embedded tomcat server
---

This is a WebDAV server based on Apache Tomcat's WebDAV servlet and embedded tomcat server

Current status: alpha/scratch/trial

- the port is patched in the source code 8082, runs on localhost
- it serves the current working directory in which the app is started
- no authentication etc

![screenshot in a browser](https://github.com/ag88/embtomcatwebdav/raw/main/web/screenshot.jpg "Screen shot")

# Build

mvn package

# Run

```
java -jar target/webdav-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

requires java >= 1.8

note that on starting, it creates a folder "work" in the current directory.
and serves the current directory on webdav at http://localhost:8082

On running, point the web browser to http://localhost:8082, you should see the directory listing of your current work directory. For more functionality, it requires a WebDAV client to interact with the WebDAV server


