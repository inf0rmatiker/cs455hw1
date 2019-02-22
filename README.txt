Author: Caleb Carlson
Date Created: 02/20/2019
Assignment: HW1

=========================================
            R E A D M E
=========================================

>>>>>>>>> Execution instructions for HW1: <<<<<<<<<<<<<
1. Untar

2. $ gradle build
	> Build the java .class files from the location of build.gradle

3. $ java -cp build/classes/java/main/ main.java.cs455.overlay.node.Registry 5003
	> Run the Registry (entrypoint) with a port (i.e. 5003)

4. $ help
	> View available commands (optional)

5. $ vim machine_list
	> Write the names of all machines you would like to connect to, separated by newlines

6. Run the startScript provided in the project directory
	> To run the startScript:
		- $ cd ../<project_directory>/
		- $ ./startScript.sh <num_nodes_per_machine>

7. Navigate back to Registry's terminal

8. list-messaging-nodes to verify all nodes are registered.
	> If not, Ctrl+C and restart from step 4.

9. setup-overlay <num_links>
	> To set up the overlay

10. send-overlay-link-weights
	> To send link weights to messages

11. start <number_of_rounds>
	> To start rounds

12. Wait ~15s, then verify the output.

>>>>>>> Cleaning Instructions for HW1: <<<<<<<<<<<<<<
1. Navigate to project directory, run $ gradle clean

Side Note:
Sorry about the code vomit


