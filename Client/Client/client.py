from helpers.inputHelper import InputHelper as IH
from helpers.cluster import Cluster as Cluster

from threading import Thread
import helpers.clientMessageHelper as um
import helpers.dataHelper as dh
import sys,time

run_client = True  # Run Client in Loop Until User Quits

print "Initializing Cluster..."
thread = Thread(target = dh.start_cluster, args = (3, ))
thread.start()

print "Waiting for Startup..."
time.sleep(2);
print "Initializing Request for Routes"


ip = sys.argv[1]
port = sys.argv[2]
init_url = "http://" + ip + ":" + port

rsp = dh.make_request(init_url, "routes")
routes = rsp['Routes']

cluster = Cluster(routes)
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
    elif cmd == 'update':
        ih.handle_update(tokens)
    elif cmd == 'logs':
        ih.handle_logs(tokens)
    elif cmd == 'crash':
        ih.handle_crash(tokens)
    else:
        print um.INVALID_CMD


thread.join()

