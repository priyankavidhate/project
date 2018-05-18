# Outbox Endpoints

### Get Outbox


```
GET  /org/:OrgId/outbox
Header Authorization : Id 
Id is, you got when you creat an account
And OrgId is organization id for which you want to get Inbox
```

Response

```
[
    {
        "id": "20a1a83415d0f9755caf08febd753d0e",
        "key": "dd942fd294b0812fa037e21a5a5abc86",
        "value": "20a1a83415d0f9755caf08febd753d0e",
        "doc": {
            "_id": "20a1a83415d0f9755caf08febd753d0e",
            "_rev": "3-dcc9ff4d3b705d593cb5e4e0ef576bc6",
            "value": {
                "item": "table-5",
                "quantity": 2,
                "created": "2017-02-14T06:36:32.919Z",
                "from": "dd942fd294b0812fa037e21a5a5abc86",
                "to": "rohit",
                "order_id": "HytuB7ltg",
                "status": "cancelled",
                "last_update": "2017-02-18T03:50:29.499Z"
            }
        }
    }
]
```
