# Pair Org Endpoints

### Get pending pair org request

```
GET /org/pair/pending/:OrgId
OrgId : OrgId for which you want to get pair orgs for 
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

### Accept or Decline Pair org request

```
POST /org/pair/action/:OrgId
OrgId : OrgId for which you want to take action on pair orgs
Header Authorization : Id 
Id is you got when you creat account
```

Response 

```
Status Code - 200 : Sucess.
Status Code - 500 : Failure, Something went wrong either with our backend is down. 
```

### Sending Pair Org Request 

```
POST /org/pair/:OrgId
OrgId : OrgId for which you want to make request for pair orgs
Header Authorization : Id 
Id is you got when you creat account
```

Response 

```
Status Code - 200 : Sucess.
Status Code - 500 : Failure, Something went wrong either with our backend is down. 
```
