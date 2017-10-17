# Search Org Endpoints

```
GET /search/org/orgNameOrTag?type=tag/name
Header Authorization : Id 
Id is you got when you create an account
```
Description

```
orgNameOrTag is the string against which search is made, if we put that as 'roh' and type as name then 
it will give you following response. 
Backend will search all org names mathcing that string
```


Response

```
[
    {
        "name": "Rohit second org",
        "tag": "secondroh",
        "branch": "abc",
        "department": "fgjyu",
        "address": "1234 main st",
        "country": "India",
        "state": "Maharashtra",
        "city": "pune",
        "zip": "4578698",
        "org_pic": "",
        "pair": [
            {
                "id": "19ace92763157e782af9fb0973aa2861",
                "tag": "lion",
                "name": "Lion",
                "status": "accept"
            }
        ],
        "items": [
            {
                "item": "test",
                "brand": "abch"
            }
        ],
        "id": "2668bc53a348d1535ec3b76857dbb4b9"
    },
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
        "pair": [
            {
                "id": "dd942fd294b0812fa037e21a5a5abc86",
                "tag": "mysha, dudu",
                "name": "simis",
                "status": "accept"
            },
            {
                "id": "19ace92763157e782af9fb0973aa2861",
                "tag": "lion",
                "name": "Lion",
                "status": "accept"
            },
            {
                "id": "71cc06286db14714b6ee34ab52441644",
                "tag": "RAKS",
                "name": "RAK Steels",
                "status": "accept"
            }
        ],
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
        "id": "ae4484c9359f5a8b3d62d0a55ec52f34"
    }
]
```
