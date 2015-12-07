#!/usr/bin/python 
import SimpleHTTPServer
import SocketServer
import mimetypes

PORT = 8000

Handler = SimpleHTTPServer.SimpleHTTPRequestHandler

Handler.extensions_map['.cljs']='text/plain'
httpd = SocketServer.TCPServer(("", PORT), Handler)

print "serving at port", PORT
httpd.serve_forever()
