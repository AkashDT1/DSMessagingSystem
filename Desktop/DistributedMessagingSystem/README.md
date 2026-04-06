# SA3 Distributed Messaging System

A robust, fault-tolerant, and consistent distributed messaging system prototype developed as part of the SA3 Project. This system features real-time message replication, leader election, and a modern web dashboard for monitoring cluster status and inter-client communication.

## Team Members & Responsibilities

| Name | Registration Number | Email | Module Responsibility |
| :--- | :--- | :--- | :--- |
| Akash .D | IT24101853 | [it24101853@my.sliit.lk](mailto:it24101853@my.sliit.lk) | Fault Tolerance |
| Asmal J.M | IT24610803 | [it24610803@my.sliit.lk](mailto:it24610803@my.sliit.lk) | Replication & Consistency |
| Akil M.R.M | IT24104314 | [it24104314@my.sliit.lk](mailto:it24104314@my.sliit.lk) | Time Synchronization |
| Sanjeevsaran J | IT24104155 | [it24104155@my.sliit.lk](mailto:it24104155@my.sliit.lk) | Consensus Algorithm |

## Overview

The Distributed Messaging System (DMS) is designed to ensure high availability and data integrity across a cluster of servers. 
- **Fault Tolerance:** Automatically detects node failures and handles recovery.
- **Replication:** Uses a leader-follower model to ensure messages are replicated across all active nodes.
- **Consensus:** Implements a leader election algorithm to maintain system coordination.
- **Time Synchronization:** Ensures consistent message ordering across the distributed environment.

## Running the Prototype

### Prerequisites
- **Java Development Kit (JDK) 11 or higher** installed.
- A modern web browser (Chrome, Firefox, or Edge).

### Step 1: Compile the Source Code
Navigate to the project root directory and run the following command to compile all Java files into the `bin` directory:

```powershell
# For Windows (PowerShell)
if (!(Test-Path bin)) { New-Item -ItemType Directory bin }
Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { javac -d bin $_.FullName }
```

### Step 2: Start the Distributed Cluster
Open three separate terminal windows to simulate a 3-node cluster. Run one server instance in each terminal using the same command but provide different ports when prompted.

**Terminal 1:**
```powershell
java -cp bin server.MessagingServer
# When prompted, enter: 5001
```

**Terminal 2:**
```powershell
java -cp bin server.MessagingServer
# When prompted, enter: 5002
```

**Terminal 3:**
```powershell
java -cp bin server.MessagingServer
# When prompted, enter: 5003
```

*Note: The servers will automatically start their Web Dashboard APIs on ports 3001, 3002, and 3003 respectively.*

### Step 3: Launch the Dashboard
1. Locate the `dashboard` folder in the project root.
2. Open `index.html` in your web browser.
3. The dashboard will automatically connect to the local nodes.

## How to Test
1. **Send Messages:** Select a client (Client A or B) and a gateway node, then type a message and hit send. You should see the message appear across all nodes.
2. **Simulate Failure:** In the dashboard, use the "Simulate Server Failure" button to stop one of the servers. Observe how the system logs the failure and how the leader election re-evaluates the cluster state.
3. **Recovery:** Restart the failed server in its terminal. It will automatically catch up with the missed messages from its neighbors on startup.

---
© 2026 SA3 Project Team
