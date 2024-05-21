package io.github.ag88.embtomcatwebdav.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FileSelPanel extends JPanel implements ActionListener {

	JTextField tfselfile;
	
	JButton btnFileSel;
	
	JFileChooser chooser;
			
	public FileSelPanel() {
		creategui("Select file/folder");
	}

	public FileSelPanel(String label) {
		creategui(label);
	}
	
	private void creategui(String label) {
		setPreferredSize(new Dimension(650, 75));
		setLayout(new FlowLayout(FlowLayout.LEADING));
		add(new JLabel(label));
		
		tfselfile = new JTextField(40);
		add(tfselfile);
		btnFileSel = Util.makeNavigationButton("folder.png", "FSEL", "Select file/folder", "Select file/folder", this);
		add(btnFileSel);		

	}

	
	public void setChooser(String directory, int mode) {
		chooser = new JFileChooser(directory);
		tfselfile.setText(directory);
		chooser.setFileSelectionMode(mode);		
	}
	
	private void dosel() {
		
		if (chooser == null)
			chooser = new JFileChooser();

		 int returnVal = chooser.showOpenDialog(this);		 
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	String selt;
		       try {
				 selt = chooser.getSelectedFile().getCanonicalPath();
		       } catch (IOException e) {
		    	 selt = chooser.getSelectedFile().getAbsolutePath();				
		       }
		       tfselfile.setText(selt);
		    }
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("FSEL")) {
			dosel();
		}		
	}
	
	
	public String getSelFile() {
		 return tfselfile.getText();
	}
	

}
