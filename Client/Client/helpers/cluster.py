import dataHelper as dh
import time, sys
from tabulate import tabulate
import urllib2


class Cluster:

    def __init__(self, routes):
        self.routes = routes

        self.urls = []
        for route in self.routes:
            self.urls.append("http://" + str(route['IP']) + ":" + str(route['endpoint_port']))

        self.default_keys = ["IP Address","Endpoint Port","Voting Port","Heartbeat Port", "State", "Term","Last Applied Index","Commit Index","Votes Obtained"]

        self.leader = None;
        self.find_leader();

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

    def update_routes(self, routes):
        self.routes = routes
        self.urls = []
        for route in self.routes:
            self.urls.append("http://" + route['IP'] + ":" + route['endpoint_port'])

    def print_cluster(self):

        data = []
        for url in self.urls:
            tmp = []
            try:
                rsp = dh.make_request(url)
                for key in self.default_keys:
                    tmp.append(rsp[key])
            except Exception:
                tmp = ["DEAD"] * len(self.default_keys)
            data.append(tmp)

        print tabulate(data, headers=self.default_keys)
