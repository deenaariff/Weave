import urllib2, json, time
import subprocess
import os

from psutil import process_iter
from signal import SIGTERM # or SIGKILL

cmd = "java -jar /Users/deenaariff/Documents/DVKS/Raft/target/Weave-1.0-SNAPSHOT-jar-with-dependencies.jar"

def kill_cluster():

	for port in [8080,8090,9000]:
		for proc in process_iter():
		    for conns in proc.connections(kind='inet'):
		        if conns.laddr.port == port:
		            proc.send_signal(SIGTERM) # or SIGKILL
		            continue

def start_cluster(nodes):

	for i in range(1,nodes+1):

		filename = "logs/log" + str(i) + ".txt"

		try:
		    os.remove(filename)
		except OSError:
		    pass

		open(filename, "w")

		subprocess.Popen([cmd + " " + str(i) + " &> " + filename + " &"], shell=True)

def make_request(ip, path=None, args=None):
    url = ip + "/"

    if path:
        url += path

    if args:
        url += "/".join(args)

    r = urllib2.urlopen(url)
    data = json.loads(r.read())
    return data

