#!/usr/bin/python

import base64
import json
import requests
import simplejson

def main():
     data = '{ "persons": [ { "id": "0", "index_number": "10.20.20.20", "name": "Nag", "system": false, "url": "https://www.google.com", "used_by": [ { "id": "2", "name": "A" } ] }, { "id": "2", "index_number": "10.20.20.21", "name": "John", "system": false,  "url": "https://www.yahoo.com", "used_by": [ { "id": "4", "name": "b" } ] }  ] } '

     j = json.loads(data)
     json_string = json.dumps(j,sort_keys=True,indent=2)
     print json_string
     parent =  j["persons"]
     for item in parent:
         print item["name"]
         print item["id"]

if __name__ == "__main__":   
   main()


