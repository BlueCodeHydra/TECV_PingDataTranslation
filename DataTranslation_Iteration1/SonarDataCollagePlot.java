/*                                                                                       
 *                                                   
 *       /__  ___/     //   / /     //   ) )       ||   / / 
 *         / /        //____       //              ||  / /  
 *        / /        / ____       //         ____  || / /   
 *       / /        //           //                ||/ /    
 *      / /        //____/ /    ((____/ /          |  /     
 *   
 *
 *
 * Project: TEC-V
 * Author: Michel Dowling
 * Description: This java script reads a csv file in the format of (Z, angle, distance)
 *              and converts it to a basic collage chart plot to aid in visualizing the outline 
 *              of the data collected from "getPingData".
 *
 * This code is provided under the MIT License.
 * Copyright (c) 2023 Michel Dowling
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files, to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 */


import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class SonarDataCollagePlot {
    private JFrame frame;
    private CollagePanel collagePanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SonarDataCollagePlot app = new SonarDataCollagePlot();
            app.createAndShowGUI();
        });
    }

    public SonarDataCollagePlot() {
        frame = new JFrame("Sonar Data Collage Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 800);

        collagePanel = new CollagePanel();
        frame.add(collagePanel);
    }

    private void createAndShowGUI() {
        frame.setVisible(true);
    }
}

class CollagePanel extends JPanel {
    private java.util.List<java.util.List<SonarDataPoint>> dataGroups = new ArrayList<>();
    private Map<Double, Color> colorMap = new HashMap<>(); // Map Z heights to colors

    public CollagePanel() {
        // Define colors for Z heights (you can add more if needed)
        colorMap.put(0.0, Color.RED);
        colorMap.put(1.0, Color.GREEN);
        colorMap.put(2.0, Color.BLUE);
        colorMap.put(3.0, Color.ORANGE);

        loadDataFromCSV("PoolTestData\\data1.csv"); // Replace with your data file path
    }

    private void loadDataFromCSV(String filePath) {
        java.util.List<SonarDataPoint> dataGroup = new ArrayList<>();
        double lastZ = Double.NaN;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    double angleDegrees = Double.parseDouble(data[1]);
                    double distance = Double.parseDouble(data[2]);
                    double z = Double.parseDouble(data[0]);

                    // Normalize the angle
                    double angleDegreesNormalized = angleDegrees % 360;
                    double angleRadians = Math.toRadians(angleDegreesNormalized);

                    double x = distance * Math.cos(angleRadians); // Flip x
                    double y = distance * Math.sin(angleRadians); // Flip y

                    if (z != lastZ) {
                        if (!dataGroup.isEmpty()) {
                            dataGroups.add(dataGroup);
                        }
                        dataGroup = new ArrayList<>();
                        lastZ = z;
                    }

                    SonarDataPoint point = new SonarDataPoint(x, y, z);
                    dataGroup.add(point);
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!dataGroup.isEmpty()) {
            dataGroups.add(dataGroup);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int xOffset = 10;
        int yOffset = 10;
        int boxWidth = 400;
        int boxHeight = 300;
        int titleHeight = 20;
        int dataPadding = 10;

        for (java.util.List<SonarDataPoint> dataGroup : dataGroups) {
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            getHeight();

            double dataMinX = getMinX(dataGroup);
            double dataMaxX = getMaxX(dataGroup);
            double dataMinY = getMinY(dataGroup);
            double dataMaxY = getMaxY(dataGroup);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(xOffset, yOffset, boxWidth, boxHeight);
            g2d.drawString("Z: " + dataGroup.get(0).z, xOffset + 5, yOffset + titleHeight - 5);

            for (SonarDataPoint point : dataGroup) {
                double x = map(-point.x, -dataMaxX, -dataMinX, xOffset, xOffset + boxWidth - dataPadding); // Flip x
                double y = map(-point.y, -dataMaxY, -dataMinY, yOffset + titleHeight, yOffset + boxHeight - dataPadding); // Flip y
                Color pointColor = colorMap.get(point.z); // Get color based on Z height

                g2d.setColor(pointColor);
                g2d.fillOval((int) x, (int) y, 5, 5);
            }

            xOffset += boxWidth + 20;
            if (xOffset + boxWidth > width) {
                xOffset = 10;
                yOffset += boxHeight + 20;
            }
        }
    }

    private double map(double value, double fromMin, double fromMax, double toMin, double toMax) {
        return (value - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin;
    }

    private double getMinX(java.util.List<SonarDataPoint> points) {
        double min = Double.MAX_VALUE;
        for (SonarDataPoint point : points) {
            if (point.x < min) {
                min = point.x;
            }
        }
        return min;
    }

    private double getMaxX(java.util.List<SonarDataPoint> points) {
        double max = -Double.MAX_VALUE;
        for (SonarDataPoint point : points) {
            if (point.x > max) {
                max = point.x;
            }
        }
        return max;
    }

    private double getMinY(java.util.List<SonarDataPoint> points) {
        double min = Double.MAX_VALUE;
        for (SonarDataPoint point : points) {
            if (point.y < min) {
                min = point.y;
            }
        }
        return min;
    }

    private double getMaxY(java.util.List<SonarDataPoint> points) {
        double max = -Double.MAX_VALUE;
        for (SonarDataPoint point : points) {
            if (point.y > max) {
                max = point.y;
            }
        }
        return max;
    }
}

class SonarDataPoint {
    double x, y, z;

    public SonarDataPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
