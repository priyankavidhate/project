# This file just grabs the list of pod IP addresses for the service
# called "couchdb" and feeds those as `couchdb@PODIP` nodes to mem3.
# It's just a proof of concept; the proper solution is likely a module
# in mem3 itself.

import json
import os
import requests
import time

def discover_peers():
    f = open("/var/run/secrets/kubernetes.io/serviceaccount/token", 'r')
    token = f.read()
    
    uri = 'https://{0}:{1}/api/v1/namespaces/{2}/endpoints/{3}'.format(
        os.environ.get("KUBERNETES_PORT_443_TCP_ADDR", "10.0.0.1"),
        os.environ.get("KUBERNETES_PORT_443_TCP_PORT", "443"),
        os.environ.get("POD_NAMESPACE", "default"),
        os.environ.get("COUCHDB_SERVICE", "couchdb")
    )
    headers = {
        'Authorization': "Bearer {0}".format(token)
    }
    print 'discover_peers'
    print uri
    resp = requests.get(uri, headers=headers, verify=False)
    if resp.status_code == 200:
        body = resp.json()
        for key in ('addresses', 'notReadyAddresses'):
            addresses = body['subsets'][0].get(key, [])
            ips = [addr['ip'] for addr in addresses]
            print key, ips
            connect_the_dots(ips)
        sleep_forever()
    else:
        print 'Error'
        print resp.text
     
def connect_the_dots(ips):
    for ip in ips:
        uri = "http://localhost:5986/_nodes/couchdb@{0}".format(ip)
        doc = {}
        print uri
        r = requests.put(uri, data = json.dumps(doc))
        print ip, r.status_code

def sleep_forever():
    while True:
        time.sleep(5)

if __name__ == '__main__':
    #print os.environ
    discover_peers()
