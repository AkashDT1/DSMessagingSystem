# Distributed Messaging System

## Overview
This is a robust, distributed messaging system built in Java. It enables seamless communication between multiple server nodes and clients while ensuring high availability and data integrity across a cluster. The system is designed to handle network partitions and node failures through integrated fault tolerance and replication mechanisms.

## Main Modules
- **`client`**: User interface and client-side communication logic.
- **`consensus`**: Implements leader election and cluster consensus algorithms.
- **`fault`**: Contains failure detection and heartbeat monitoring logic.
- **`model`**: Core data structures and message definitions.
- **`replication`**: Manages data consistency and log replication across nodes.
*   **`server`**: The central messaging service engine and connection manager.
- **`storage`**: Handles local message persistence and retrieval.
- **`time`**: Implements time synchronization and clock management (e.g., Lamport clocks).

## Key Features
- **Fault Tolerance**: Automatic detection of failed nodes and graceful recovery.
- **Replication & Consistency**: Multi-node data replication ensuring no single point of failure.
- **Consensus**: Leader-based architecture for coordinated state updates.
- **Time Synchronization**: Consistent ordering of events across the distributed cluster.

## Team & Responsibilities
- **Akash**: Lead Developer - Fault Tolerance, Consensus Logic, and Cluster Management.
- **Team Member 2**: Replication Strategy and Data Consistency.
- **Team Member 3**: Time Synchronization and Storage Layer.
- **Team Member 4**: Client Interface and Server Networking.

---
*Developed as part of the Distributed Systems coursework.*
