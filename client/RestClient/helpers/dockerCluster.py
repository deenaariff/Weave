import dataHelper as dh
import time, sys, os
import docker
import shutil

class Cluster:

    def __init__(self, configs, docker_image):

        self.docker_ip = "192.168.99.100"
        self.docker_image = docker_image;

        self.routes = []
        self.client = docker.from_env()
        self.containers = []
        self.default_keys = ["IP Address","Endpoint Port","Voting Port","Heartbeat Port", "State", "Term","Last Applied Index","Commit Index","Votes Obtained"]

        try:
            result = self.client.images.get(self.docker_image)
            print "Detected image '" + self.docker_image + "'"
        except docker.errors.ImageNotFound as e:
            print "Error: Image '" + self.docker_image + "' not Found"
            sys.exit(1)

        self.configs = configs
        self.num_nodes = len(configs)
        self.initialize_cluster()
        self.leader = None
    
    # Start a cluster of docker nodes
    # Delete existing logs 
    def initialize_cluster(self):

        d='./logs/'
        filesToRemove = [os.path.join(d,f) for f in os.listdir( d )]
        for f in filesToRemove:
            os.remove(f) 

        count = 0

        for config in configs:

            print "Starting Node: " + str(count+1)

            port_mapping = {}
            for port in config:
                port_mapping[str(port)+'/tcp'] = port

            result = self.client.containers.run(self.docker_image, detach=True, ports=port_mapping)
            self.containers.append(result)

            port = str(config[0])
            url = "http://" + self.docker_ip + ":" + port

            self.routes.append(url)

    def find_leader(self):
        if len(self.routes) > 0:
            while not self.leader:
                for url in self.urls:
                    try:
                        rsp = dh.make_request(url)
                        if rsp['State'] == 'LEADER':
                            self.leader = "http://" + rsp['IP Address'] + ":" + str(rsp['Endpoint Port'])
                    except urllib2.HTTPError, e:
                        print('HTTPError = ' + str(e.code))
                    except urllib2.URLError, e:
                        print('URLError = ' + str(e.reason))
                        print url + " not reachable"
                    except Exception, e:
                        print e
                time.sleep(1)
        else:
            print "Error no routes available"
            sys.exit()

    def print_cluster(self):

        data = []
        for url in self.routes:
            tmp = []
            try:
                rsp = dh.make_request(url)
                for key in self.default_keys:
                    tmp.append(rsp[key])
            except Exception:
                tmp = ["DEAD"] * len(self.default_keys)
            data.append(tmp)

        print tabulate(data, headers=self.default_keys)

    # Remove the cluster of docker nodes
    # Add all logs to files in logs/
    def remove_cluster(self):

        print "Stopping " + str(len(self.containers)) + " containers"
        file_index = 1

        for container in self.containers:

            filename = "logs/log" + str(file_index) + ".txt"

            with open(filename, "w+") as f:
                f.writelines(container.logs())
                f.close()

            container.stop()

            file_index += 1