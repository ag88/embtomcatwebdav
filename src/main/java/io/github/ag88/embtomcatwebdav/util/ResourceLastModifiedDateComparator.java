/*
 * Attribution to https://tomcat.apache.org/
 * 
 * portions adapted and modified Andrew Goh http://github.com/ag88
 *  
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package io.github.ag88.embtomcatwebdav.util;

import java.util.Comparator;

import org.apache.catalina.WebResource;

public class ResourceLastModifiedDateComparator implements Comparator<WebResource> {
	
    private Comparator<WebResource> base;

    public ResourceLastModifiedDateComparator(Comparator<WebResource> base) {
        this.base = base;
    }

    @Override
    public int compare(WebResource r1, WebResource r2) {
        int c = Long.compare(r1.getLastModified(), r2.getLastModified());

        if(0 == c) {
            return base.compare(r1, r2);
        } else {
            return c;
        }
    }



}
