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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.catalina.WebResource;

/**
 * A class encapsulating the sorting of resources.
 */
public class SortManager {

	/**
	 * The default sort.
	 */
	protected Comparator<WebResource> defaultResourceComparator;

	/**
	 * Comparator to use when sorting resources by name.
	 */
	protected Comparator<WebResource> resourceNameComparator;

	/**
	 * Comparator to use when sorting files by name, ascending (reverse).
	 */
	protected Comparator<WebResource> resourceNameComparatorAsc;

	/**
	 * Comparator to use when sorting resources by size.
	 */
	protected Comparator<WebResource> resourceSizeComparator;

	/**
	 * Comparator to use when sorting files by size, ascending (reverse).
	 */
	protected Comparator<WebResource> resourceSizeComparatorAsc;

	/**
	 * Comparator to use when sorting resources by last-modified date.
	 */
	protected Comparator<WebResource> resourceLastModifiedComparator;

	/**
	 * Comparator to use when sorting files by last-modified date, ascending
	 * (reverse).
	 */
	protected Comparator<WebResource> resourceLastModifiedComparatorAsc;

	public SortManager(boolean directoriesFirst) {
		resourceNameComparator = new ResourceNameComparator();
		resourceNameComparatorAsc = Collections.reverseOrder(resourceNameComparator);
		resourceSizeComparator = new ResourceSizeComparator(resourceNameComparator);
		resourceSizeComparatorAsc = Collections.reverseOrder(resourceSizeComparator);
		resourceLastModifiedComparator = new ResourceLastModifiedDateComparator(resourceNameComparator);
		resourceLastModifiedComparatorAsc = Collections.reverseOrder(resourceLastModifiedComparator);

		if (directoriesFirst) {
			resourceNameComparator = new DirsFirstComparator(resourceNameComparator);
			resourceNameComparatorAsc = new DirsFirstComparator(resourceNameComparatorAsc);
			resourceSizeComparator = new DirsFirstComparator(resourceSizeComparator);
			resourceSizeComparatorAsc = new DirsFirstComparator(resourceSizeComparatorAsc);
			resourceLastModifiedComparator = new DirsFirstComparator(resourceLastModifiedComparator);
			resourceLastModifiedComparatorAsc = new DirsFirstComparator(resourceLastModifiedComparatorAsc);
		}

		defaultResourceComparator = resourceNameComparator;
	}

	/**
	 * Sorts an array of resources according to an ordering string.
	 *
	 * @param resources The array to sort.
	 * @param order     The ordering string.
	 *
	 * @see #getOrder(String)
	 */
	public void sort(WebResource[] resources, String order) {
		Comparator<WebResource> comparator = getComparator(order);

		if (null != comparator) {
			Arrays.sort(resources, comparator);
		}
	}

	public Comparator<WebResource> getComparator(String order) {
		return getComparator(getOrder(order));
	}

	public Comparator<WebResource> getComparator(Order order) {
		if (null == order) {
			return defaultResourceComparator;
		}

		if ('N' == order.column) {
			if (order.ascending) {
				return resourceNameComparatorAsc;
			} else {
				return resourceNameComparator;
			}
		}

		if ('S' == order.column) {
			if (order.ascending) {
				return resourceSizeComparatorAsc;
			} else {
				return resourceSizeComparator;
			}
		}

		if ('M' == order.column) {
			if (order.ascending) {
				return resourceLastModifiedComparatorAsc;
			} else {
				return resourceLastModifiedComparator;
			}
		}

		return defaultResourceComparator;
	}

	/**
	 * Gets the Order to apply given an ordering-string. This ordering-string
	 * matches a subset of the ordering-strings supported by <a href=
	 * "https://httpd.apache.org/docs/2.4/mod/mod_autoindex.html#query">Apache
	 * httpd</a>.
	 *
	 * @param order The ordering-string provided by the client.
	 *
	 * @return An Order specifying the column and ascending/descending to be applied
	 *         to resources.
	 */
	public Order getOrder(String order) {
		if (null == order || 0 == order.trim().length()) {
			return Order.DEFAULT;
		}

		String[] options = order.split(";");

		if (0 == options.length) {
			return Order.DEFAULT;
		}

		char column = '\0';
		boolean ascending = false;

		for (String option : options) {
			option = option.trim();

			if (2 < option.length()) {
				char opt = option.charAt(0);
				if ('C' == opt) {
					column = option.charAt(2);
				} else if ('O' == opt) {
					ascending = ('A' == option.charAt(2));
				}
			}
		}

		if ('N' == column) {
			if (ascending) {
				return Order.NAME_ASC;
			} else {
				return Order.NAME;
			}
		}

		if ('S' == column) {
			if (ascending) {
				return Order.SIZE_ASC;
			} else {
				return Order.SIZE;
			}
		}

		if ('M' == column) {
			if (ascending) {
				return Order.LAST_MODIFIED_ASC;
			} else {
				return Order.LAST_MODIFIED;
			}
		}

		return Order.DEFAULT;
	}

	public static class Order {
		public final char column;
		public final boolean ascending;

		Order(char column, boolean ascending) {
			this.column = column;
			this.ascending = ascending;
		}

		public static final Order NAME = new Order('N', false);
		public static final Order NAME_ASC = new Order('N', true);
		public static final Order SIZE = new Order('S', false);
		public static final Order SIZE_ASC = new Order('S', true);
		public static final Order LAST_MODIFIED = new Order('M', false);
		public static final Order LAST_MODIFIED_ASC = new Order('M', true);

		public static final Order DEFAULT = NAME;
	}

}
