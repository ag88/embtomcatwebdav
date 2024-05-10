package io.github.ag88.embtomcatwebdav.util;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ProcXML {
	
	enum State {
		INIT,
		DEPENDENCY,
		LICENSE
	}

	State state;
	
	public ProcXML() {
	}

	private void process(String[] args) {

		try {
			File file = new File(args[0]);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document = db.parse(file);
			state = State.INIT;
			traverse(document.getDocumentElement(), 0);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void traverse(Node node, int level) {
		StringBuilder sb;
		
		// do something with the current node instead of System.out
		// System.out.println(node.getNodeName());
		if (node.getNodeType() == Node.ELEMENT_NODE && 
			node.getNodeName().equals("dependency")) {
			state = State.DEPENDENCY;
			sb = new StringBuilder(100);
			sb.append("<p>");
			sb.append("<div class=\"container-sm\">");
			System.out.print(sb.toString());
		} else if (node.getNodeType() == Node.ELEMENT_NODE &&
			node.getNodeName().equals("licenses")) {
			state = State.LICENSE;
		} else
			printhtml(node, level);
			
		//printnode1(node, level);
		
		NodeList nodeList = node.getChildNodes();
		level++;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node curnode = nodeList.item(i);
			if (curnode.getNodeType() == Node.ELEMENT_NODE) {
				if (curnode.getNodeName().equals("downloadUrls")) continue;
				
				traverse(curnode, level);
			}
			//printnode1(curnode, level);
		}

		if (node.getNodeType() == Node.ELEMENT_NODE &&
			node.getNodeName().equals("dependency") &&
			state == State.DEPENDENCY) {
			state = State.INIT;
			sb = new StringBuilder(50);
			sb.append("</div>");
			System.out.print(sb.toString());

		} else if (node.getNodeType() == Node.ELEMENT_NODE &&
			node.getNodeName().equals("licenses") &&
			state == State.LICENSE) {
			state = State.DEPENDENCY;
		}

	}
	
	public void printhtml(Node node, int level) {
		StringBuilder sb = new StringBuilder(256);
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			String name = node.getNodeName();
			if (state == State.DEPENDENCY) {
				AbstractMap.SimpleEntry<String, String> entry = getfield(node);
				if (entry == null) return;
				if (entry.getKey().equals("name")) {
					sb.append("<div class=\"row\" style=\"font-size: 150%;\">");
					sb.append(entry.getValue());
					sb.append("</div>");
					System.out.println(sb.toString());
					return;
				} else if (entry.getKey().equals("groupId")) {
					sb.append("<div class=\"row\">\n");
					sb.append("<div class=\"col-2\">");
					sb.append(entry.getValue());
					sb.append("</div>");
					System.out.println(sb.toString());
					return;
				} else if (entry.getKey().equals("artifactId")) {
					sb.append("<div class=\"col-2\">");
					sb.append(entry.getValue());
					sb.append("</div>");
					System.out.println(sb.toString());
					return;
				} else if (entry.getKey().equals("version")) {
					sb.append("<div class=\"col-1\">");
					sb.append(entry.getValue());
					sb.append("</div>\n");
					sb.append("</div>");
					System.out.println(sb.toString());
					return;
				} else if (entry.getKey().equals("projectUrl")) {
					sb.append("<div class=\"row\">\n");
					sb.append("project url:");
					sb.append("<a class=\"col-sm\" href=\"");					
					sb.append(entry.getValue());
					sb.append("\">\n");
					sb.append(entry.getValue());
					sb.append("</a>\n");
					sb.append("</div>");
					System.out.println(sb.toString());
					return;
				}
			} else if (state == State.LICENSE) {
				if(name.equals("license")) {
					Map<String, String> fields = getfields(node);
					sb.append("<div class=\"row\">\n");
					sb.append("<a class=\"col-sm\" href=\"");					
					sb.append(fields.get("url"));
					sb.append("\">\n");
					sb.append(fields.get("name"));
					sb.append("</a>");
					sb.append("</div>");
					System.out.println(sb.toString());
					return;
				}
			} 
		}
	}
	
	public AbstractMap.SimpleEntry<String, String> getfield(Node node) {
		AbstractMap.SimpleEntry<String, String> entry;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			String key = node.getNodeName();
			Node cnode = node.getFirstChild();
		    if (cnode.getNodeType() == Node.TEXT_NODE) {
				if (!cnode.getNodeValue().trim().equals("")) {
					String value = cnode.getNodeValue();
					entry = new SimpleEntry<String, String>(key, value);
					return entry;
				}
		    }
		}		
		    
		return null;
	}
	
	public Map<String, String> getfields(Node node) {
		Map<String, String> map = new TreeMap<String, String>();
		
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node cnode = nodeList.item(i);
			if (cnode.getNodeType() == Node.ELEMENT_NODE) {
				String key = cnode.getNodeName();
				Node cnode1 = cnode.getFirstChild();
				if (cnode1.getNodeType() == Node.TEXT_NODE) {
					if (cnode1.getNodeValue() != null || !cnode1.getNodeValue().trim().equals("")) {
						String value = cnode1.getNodeValue();
						map.put(key, value);
					}
				}
			}
		}		
		    
		return map;
	}
	
	public void printnode1(Node node, int level) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			StringBuilder sb = new StringBuilder(100);
			for (int i = 0; i < level; i++)
				sb.append(' ');
			sb.append(node.getNodeName());
			Node cnode = node.getFirstChild();
		    if (cnode.getNodeType() == Node.TEXT_NODE) {
				if (!cnode.getNodeValue().trim().equals("")) {
					sb.append(" ");
					sb.append(cnode.getNodeValue());
					System.out.println(sb.toString());
				}
				else 
					System.out.println(sb.toString());
			}
		}
	}
	
	public void printnode(Node node, int level) {
		if (node.getNodeType() == Node.ELEMENT_NODE ||
				node.getNodeType() == Node.ENTITY_NODE ||
				node.getNodeType() == Node.ATTRIBUTE_NODE ||					
				node.getNodeType() == Node.TEXT_NODE) {
			StringBuilder sb = new StringBuilder(100);
			for (int i = 0; i < level; i++)
				sb.append(' ');
			sb.append("type: ");
			sb.append(node.getNodeType());
			sb.append(" ");
			sb.append("name: ");
			sb.append(node.getNodeName());
			sb.append(" ");
			sb.append("value: ");
			sb.append(node.getNodeValue());
			System.out.println(sb.toString());
		}
	}

	public static void main(String[] args) {
		new ProcXML().process(args);
	}

}
