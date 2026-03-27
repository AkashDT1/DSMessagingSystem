# System Test Scenarios

This document outlines simple test scenarios for our Distributed Messaging System to verify correctness during the final demo.

## 1. Node Failure Scenario
**Goal:** Verify the system detects a node crash.
- **Step 1:** Start three servers (A, B, and C).
- **Step 2:** Suddenly stop Server B.
- **Step 3:** Observe logs in Server A and C to see the `FailureDetector` identifying Server B as disconnected.
- **Expected:** Servers A and C should log that Server B is unreachable and adjust their active node list.

## 2. Data Replication Scenario
**Goal:** Verify that a message exists on multiple nodes.
- **Step 1:** Connect a client to the current Leader node (e.g., Server A).
- **Step 2:** Send a new message "Hello Distributed World".
- **Step 3:** Manually check the storage files (`server_storage_500x.dat`) for all active nodes.
- **Expected:** The message should be present in the storage files of both the leader and the follower nodes.

## 3. Leader Election Scenario
**Goal:** Verify a new leader is chosen if the current one fails.
- **Step 1:** Identify which server is currently the leader.
- **Step 2:** Stop that server.
- **Step 3:** Wait for the `FailureDetector` to trigger and the remaining nodes to initiate a new election.
- **Expected:** One of the remaining nodes should take over as the new leader, and clients should be able to send messages to it.

## 4. Message Ordering Scenario
**Goal:** Verify messages are processed in the correct sequence.
- **Step 1:** Send three messages in rapid succession (Msg 1, Msg 2, Msg 3).
- **Step 2:** Read the message list from a different node.
- **Step 3:** Check the sequence numbers or timestamps assigned.
- **Expected:** All nodes should display the messages in the exact same order (1, 2, then 3), maintaining consistency across the cluster.
