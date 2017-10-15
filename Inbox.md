# Inbox End points

### Get Paired Orgs

```
GET  /org/pair/:orgId
Header Authorization : Id 
Id is you got when you creat account
```

Response

```
[
    {
        "name": "simis",
        "tag": "mysha, dudu",
        "branch": "vijay naga",
        "department": "cakes",
        "address": "vijay nagar, indore",
        "country": "India",
        "state": "Madhya Pradesh",
        "city": "Indore",
        "zip": "450001",
        "org_pic": "default",
        "items": [
            {
                "item": "teat",
                "brand": "check",
                "hsnCode": "abcsh"
            }
        ],
        "id": "dd942fd294b0812fa037e21a5a5abc86"
    }
]
```

### Get Orgs for the account

```
GET  /account/:Id/orgs
Header Authorization : Id 
Id is, you got when you creat an account
```

Response

```
[
    {
        "name": "RohitOrg",
        "tag": "rohit",
        "branch": "",
        "department": "",
        "address": "1234 wildwood ave",
        "country": "India",
        "state": "Maharashtra",
        "city": "pune",
        "zip": "411043",
        "org_pic": "default",
        "items": [
            {
                "item": "abcd",
                "brand": "test",
                "hsnCode": "12sh"
            },
            {
                "item": "test2",
                "brand": "abh"
            },
            {
                "item": "test",
                "brand": "xyz4"
            }
        ],
        "id": "ae4484c9359f5a8b3d62d0a55ec52f34",
        "band": 1
    }
]
```

### Get Inbox


```
GET  /org/:OrgId/inbox
Header Authorization : Id 
Id is, you got when you creat an account
And OrgId is organization id for which you want to get Inbox
```

Response

```
[
    {
        "id": "078d96523e664a495958dc5871c79432",
        "key": "dd942fd294b0812fa037e21a5a5abc86",
        "value": "078d96523e664a495958dc5871c79432",
        "doc": {
            "_id": "078d96523e664a495958dc5871c79432",
            "_rev": "1-bc808e2adaba8aafc879a8a305c00f3c",
            "value": {
                "status": "created",
                "shipment": [
                    {
                        "item": "teat",
                        "brand": "check",
                        "hsnCode": "abcsh",
                        "quantity": "20"
                    }
                ],
                "to": "dd942fd294b0812fa037e21a5a5abc86",
                "from": "rohit",
                "created": "2017-10-09T07:11:48.077Z",
                "createdBy": "Rohit Vyavahare",
                "order_id": 17
            }
        }
    }
]
```

