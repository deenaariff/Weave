from helpers.inputHelper import InputHelper as IH
from helpers.cluster import Cluster as Cluster
#from helpers.orchestrator import Orchestrator as Orchestrator
#from threading import Thread
import helpers.clientMessageHelper as um
import helpers.dataHelper as dh
import sys,time

run_client = True  # Run Client in Loop Until User Quits

remote = sys.argv[1]

#print "Initializing Cluster..."
cluster = Cluster(3)

print "Giving Time for Processes to Startup..."
time.sleep(3)

print "Initializing Request for Routes..."
ip = sys.argv[2]
port = sys.argv[3]

init_url = "http://" + ip + ":" + port
rsp = dh.make_request(init_url, "routes")
routes = rsp['Routes']

cluster.initialize_routes(routes)
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






