package io.github.ag88.embtomcatwebdav;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	
	BufferedImage image;
	
	public ImagePanel() {
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}

	public ImagePanel(BufferedImage image) {
		super();
		this.image = image;
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	}

	public BufferedImage getImage() {
		return image;
	}


	public void setImage(BufferedImage image) {
		this.image = image;
	}
		

}
