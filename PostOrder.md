
# Order Details Endpoints


```
POST /org/:orgId/order
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
		]
    "status": "created"
	}
}
```

```
Status Code - 200 : Sucess, Order Created. Resturns Order Id
Status Code - 500 : Failure, Something went wrong with our backend. 
```
