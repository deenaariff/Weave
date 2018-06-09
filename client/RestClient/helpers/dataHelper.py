import urllib2
import json
import time
import xml.etree.ElementTree

from signal import SIGTERM # or SIGKILL


def make_request(ip, path=None, args=None):
    url = ip + "/"

    if path:
        url += path

    if args:
        url += "/".join(args)

    r = urllib2.urlopen(url)
    data = json.loads(r.read())
    return data

def get_config(resource):
	result = []
	e = xml.etree.ElementTree.parse(resource).getroot()
	for child in e:
		node = {}
		if child.tag == 'node':
			_id = child.attrib['id']
			node['id'] = _id
			ports = []
			for port in child:
				if port.tag == 'ip':
					node[port.tag] = port.text
				else:
					ports.append(port.text)
			node['ports'] = ports
		result.append(node)
	return result



