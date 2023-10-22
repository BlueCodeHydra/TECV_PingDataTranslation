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
 *              and converts it to a basic chart plot to aid in visualizing the outline 
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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

public class SonarData1 {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Sonar Data Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        SonarPlotPanel plotPanel = new SonarPlotPanel();

        frame.add(plotPanel);
        frame.setVisible(true);

        plotPanel.loadDataFromCSV("PoolTestData\\data1.csv"); // Replace with your data file path
    }
}

class SonarPlotPanel extends JPanel {
    private java.util.List<java.util.List<Point>> plotData = new ArrayList<>();
    private java.util.List<Color> plotColors = new ArrayList<>();
    private java.util.List<String> plotTitles = new ArrayList<>();

    public void loadDataFromCSV(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String line;
            java.util.List<Point> currentPlot = new ArrayList<>();
            double currentZ = -1;
            Color currentColor = null;
            String currentTitle = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    double z = Double.parseDouble(data[0]);
                    double angleDegrees = Double.parseDouble(data[1]);
                    double distance = Double.parseDouble(data[2]);

                    // Normalize the angle
                    double angleDegreesNormalized = angleDegrees % 360;
                    double angleRadians = Math.toRadians(angleDegreesNormalized);

                    double x = distance * Math.cos(angleRadians);
                    double y = distance * Math.sin(angleRadians);

                    if (z != currentZ) {
                        if (!currentPlot.isEmpty()) {
                            plotData.add(currentPlot);
                            plotColors.add(currentColor);
                            plotTitles.add(currentTitle);
                        }
                        currentPlot = new ArrayList<>();
                        currentZ = z;
                        currentColor = getRandomColor();
                        currentTitle = "Z: " + z;
                    }

                    currentPlot.add(new Point(x, y));
                }
            }

            if (!currentPlot.isEmpty()) {
                plotData.add(currentPlot);
                plotColors.add(currentColor);
                plotTitles.add(currentTitle);
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        int numPlots = plotData.size();

        // Set the margin size to create a border around the plotted points
        int margin = 5;

        for (int i = 0; i < numPlots; i++) {
            java.util.List<Point> data = plotData.get(i);
            Color color = plotColors.get(i);
            String title = plotTitles.get(i);

            double xMin = getMinX(data);
            double xMax = getMaxX(data);
            double yMin = getMinY(data);
            double yMax = getMaxY(data);

            // Adjust the drawing area by subtracting the margin
            xMin -= margin;
            xMax += margin;
            yMin -= margin;
            yMax += margin;

            double xRange = xMax - xMin;
            double yRange = yMax - yMin;
            double xScale = (width - 2 * margin) / xRange; // Consider the margin
            double yScale = (height - 2 * margin) / yRange; // Consider the margin

            g2d.setColor(color);

            for (Point point : data) {
                // Flip both x and y coordinates
                int x = (int) (width - margin - (point.x - xMin) * xScale);
                int y = (int) (height - margin - (point.y - yMin) * yScale);
                g2d.fillRect(x, y, 2, 2);
            }

            g2d.setColor(Color.BLACK);
            g2d.drawString(title, margin, margin + 20 + i * 20); // Display the title with margin
        }
    }



    private double getMinX(java.util.List<Point> data) {
        double min = Double.MAX_VALUE;
        for (Point point : data) {
            if (point.x < min) {
                min = point.x;
            }
        }
        return min;
    }

    private double getMaxX(java.util.List<Point> data) {
        double max = -Double.MAX_VALUE;
        for (Point point : data) {
            if (point.x > max) {
                max = point.x;
            }
        }
        return max;
    }

    private double getMinY(java.util.List<Point> data) {
        double min = Double.MAX_VALUE;
        for (Point point : data) {
            if (point.y < min) {
                min = point.y;
            }
        }
        return min;
    }

    private double getMaxY(java.util.List<Point> data) {
        double max = -Double.MAX_VALUE;
        for (Point point : data) {
            if (point.y > max) {
                max = point.y;
            }
        }
        return max;
    }

    private Color getRandomColor() {
        return new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
    }
}

class Point {
    double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
}