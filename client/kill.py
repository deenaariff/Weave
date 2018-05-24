from psutil import process_iter
from signal import SIGTERM # or SIGKILL

for port in [8080,8081,8082,8090,8091,8092,9000,9001,9002]:
	for proc in process_iter():
	    for conns in proc.connections(kind='inet'):
	        if conns.laddr.port == port:
                    print conns
	            proc.send_signal(SIGTERM) # or SIGKILL
	            continue
