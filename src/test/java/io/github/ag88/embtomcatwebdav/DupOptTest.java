package io.github.ag88.embtomcatwebdav;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.namespace.QName;

import org.apache.catalina.LifecycleState;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.model.Allprop;
import com.github.sardine.model.Propfind;
import com.github.sardine.util.SardineUtil;

import io.github.ag88.embtomcatwebdav.opt.OptFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.w3c.dom.Element;

import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This test runs the server
 */
@TestMethodOrder(OrderAnnotation.class)
public class DupOptTest {

	Logger log = Logger.getLogger(DupOptTest.class.getName());
	        
    
    
    @Test
    public void testDupOpt(TestReporter reporter) {
    	OptFactory optfactory = OptFactory.getInstance();
    	optfactory.setChkdup(true);
    	
    	
    }
    

        
}
