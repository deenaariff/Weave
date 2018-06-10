# Weave

<img src="https://raw.githubusercontent.com/deenaariff/Weave/master/media/Weave.png" alt="alt text" width="250" height="250">

Weave is a distributed key-store implemented in Java and uses a custom implementation of RAFT based leader election for consensus. It is designed to be fast, accessible, and fault-tolerant.

Weave was intended to further the goals of the original RAFT paper, including understandability. This is why Weave is well-documented and easily extendible. It also includes a Python-based Command Line Client to test and analyze the state of the key-store.

Among our three main goals in creating Weave, were to design:

1) A fault-tolerant distributed key-store for cloud based environments 
2) A go-to implementation of RAFT for distributed Java Applications that require a consensus algorithm
3) A distributed environment for academic enrichment in the subject of Distributed Consensus 

## JAVADOC

You can read the Javadoc [here](https://deenaariff.github.io/Weave/).

## An Introduction to RAFT Consensus

RAFT was introduced in the paper “In Search of an Understandable Consensus Algorithm” by Diego Ongaro and John Osterhout, which was proposed as an alternative to the Paxos consensus Algorithm introduced by Leslie Lamport in the 1990’s.

The original Paxos paper is considered to be notoriously difficult to implement, resulting in various industry implementations that cannot be formally proven and are therefore unsafe to a degree. To create a solution to this problem, RAFT was offered as an easier method to implement distributed consensus.

RAFT operates off the principle of Leader-Election, in which the cluster of nodes elects one node a leader by which to propagate all results. The leader will keep a log of commands (these can be interpreted as key-value store commands), and will ensure that all followers become eventually consistent with its own latest appended Log entry.

RAFT is also designed to be fault-tolerant. If a leader dies, then a follower will consider itself a candidate, since leaders notify followers of their aliveness through periodic heartbeats. The timeout to become a Candidate is randomized between 150-300 ms. This ensures the probability of a split vote, once a Candidate attempts to request votes, is low. Candidates can also restart elections, with their own randomized timeout should a split vote occur.

If a leader rejoins the cluster after having been paused, it will default to a follower as all nodes increase their term integer value on each election. Leaders sync followers to terms as well. Therefore, a leader with a old term cannot hijack cluster with stale values. 

## Build the JAR file with Maven

To buil Weave with Maven, download Apache Maven using the following instructions at https://maven.apache.org/download.cgi.

To build the JAR file for the application run build.sh in the root directory of the repository or run the following command in the root directory. 

`` mvn clean compile assembly:single``

Unless otherwise specificed, Maven will store the built JAR in the a target/ directory.

## Starting a Single Node

## Testing a Cluster

### Run Weave Locally using JAR File
To run a Weave Raft cluster of 3 nodes, create a file nodes.xml with the following configurations.

    <WeaveConfig>
    <nodes>
        <node id="1">
            <ip>localhost</ip>
            <client>8080</client>
            <heartbeat>8081</heartbeat>
            <voting>8082</voting>
        </node>
        <node id="2">
            <ip>localhost</ip>
            <client>8090</client>
            <heartbeat>8091</heartbeat>
            <voting>8092</voting>
        </node>
        <node id="3">
            <ip>localhost</ip>
            <client>9000</client>
            <heartbeat>9001</heartbeat>
            <voting>9002</voting>
        </node>
    </nodes>
    </WeaveConfig>

This is a configuration file that will be passed into every node. Each node will be assigned an id (passed as a jar file paramater) which will cause it to listen for client requests on the "client port", heartbeat (AppendEntries RPC) messages on the "heartbeat" port, and "voting" requests and responses on the voting port.

Open three separate terminal tabs and run the following to start the first node.

    java -jar Weave.jar -id 1 -config nodes.xml

To run the second node run:

    java -jar Weave.jar -id 2 -config nodes.xml

Likewise, for the third:

    java -jar Weave.jar -id 3 -config nodes.xml
    
### Run Weave Locally using the Python Client

This is still a work in progress, the documentation will become updated once this feature is fully-tested.

## Watching for Election Changes

## Future Goals

## Contributions

Contact Deen Aariff directly at aariff.deen@gmail.com if you would like to contribute to the project.