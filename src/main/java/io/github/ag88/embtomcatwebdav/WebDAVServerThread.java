/*
 Copyright 2023 Andrew Goh http://github.com/ag88
 
 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package io.github.ag88.embtomcatwebdav;

// TODO: Auto-generated Javadoc
/**
 * This is a @link {@link Runnable} container to run the server in its own background thread
 */
public class WebDAVServerThread extends Thread {

	/** The server. */
	WebDavServer server;
	
	/**
	 * Instantiates a new web DAV server thread.
	 *
	 * @param name the name
	 * @param server the server
	 */
	public WebDAVServerThread(String name, WebDavServer server) {
		super(name);
		this.server = server;
	}
	
	
	/**
	 * Run.
	 */
	@Override
	public void run() {
		server.runserver();
	}
	
	/**
	 * Gets the server.
	 *
	 * @return the server
	 */
	public WebDavServer getServer() {
		return server;
	}

	/**
	 * Sets the server.
	 *
	 * @param server the new server
	 */
	public void setServer(WebDavServer server) {
		this.server = server;
	}
	
}
