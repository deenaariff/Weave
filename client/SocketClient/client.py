import sys
import socket
import os
import subprocess
import pprint
import json
from threading import Thread

def listen(port):

    # Create a socket object
    s = socket.socket()
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    # Bind the Socket to localhost at a given port
    try:
        s.bind(('', port))
        print "Server Listening on Port: " + str(s.getsockname()[1])
    except socket.error as err:
        print "Socket Connection Error %s" % err

    # Listen for incoming messages

    # Listen for incoming clients and handle requests
    while True:

        try:
            # conn is a new socket object
            conn, address = s.accept()

            payload = conn.recv(10000)

            if payload:
                pprint(json.loads(payload))

            conn.send(result)

        except KeyboardInterrupt:
            conn.close()
            break

        conn.close()


if __name__ == "__main__":

    thread = Thread(target = listen(8000))

    s = socket.socket()

    while True:

        try:

            # Print Commands User Can Enter
            ih.print_user_options()

            # Handle User Response
            text = raw_input(">> ")
            tokens = text.split(" ")

            send = True
            msg = ""
            cmd = tokens[0]

            # Create Appropriate MSG Given User cmd
            # Handle Invalid User cmd if necessary
            if cmd == 'get':
                data = {
                    'id': 1,
                    'cmd': cmd,
                    'var': tokens[1],
                    'leader' : True
                }
                msg = json.dumps(data)
            elif cmd == 'set':
                data = {
                    'id' : 1,
                    'cmd': cmd,
                    'var': tokens[1],
                    'val': tokens[2]
                }
                msg = json.dumps(data)

            s.connect(('', 8080))

            # If send = True send the data and handle server response
            if send:
                s.sendall(msg)
                ih.handle_server_response(s, cmd)

        # Handle any socket errors
        except socket.error as err:

            print um.SOCKET_ERROR
            print err
            exit(1)

        except KeyboardInterrupt:
            s.close()
            exit(0)

        # Close the socket connection
        s.close()

    