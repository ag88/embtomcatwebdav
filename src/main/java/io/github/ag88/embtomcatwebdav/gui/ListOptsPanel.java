package io.github.ag88.embtomcatwebdav.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.App;
import io.github.ag88.embtomcatwebdav.opt.Opt;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;

public class ListOptsPanel extends JPanel {

	Log log = LogFactory.getLog(ListOptsPanel.class);

	public ListOptsPanel() {

		creategui();
	}

	private void creategui() {

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		DefaultListModel<String> m = new DefaultListModel<>();
		JList<String> jlist = new JList<String>(m);
		String props = OptFactory.getInstance().genconfigprop();
		String[] propl = props.split(System.lineSeparator());
		for (String l : propl)
			m.addElement(l);
		JScrollPane jsp = new JScrollPane(jlist);
		jsp.setPreferredSize(new Dimension(800,580));
		add(jsp);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
		p.add(new JLabel("config file:"));
		p.add(new JLabel(App.getInstance().getconfigfile()));
		add(p);

		setPreferredSize(new Dimension(800,600));

	}

}
