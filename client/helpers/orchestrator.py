import os
import subprocess
import json


# SCP a file to another machine
def send_file(machine, file):
	local_path = RESOURCE_PATH + file
    login = AUTH + "@" + machine
    scp_to = login + ":" + DESTINATION_PATH + file
    cmd = "mkdir -p " + DESTINATION_PATH

    try:
        out = subprocess.Popen(["ssh", "-o", "StrictHostKeyChecking=no", login, cmd], shell=False])
        out.wait()
        out = subprocess.Popen(["scp", "-B", local_path, scp_to])
        out.wait()
    except subprocess.CalledProcessError:
        return "ERROR: An Error Occured over SSH/SCP", 1

    return "File Successfully Written to Remote Machine (" + machine + ")", 0


def run_jar(machine,jar,args):
	cmd = "java -jar" + ' '.join([jar] + args)

    try:
        out = subprocess.Popen(["ssh", "-o", "StrictHostKeyChecking=no", login, cmd], shell=False])
        out.wait()
    except subprocess.CalledProcessError:
        return "ERROR: An Error Occured over SSH/SCP", 1

    return "Running File on Remote Machine", 0


class Orchestrator:

	self.AUTH = os.environ['USER'] # grab user authentication info from dc machine
	self.CONFIG_FILE = 'config.json'
	self.RESOURCE_PATH = './resources/'
	self.DESTINATION_PATH = 'application/'


	# Load the configuration data
	with open(CONFIG_FILE) as f:
	    data = json.load(f)


	self.machines = data['ips'] # obtain ip addresses to orchestrate on
	self.jars = data['jars']
	self.deps = data['dependencies']

	self.ips = []
	self.ports = []

	for machine in machines:
		connection_info = machine.split(":")
		self.ips.append(connection_info[0])
		self.ports.append(connection_info[1])

	def run(self, num):

		# move files
		for ip in self.machines:

			for jar in jars:
		        send_file(ip, jar["jar"])

		    for dep in deps:
		    	send_file(ip, dep)

		# runfiles
		index = 0
		for ip in self.machines:

			for jar in jars:
				args = jar['args']
				for arg in args:
					arg = arg.replace("%index",str(index+1))
				run_jar(jar,args)

			index += 1
