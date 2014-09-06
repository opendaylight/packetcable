# Something from the real world

# 0. Create a new cmts for adding cableflows
# Operation: POST
# URI: http://192.168.11.1:8181/restconf/config/opendaylight-inventory:nodes/node/cmts:1
# /restconf/config/opendaylight-inventory:nodes/node/cmts:1

cmts1 = {
    "cmts": {
        "name": "SunnyvaleCMTS",
        "id": "2",
        "enable": "true",
	"ipaddress": "10.200.90.3",
	"port": 3918,
    }
}

# 1. Create a new flow on the switch cableflow:1 (in table 1)
# Operation: POST
# URI: http://192.168.11.1:8181/restconf/config/opendaylight-inventory:nodes/node/cableflow:1
# /restconf/config/opendaylight-inventory:nodes/node/cableflow:1


cableflow1 = {
    "flow": {
        "barrier": "false",
        "flow-name": "FooXCableFlow1",
        "id": "111",
        "installHw": "false",
        "instructions": {
            "instruction": {
                "apply-actions": {
                    "action": {
                        "drop-action": null,
                        "order": "0"
                    }
		    "action": {
			traffic-profile": "best-effort",
                        "order": "0"
		    }
                },
                "order": "1"
            }
        },
        "match": {
            "ethernet-match": {
                "ethernet-type": {
                    "type": "2048"
                }
            },
            "ipv4-destination": "10.0.0.1/24"
        },
        "priority": "2",
    }
}


# 2. Change strict to true in previous flow
# Operation: PUT
# URI: http://192.168.11.1:8080/restconf/config/opendaylight-inventory:nodes/node/cableflow:1/table/1/flow/111
# /restconf/config/opendaylight-inventory:nodes/node/cableflow:1/table/1/flow/111

cableflow2 = {
    "flow": {
        "barrier": "false",
        "flow-name": "FooXCableFlow2",
        "id": "111",
        "installHw": "false",
        "instructions": {
            "instruction": {
                "apply-actions": {
		    "action": {
			traffic-profile": "best-effort",
                        "order": "0"
		    }
                },
                "order": "0"
            }
        },
        "match": {
            "ethernet-match": {
                "ethernet-type": {
                    "type": "2048"
                }
            },
            "ipv4-destination": "10.0.0.1/24"
        },
        "priority": "2",
    }
}

# 3. Show flow - check that strict is true
# Operation: GET
# URI: http://192.168.11.1:8080/restconf/config/opendaylight-inventory:nodes/node/cableflow:1/table/1/flow/111
# /restconf/config/opendaylight-inventory:nodes/node/cableflow:1/table/1/flow/111

#4. Delete created flow
# Operation: DELETE
# URI: http://192.168.11.1:8080/restconf/config/opendaylight-inventory:nodes/node/cableflow:1/table/1/flow/111
# /restconf/config/opendaylight-inventory:nodes/node/cableflow:1/table/1/flow/111

