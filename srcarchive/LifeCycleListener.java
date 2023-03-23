package com.embtomcat.webdav;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.WebAnnotationSet;
import org.apache.tomcat.util.descriptor.web.LoginConfig;

public class LifeCycleListener implements LifecycleListener {

	public LifeCycleListener() {
	}

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
        try {
            Context context = (Context) event.getLifecycle();
            if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
                context.setConfigured(true);

                // Process annotations
                WebAnnotationSet.loadApplicationAnnotations(context);

                // LoginConfig is required to process @ServletSecurity
                // annotations
                if (context.getLoginConfig() == null) {
                    context.setLoginConfig(new LoginConfig("NONE", null, null, null));
                    context.getPipeline().addValve(new NonLoginAuthenticator());
                }
            }
        } catch (ClassCastException e) {
        }
        
        if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
            initWebappDefaults((Context) event.getLifecycle());
        }        
	}

    public static void initWebappDefaults(Context ctx) {
        // Default servlet
        Wrapper servlet = Tomcat.addServlet(
                ctx, "default", "org.apache.catalina.servlets.DefaultServlet");
        servlet.setLoadOnStartup(1);
        servlet.setOverridable(true);

        // JSP servlet (by class name - to avoid loading all deps)
        /*
        servlet = addServlet(
                ctx, "jsp", "org.apache.jasper.servlet.JspServlet");
        servlet.addInitParameter("fork", "false");
        servlet.setLoadOnStartup(3);
        servlet.setOverridable(true);
        */

        // Servlet mappings
        ctx.addServletMappingDecoded("/", "default");
        //ctx.addServletMappingDecoded("*.jsp", "jsp");
        //ctx.addServletMappingDecoded("*.jspx", "jsp");

        // Sessions
        ctx.setSessionTimeout(30);

        // MIME type mappings
        Tomcat.addDefaultMimeTypeMappings(ctx);

        // Welcome files
        ctx.addWelcomeFile("index.html");
        ctx.addWelcomeFile("index.htm");
        //ctx.addWelcomeFile("index.jsp");
    }
	
}
