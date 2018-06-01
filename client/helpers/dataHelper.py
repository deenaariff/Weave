import urllib2, json, time

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

