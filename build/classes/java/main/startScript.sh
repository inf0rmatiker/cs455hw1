CLASSES=$PWD
SCRIPT="cd $CLASSES;
java -cp . main.java.cs455.overlay.node.MessagingNode helena 5003"
#$1 is the command-line argument specifying how many times it should open the machine list. 
#If 2 is specified, and there are 10 machines on the list, this will open and run on 20 machines.
for ((j=0;j<$1;j++))
do
	COMMAND='gnome-terminal'
	for i in `cat machine_list`
	do
		echo 'logging into '$i
		OPTION='--tab -e "ssh -t '$i' '$SCRIPT'"'
		COMMAND+=" $OPTION"
		done
		eval $COMMAND &
	done
