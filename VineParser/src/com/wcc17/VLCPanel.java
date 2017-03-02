package com.wcc17;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by wcc17 on 3/2/17.
 */
public class VLCPanel extends JPanel {

    private BufferedImage image;

    //TODO: these need to change based on the video being played
    public static final int width = 480;
    public static final int height = 480;

    public VLCPanel() {
        setBackground(Color.BLACK);
        setOpaque(true);
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(image, null, 0, 0);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return this.image;
    }
}
