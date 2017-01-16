package com.wcc17;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Created by wcc17 on 1/15/17.
 */
public class Window {

	static JFrame vineViewerFrame; 
	static JPanel vineListPanel;
	static JPanel vineInfoPanel;
	
    static JScrollPane vineScrollPane;
    static JLabel vineLabel;
    static JButton watchVineButton;

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

        //Display the window
        vineViewerFrame.pack();
        vineViewerFrame.setVisible(true);
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
}
