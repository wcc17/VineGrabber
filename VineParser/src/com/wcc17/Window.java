package com.wcc17;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.*;

import uk.co.caprica.vlcj.component.DirectMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

/**
 * Created by wcc17 on 1/15/17.
 */
public class Window {

	static JFrame vineViewerFrame; 
	static JPanel vineListPanel;
	static JPanel vineInfoPanel;
	static VLCPanel videoSurfacePanel;
	
    static JScrollPane vineScrollPane;
    static JLabel vineLabel;
    static JButton watchVineButton;

    static BufferedImage image;
    public static int width;
    public static int height;
    private DirectMediaPlayerComponent mediaPlayerComponent;

    public Window() {
        initialize();
    }

    public void initialize() {
    	vineViewerFrame = new JFrame("Vine Viewer");
    	vineViewerFrame.setLayout(new FlowLayout());
    	vineViewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    	vineListPanel = new JPanel();
    	vineInfoPanel = new JPanel();
    	
        initializeVineJList();
        initializeVineJLabel();
        initializeWatchVineJButton();
        
        vineListPanel.add(vineScrollPane);
        vineInfoPanel.add(vineLabel);
        vineInfoPanel.add(watchVineButton);

        vineViewerFrame.add(vineListPanel);
        vineViewerFrame.add(vineInfoPanel);

        videoSurfacePanel = new VLCPanel();
        vineViewerFrame.add(videoSurfacePanel);
        width = 480;
        height = 480;
        image = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .createCompatibleImage(width, height);
        BufferFormatCallback bufferFormatCallback = new BufferFormatCallback() {
            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                return new RV32BufferFormat(width, height);
            }
        };
        mediaPlayerComponent = new DirectMediaPlayerComponent(bufferFormatCallback) {
            @Override
            protected RenderCallback onGetRenderCallback() {
                return new VineRenderCallbackAdapter();
            }
        };

        //Display the window
        vineViewerFrame.pack();
        vineViewerFrame.setVisible(true);

        //TODO: TO PLAY A VIDEO
        mediaPlayerComponent.getMediaPlayer()
                .playMedia("vines-VICTORIA/0 - MileSplit US - 2016-11-16T20-07-44.000000.mp4");
    }
    
    public void initializeVineJList() {
    	//set up Vine JList
        List<Vine> vines = getVineList();
        Vine[] vineData = new Vine[vines.size()];
        for(int i = 0; i < vines.size(); i++) {
            vineData[i] = vines.get(i);
        }
        final JList<Vine> vineJList = new JList<Vine>(vineData);
        vineScrollPane = new JScrollPane(vineJList);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = vineJList.locationToIndex(e.getPoint());
                changeVineLabel(vineJList.getModel().getElementAt(index));
                System.out.println("Clicked on Item " + index);
            }
        };
        vineJList.addMouseListener(mouseListener);
    }
    
    public void initializeVineJLabel() {
    	vineLabel = new JLabel("No Vine selected");
    }
    
    public void initializeWatchVineJButton() {
    	watchVineButton = new JButton("Watch");
    	watchVineButton.setHorizontalTextPosition(SwingConstants.CENTER);
    	
    	watchVineButton.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			System.out.println("watch button clicked");
    		}
    	});
    }
    
    public void changeVineLabel(Vine vine) {
    	StringBuilder vineLabelBuilder = new StringBuilder();
    	vineLabelBuilder.append("<html>");
    	vineLabelBuilder.append("index: ");
    	vineLabelBuilder = appendData(vineLabelBuilder, String.valueOf(vine.index));
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.avatarUrl);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.created);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.description);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.likes);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.loops);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.username);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueAddress);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueCity);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueCountryCode);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueName);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueState);
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.videoUrl);
    	vineLabelBuilder.append("</html>");
    	
    	vineLabel.setText(vineLabelBuilder.toString());
    }
    
    public StringBuilder appendData(StringBuilder dataStringBuilder, String labelValue) {
    	dataStringBuilder.append(labelValue);
    	dataStringBuilder.append("<br>");
    	
    	return dataStringBuilder;
    }

    public List<Vine> getVineList() {
        List<Vine> vines = VineService.parseVineList();
        return vines;
    }

    public void downloadVines() {
        List<Vine> vines = VineService.parseVineList();
        VineService.downloadVines(vines);
    }

    private class VineRenderCallbackAdapter extends RenderCallbackAdapter {

        private VineRenderCallbackAdapter() {
            super(new int[width * height]);
        }

        @Override
        protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
            // Simply copy buffer to the image and repaint
            image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
            videoSurfacePanel.setImage(image);
            videoSurfacePanel.repaint();
        }
    }
}
