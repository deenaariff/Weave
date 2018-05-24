import dataHelper as dataHelper
import clientMessageHelper as um
from tabulate import tabulate


class InputHelper:

    def __init__(self, cluster):
        self.cluster = cluster

    # Print All Commands User Can Enter
    def print_user_options(self):
        print
        data = [
            ['1','list','node # (optional)','<>','Display Info From Each Node in the Cluster'],
            ['2','data','<>','<>','Show the Data Store Across all Nodes'],
            ['3','update','key','value','Update the key-value store'],
            ['4','crash','node #','<>','Virtually Crash a Given Node in the Cluster'],
            ['5','kill','node # (optional)','<>', 'Completely kill a node or cluster']
        ]
        print tabulate(data, headers=['#','Command','Argument 1','Argument 2','Description'])
        print

    # Format response if user sends command 'download'
    def handle_list(self):
        self.cluster.print_cluster()

      # Format response if user sends command 'download'
    def handle_get_data(self):

        for ip in self.cluster.urls:

            rsp = dataHelper.make_request(ip,"getKeyStore")

            keys = ["Endpoint","Term","Commit Index","Data"]

            for key in keys:
                print key + ":", rsp[key]
        
            print
    
    def handle_logs(self):
        print "Implement this"

    # Format response if user sends command 'list'
    def handle_update(self,tokens):
        if len(tokens) < 2:
            print um.MISSING_ARGS
        else:

            self.cluster.find_leader();

            rsp = dataHelper.make_request(self.cluster.leader,"update/",tokens)

            keys = ["Total Logs","Key","Value","State", "Term"]

            for key in keys:
                print key + ":", rsp[key]

    # Format response if user sends command 'upload'
    def handle_crash(self,tokens):
        if len(tokens) < 1:
            print um.MISSING_ARGS
        else:
            rsp = dataHelper.make_request(self.cluster.leader,"crash/",tokens)

