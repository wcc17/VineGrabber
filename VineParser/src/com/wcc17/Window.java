package com.wcc17;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    static final int MAX_LINE_WIDTH = 50;

	static JFrame vineViewerFrame;
	static JPanel vineListPanel = new JPanel();
	static JPanel vineInfoPanel = new JPanel();
	static VLCPanel videoSurfacePanel;
	
    static JScrollPane vineScrollPane;
    static JLabel vineLabel;
    static JButton watchVineButton;
    static JTextField userInputTextField;

    static BufferedImage image;
    public static int width;
    public static int height;
    private DirectMediaPlayerComponent mediaPlayerComponent;

    Vine selectedVine = null;
    Map<Vine, String> vineFileMap = new HashMap<Vine, String>();

    public Window() {
        initialize();
    }

    public void initialize() {
    	vineViewerFrame = new JFrame("Vine Viewer");
        vineViewerFrame.setPreferredSize(new Dimension(550, 900));
        vineViewerFrame.setResizable(false);
    	vineViewerFrame.setLayout(new GridBagLayout());
    	vineViewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeVineJList();
        initializeVineJLabel();
        initializeWatchVineJButton();
        initializeVideoSurfacePanel();

        setupMainGridLayout();

        //Display the window
        vineViewerFrame.pack();
        vineViewerFrame.setVisible(true);
    }

    public void setupMainGridLayout() {
        GridBagConstraints c = new GridBagConstraints();

        //set up video surface
        c.gridx = 0;
        c.gridy = 0;
        vineViewerFrame.add(videoSurfacePanel, c);

        //set up list of vines
        c.gridx = 0;
        c.gridy = 1;
        vineListPanel.add(vineScrollPane);
        vineViewerFrame.add(vineListPanel, c);

        //set up window to show vine info
//        c.weightx = 1.0;
//        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 2;
        initializeVineInfoPanel();
        vineViewerFrame.add(vineInfoPanel, c);
    }

    public void initializeVineInfoPanel() {
        GridBagConstraints c = new GridBagConstraints();
        vineInfoPanel.setLayout(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        vineInfoPanel.add(watchVineButton, c);

        c.gridx = 0;
        c.gridy = 1;
        vineInfoPanel.add(vineLabel, c);
    }

    public void initializeVineJList() {
    	//set up Vine JList
        List<Vine> vines = VineService.parseVineList();
        Vine[] vineData = new Vine[vines.size()];
        for(int i = 0; i < vines.size(); i++) {
            vineData[i] = vines.get(i);
        }
        final JList<Vine> vineJList = new JList<Vine>(vineData);
        vineScrollPane = new JScrollPane(vineJList);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = vineJList.locationToIndex(e.getPoint());
                System.out.println("Clicked on Item " + index);

                selectedVine = vineJList.getModel().getElementAt(index);
                changeVineLabel(selectedVine);
            }
        };
        vineJList.addMouseListener(mouseListener);

        buildVineFileMap(vines);
    }

    public void buildVineFileMap(List<Vine> vines) {
        //just to ensure that vine indexes are always matched up correctly without looping through all
        Map<Integer, Vine> vineMap = new HashMap<Integer, Vine>();
        for(Vine vine : vines) {
            vineMap.put(vine.index, vine);
        }

        File folder = new File("vines/");
        File[] listOfFiles = folder.listFiles();
        for(File file : listOfFiles) {
            String fileName = file.getName();
            String fileIndexString = new String();
            for(int i = 0; i < fileName.length(); i++) {
                if(fileName.charAt(i) != ' '
                        && fileName.charAt(i) != '-'
                        && Character.isDigit(fileName.charAt(i))) {
                    fileIndexString += String.valueOf(fileName.charAt(i));
                } else {
                    break;
                }
            }

            try {
                Integer fileIndex = Integer.parseInt(fileIndexString);
                if(file.getName().equals("2117 - Jonathan Coffman - 2013-05-17T03-23-47.000000.mp4")) {
                    System.out.println("broken");
                }
                vineFileMap.put(vineMap.get(fileIndex), file.getName());
            } catch (NumberFormatException e) {
                //skip the file, we don't need it (its not a vine file if it doesnt have the index at the front)
                System.out.println(fileName + " not added to map of vines to files");
            }
        }
    }
    
    public void initializeVineJLabel() {
        vineLabel = new JLabel("No Vine selected");

        //to initialize
        //TODO: this is a big problem. for some reason the indexing of vines is off.
        //TODO: Uncommenting this will expose the problem. Need to look into
        //TODO: may only be happnening in debug mode. not sure whats up with that
        selectedVine = vineFileMap.keySet().iterator().next();
        changeVineLabel(selectedVine);
    }
    
    public void initializeWatchVineJButton() {
    	watchVineButton = new JButton("Watch");
    	watchVineButton.setHorizontalTextPosition(SwingConstants.CENTER);
    	
    	watchVineButton.addActionListener(new ActionListener() {

    	    @Override
    		public void actionPerformed(ActionEvent e) {
                if(selectedVine != null) {
                    mediaPlayerComponent.getMediaPlayer().playMedia("vines/" + vineFileMap.get(selectedVine));
                }
    		}
    	});
    }
    
    public void changeVineLabel(Vine vine) {
    	StringBuilder vineLabelBuilder = new StringBuilder();
    	vineLabelBuilder.append("<html>");
    	vineLabelBuilder.append("index: ");
    	vineLabelBuilder = appendData(vineLabelBuilder, String.valueOf(vine.index));
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.created);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.description);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.likes);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.loops);
    	vineLabelBuilder = appendData(vineLabelBuilder, vine.username);
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueAddress);
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueCity);
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueCountryCode);
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueName);
//    	vineLabelBuilder = appendData(vineLabelBuilder, vine.venueState);
    	vineLabelBuilder.append("</html>");
    	
    	vineLabel.setText(vineLabelBuilder.toString());
    }
    
    public StringBuilder appendData(StringBuilder dataStringBuilder, String labelValue) {
        if(labelValue.length() > MAX_LINE_WIDTH) {
            labelValue = buildMultiLineLabelData(labelValue);
        }

    	dataStringBuilder.append(labelValue);
    	dataStringBuilder.append("<br>");
    	
    	return dataStringBuilder;
    }

    public String buildMultiLineLabelData(String labelValue) {
        StringBuilder multiLineLabelBuilder = new StringBuilder();

        int i = 0;
        while(i < labelValue.length()) {
            int endIndex = (i + MAX_LINE_WIDTH);
            if(endIndex > (labelValue.length() - 1)) {
                endIndex = labelValue.length() - 1;
            }

            String line = labelValue.substring(i, endIndex);
            multiLineLabelBuilder.append(line);

            i += MAX_LINE_WIDTH;
            if(i < labelValue.length()) {
                multiLineLabelBuilder.append("<br>");
            }
        }

        return multiLineLabelBuilder.toString();
    }

    private void initializeVideoSurfacePanel() {
        videoSurfacePanel = new VLCPanel();
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

        mediaPlayerComponent.getMediaPlayer().setRepeat(true);
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
