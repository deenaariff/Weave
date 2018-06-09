from helpers.inputHelper import InputHelper as IH
import helpers.clientMessageHelper as um
import helpers.dataHelper as dh
import helper.dockerCluster as Cluster
import sys,time

run_client = False  # Run Client in Loop Until User Quits

configs = dh.get_config('resources/nodes.xml')

print "Initializing Cluster..."
cluster = Cluster(configs, sys.argv[1])

print "Giving Time for Processes to Startup..."
time.sleep(3)

cluster.find_leader();

ih = IH(cluster)

while run_client:

    # Print Commands User Can Enter
    ih.print_user_options()

    # Handle User Response
    text = raw_input(">> ")
    tokens = text.split(" ")

    send = True
    msg = ""
    cmd = tokens[0]
    tokens = tokens[1:]

    # Create Appropriate MSG Given User cmd
    # Handle Invalid User cmd if necessary
    if cmd == 'q' or cmd == 'exit':
        run_client = False
    elif cmd == 'list':
        ih.handle_list()
    elif cmd == 'data':
        ih.handle_get_data()
    elif cmd == 'update':
        ih.handle_update(tokens)
    elif cmd == 'logs':
        ih.handle_logs(tokens)
    elif cmd == 'crash':
        ih.handle_crash(tokens)
    else:
        print um.INVALID_CMD






