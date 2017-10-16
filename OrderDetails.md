#Order Details Endpoints


```
PUT /order/:orderId?message=true/false
Header Authorization : Id 
Id is you got when you create an account
```

Body

```
{
	"to" :{
		
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
    "id" :"dd942fd294b0812fa037e21a5a5abc86"
		
	},
	
	"from" :{
		
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
    "id": "ae4484c9359f5a8b3d62d0a55ec52f34"
		
	},
	
	"order" :{
		
   "shipment": [
      {
        "item": "test",
        "quantity": "12"
      }
    ],
    "status": "shipping",
    "to": "dd942fd294b0812fa037e21a5a5abc86",
    "from": "ae4484c9359f5a8b3d62d0a55ec52f34",
    "created": "2017-02-20T04:57:08.398Z",
    "order_id": 8,
    "id": "ded2737bfe31fff3fe5c7c689b8f2d79"
		
	}
}
```

```
Status Code - 200 : Sucess, Order Created
Status Code - 500 : Failure, Something went wrong with our backend. 
```
