# Distributed Messaging System

## Overview
This is a simple distributed messaging system written in Java. It allows multiple servers and clients to talk to each other while keeping everything in sync. We built this to learn how distributed systems handle things like node failures and data replication across a cluster.

## Module Responsibilities
Our project is divided into several parts, each handling a specific job:
- **`server`**: The main brain of the system. It manages incoming client connections and coordinates with other servers.
- **`client`**: Provides the interface for users to send and receive messages from the server cluster.
- **`fault`**: Keeps an eye on other nodes. If a server goes down, this module detects it using heartbeats so we can recover quickly.
- **`replication`**: Makes sure every node has a copy of the messages. It manages consistency so no data is lost if one node fails.
- **`consensus`**: Helps the servers agree on who the leader is and handle cluster-wide decisions.
- **`storage`**: Deals with saving messages locally on the disk so they are still there after a restart.
- **`time`**: Keeps track of when things happened using logical clocks to avoid confusion between nodes.
- **`model`**: Contains the basic building blocks like message types and data structures used throughout the system.

## Key Features
- **Reliable Messaging**: Even if one server stops working, the others keep going.
- **Automatic Recovery**: The system automatically detects when a crashed node comes back and updates it.
- **Data Sync**: Messages are copied to multiple servers so the data is always safe.

---
*Developed as part of our Distributed Systems coursework.*
