package io.github.ag88.embtomcatwebdav.gui;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.App;

public class Util implements ActionListener {

	Log log = LogFactory.getLog(Util.class);

	public Util() {
		// TODO Auto-generated constructor stub
	}

	public static JButton makeNavigationButton(String imageName, String actionCommand,
			String toolTipText, String altText, ActionListener listener) {

		//Create and initialize the button.
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(listener);

		setIcon(button, imageName, altText);

		return button;
	}
	
	public static void setIcon(JButton button, String imageName, String altText) {
		Log log = LogFactory.getLog(Util.class);
		//Look for the image.
		String imgLocation = "/icons/" + imageName;
		URL imageURL = App.class.getResource(imgLocation);

		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, altText));
		} else { // no image found
			button.setText(altText);
			log.error("Resource not found: " + imgLocation);
		}		
	}

	
	public static JMenuItem addmenuitem(String label, String cmd, int keyevent, ActionListener listener) {
		JMenuItem item = new JMenuItem(label);
		item.setMnemonic(keyevent);
		item.setActionCommand(cmd);
		item.addActionListener(listener);
		return item;		
	}

	public static JLabel URLJLabel(final String url, String text) {
		Log log = LogFactory.getLog(Util.class);
		JLabel jl = new JLabel();
		String html = String.format("<html><a href=\"%s\">%s</a></html>", url, text);
		jl.setText(html);
		jl.setCursor(new Cursor(Cursor.HAND_CURSOR));
		jl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (URISyntaxException | IOException ex) {
					log.debug("invalid url " + url);
					log.debug(e);
				}
			}
		});
		return jl;
	}

    //Obtain the image URL
    public Image createImage(String imageName, String description) {
    	
		//Look for the image.
		String imgLocation = "/icons/" + imageName;
		URL imageURL = App.class.getResource(imgLocation);
        
        if (imageURL == null) {
            System.err.println("Resource not found: " + imageName);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }

	public void makesystray() {
		//Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            log.error("SystemTray is not supported");
            return;
        }
        //final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(createImage("Home24.gif", "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();
       
        /*
        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
        Menu displayMenu = new Menu("Display");
        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem infoItem = new MenuItem("Info");
        MenuItem noneItem = new MenuItem("None");
        MenuItem exitItem = new MenuItem("Exit");
       
        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        displayMenu.add(infoItem);
        displayMenu.add(noneItem);
        popup.add(exitItem);
       
        trayIcon.setPopupMenu(popup);
        */
       
        trayIcon.setActionCommand("TRAY");
        trayIcon.addActionListener(this);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
        	log.error("TrayIcon could not be added.");
        }
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("TRAY")) {
			App.getInstance().createGui();
		}
	}

}
