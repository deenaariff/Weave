from sys import executable
from subprocess import Popen, CREATE_NEW_CONSOLE
import flask

node_config_file = 'nodes.xml'

f = open(node_config_file,'w'):
	data = f.read()

### Process xml and pass in obtain number of nodes
num_nodes = 0

for i in range(0,num_nodes):
	Popen(['java -jar',i, data], creationflags=CREATE_NEW_CONSOLE)

input('Enter to exit from this launcher script...')

