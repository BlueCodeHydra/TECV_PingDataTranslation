"""
                                                                                        
                                                   
       /__  ___/     //   / /     //   ) )       ||   / / 
         / /        //____       //              ||  / /  
        / /        / ____       //         ____  || / /   
       / /        //           //                ||/ /    
      / /        //____/ /    ((____/ /          |  /     


Project: TEC-V
Author: Michel Dowling
Description: This Python script reads data from Blue Robotics Ping360 sonar
             and converts the data into two values:
                - Angle:    Identifies the angle in which is being scanned.
                - Distance: Finds the best distance to a solid object based off 
                            the intensity values recived from that angle.
             Three data points are then outputed to a data.csv file which saves 
             this information for later usage.

This code is provided under the MIT License.
Copyright (c) 2023 Michel Dowling

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files, to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
"""

from brping import Ping360
import time
import socket
from datetime import date
import struct
import statistics
import csv

def main():
    # Ping initialization
    ping360 = Ping360()

    # For UDP
    ping360.connect_udp("192.168.2.179", 12345)

    if ping360.initialize() is True:
        print("Connection successful!")
    else:
        print("Failed to initialize Ping!")
        exit(1)

    data_outputFile = "data5.csv"

    ping360.set_gain_setting(2)
    ping360.set_transmit_frequency(1000)

    print("------------------------------------")
    print("Starting Ping..")
    print("Press Enter for the next iteration or CTRL+C to exit")
    print("------------------------------------")

    input("Press Enter to start...")  # Initial start

    # Create a dictionary to store raw data for each angle
    raw_data = {}
    median_distances = {}

    depth = 0  # Initialize depth value

    try:
        while True:  # Continuous loop
            user_input = input("Press Enter for the next iteration or type 'quit' to exit: ")

            if user_input == 'quit':
                print("Exiting...")
                break

            for currentAngle in range(400):
                # Read a single iteration of intensity data
                ping_data = ping360.transmitAngle(currentAngle)
                
                # Extracting intensity as integer values
                intensity_data = [(struct.unpack('!H', int(data).to_bytes(2, byteorder='big'))[0], i) for i, data in enumerate(ping_data.msg_data)]

                # Store the raw data for the current angle
                raw_data[currentAngle] = intensity_data

            # Perform calculations on the raw data for all 399 angles
            for angle, intensity_data in raw_data.items():
                mps = meters_per_sample(ping_data)  # Distance per sample

                # Filtering out intensity values under 0.2 meters
                filtered_intensity_data = [(intensity, index) for intensity, index in intensity_data if compute_distance(index, mps) >= 2]

                # Sorting filtered intensity data in descending order
                sorted_intensity_data = sorted(filtered_intensity_data, key=lambda x: x[0], reverse=True)

                # Saving the top 20 highest intensity values along with their index
                top_20_intensity_data = sorted_intensity_data[:20]

                # Compute distances and intensities of the different samples
                distance_mat = []
                intensity_mat = []
                top_20_distances = []  # Array to store distances from the top 20

                for i in range(len(ping_data.msg_data)):
                    distance = compute_distance(i, mps)  # Compute distance using the sample index
                    intensity = ping_data.msg_data[i]
                    distance_mat.append(distance)
                    intensity_mat.append(intensity)

                    # Check if the distance is in the top 20, and if so, add it to the array
                    if i in [index for _, index in top_20_intensity_data]:
                        top_20_distances.append(distance)

                # Calculate the median of the lowest 10 values from the top 20 distances
                lowest_10_distances = sorted(top_20_distances)[:10]
                median_distance = statistics.median(lowest_10_distances)

                # Store the median distance for the angle
                median_distances[angle] = median_distance

            # Output the data for each angle with the depth value
            for angle, distance in median_distances.items():
                print(f"Depth: {depth}, Angle: {angle}, Median Distance: {distance:.2f} meters")

            # Write the data to the CSV file (append mode)
            with open(data_outputFile, mode='a', newline='') as csv_file:
                fieldnames = ['Depth', 'Angle', 'Median Distance']
                writer = csv.DictWriter(csv_file, fieldnames=fieldnames)

                for angle, distance in median_distances.items():
                    writer.writerow({'Depth': depth, 'Angle': angle, 'Median Distance': distance})

            depth += 1  # Increment the depth value after completing a loop

    except KeyboardInterrupt:
        pass

def meters_per_sample(ping_message, v_sound=1475):
    # Sample period is in 25ns increments, time of flight includes there and back, so divide by 2
    return v_sound * ping_message.sample_period * 12.5e-9

def compute_distance(sample_index, mps):
    return sample_index * mps

if __name__ == "__main__":
    main()
