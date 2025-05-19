# Smart Waste Sorting System
A system that will automate waste classification and disposal based on images.
The system will retrieve images of waste and classify which category the waste belongs to
and choose which disposal bin to place the waste in.

### Waste classification x disposal
-----------------------------------------------------------------------
| WASTE    | Paper    | Plastic   | Glass    | Metal     | Organic     |
-----------|----------|-----------|----------|-----------|---------- |
| DISPOSAL | Blue Bin | Yellow Bin | Green Bin | Red Bin | Black Bin |
-----------------------------------------------------------------------
![a picture of recycling bins](https://github.com/user-attachments/assets/fe2b118d-d8cd-4b06-8974-e8e72957522b)

## Solution Overview
The problem being addressed here is the lack of recycling. This system aims to automate recycling.
The waste that is collected is then disposed into their correct bins, thus enforcing recycling.


# Robotic Waste Collection and Classification Simulation

**Mini CS Project – Conceptual System for South Africa's Waste and Recycling Challenges**

## Problem: Waste Generation and Management

* South Africa produces enormous waste volumes each year, straining municipal systems.
* A significant portion of waste goes uncollected, leading to illegal dumping and pollution.
* Many landfill sites do not meet environmental standards, exacerbating health and ecosystem impacts.
* Innovative, sustainable waste management solutions are urgently needed to address this growing crisis.

## Problem: Recycling and Infrastructure Gaps

* Only a small fraction of waste is recycled; most ends up in landfills or is mismanaged.
* Public awareness and recycling culture are weak, with limited incentives to encourage recycling.
* Formal recycling initiatives are under-resourced and often unfunded, slowing progress.
* A large informal recycling sector (waste pickers, buy-back centers) already exists, indicating potential if scaled and supported.

## Proposed Solution: Robots & Simulation

* Develop an automated system where robots navigate a simulated map to collect, classify, and dispose of waste.
* The simulation models the environment as a graph: nodes = locations (neighborhoods, bins) and edges = paths between them.
* Robots use sensors to detect waste; an onboard AI model classifies each item (plastic, metal, etc.) in real time.
* Simulation allows testing strategies (robot coordination, waste distributions) before field deployment, aligning with calls for alternative waste management methods.

## Methodology: Graph-based Map Design

* Represent the target region as a graph of nodes (e.g., city blocks or waste collection points) and edges (roads or paths).
* This abstraction simplifies path planning: robots navigate by moving from node to node along edges.
* The graph model is easily extensible to add new areas or obstacles and visualize coverage.
* \[Insert Map Diagram Here]

## Methodology: Robot Navigation

* Each robot travels along graph edges using path-planning algorithms (e.g., shortest-path routing, heuristic search).
* Robots can coordinate to cover different parts of the map efficiently, reducing overlap.
* At each node, sensors check for waste. If found, the robot pauses to classify and pick up items.
* \[Insert Robot Path Diagram Here]

## Methodology: Waste Classification

* Robots capture images of detected waste items (using cameras or sensors).
* A Graph Convolutional Network (GCN) model classifies each item. The image is over-segmented into regions to form a graph.
* Each graph node is a superpixel with features (color, texture). Edges link adjacent regions, encoding spatial structure.
* The GCN processes this graph to label the waste (plastic, metal, etc.), guiding robot disposal actions.
* \[Insert Classification Diagram Here]

## Graph Convolutional Networks for Classification

* GCNs generalize CNNs to graph-structured data, enabling image inputs to be treated as graphs.
* In GCN-based image classification, an image is segmented into superpixels, each becoming a graph node.
* Edges connect spatially adjacent regions, allowing the model to capture both local and global image features.
* Rodrigues & Carbonera (2024) showed that such GCN approaches can classify images effectively by leveraging graph structure.
* We adapt their idea: waste images are turned into graphs for GCN-based classification of material type.

## Simulation Workflow

* Implement the system in a robotics simulation platform (e.g., Gazebo or custom environment).
* Initialize the graph-based map, place a set number of robots, and randomly distribute waste items across nodes.
* During each simulation cycle, robots move along edges, detect and classify waste at nodes, then transport items to disposal points.
* Track key outcomes qualitatively: coverage of area, proportion of waste collected, and classification accuracy (no numeric reporting here).
* Different scenarios (robot count, waste density) can be simulated to evaluate system behavior.

## Potential Impact

* Automating collection and sorting could boost recycling rates and reduce reliance on landfills.
* Simulation outcomes can guide infrastructure and policy decisions (e.g., placement of recycling bins, number of robots needed).
* Identifying waste hotspots in simulation can raise awareness and target cleanup efforts effectively.
* Ultimately supports cleaner environments and progress toward a circular economy in South Africa.

## Conclusion & Next Steps

* This conceptual system shows how robots and AI can address waste management challenges in South Africa.
* Next steps: implement the simulation, train the GCN with real waste image data, and refine robot coordination algorithms.
* Future work: real-world robot trials, integration with city infrastructure (smart bins, etc.), and expansion to other regions.
* Goal: inspire practical solutions and collaboration between technology and waste management stakeholders.

## References

* United Nations Environment Programme. *South African Municipal Waste Management Systems: Challenges and Solutions*.
* Research and Markets (via Globe Newswire). *Recycling and Waste Management Industry Insights: South Africa Faces Critical Challenges*.
* Rodrigues, J. and Carbonera, J. (2024). *Graph Convolutional Networks for Image Classification: Comparing Approaches for Building Graphs from Images* (ICEIS 2024).
