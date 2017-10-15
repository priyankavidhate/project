# Edit Org Endpoints

```
Put /org/:id
Header Authorization : Id 
Id is you got when you create an account
```

Body

```
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
    "org_pic": ""
}
```

Description

```
Required Parameters : name,tag,address,country,state,city,zip
Optional Parameter : branch,address,org_pic
```

```
Status Code - 200 : Sucess, Org Created
Status Code - 500 : Failure, Something went wrong with our backend. 
```
