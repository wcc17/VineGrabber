package com.wcc17;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by wcc17 on 1/15/17.
 */
public class Window {

    static VineService vineService;

    public Window() {
        vineService = new VineService();
    }

    public void run() {
        JFrame vineViewerFrame = new JFrame("Vine Viewer");
        vineViewerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //set up Vine JList
        List<Vine> vines = getVineList();
        Vine[] vineData = new Vine[vines.size()];
        for(int i = 0; i < vines.size(); i++) {
            vineData[i] = vines.get(i);
        }
        final JList vineJList = new JList(vineData);
        JScrollPane vineScrollPane = new JScrollPane(vineJList);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
                int index = vineJList.locationToIndex(e.getPoint());
                System.out.println("Clicked on Item " + index);
//                }
            }
        };
        vineJList.addMouseListener(mouseListener);

        //add stuff
        JLabel helloWorldLabel = new JLabel("Hello World");
        vineViewerFrame.getContentPane().add(helloWorldLabel);
        vineViewerFrame.getContentPane().add(vineScrollPane);

        //Display the window
        vineViewerFrame.pack();
        vineViewerFrame.setVisible(true);
    }

    public List<Vine> getVineList() {
        List<Vine> vines = vineService.parseVineList();
        return vines;
    }

    public void downloadVines() {
        List<Vine> vines = vineService.parseVineList();
        vineService.downloadVines(vines);
    }
}
