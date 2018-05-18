# Creat Org Endpoints

### Check Tag already exist

```
GET /tag/:tag
Header Authorization : Id 
Id is you got when you create an account
```

Description

```
Before create an Org, check if tag already exist. Every Org should have unique tag. They can have same name but not tag
```

Response 

```
Status Code - 200 : Sucess, tag already exist
Status Code - 404 : Sucess, tag does not exsit
Status Code - 500 : Failure, Something went wrong with our backend. 
```

### Create an Org

```
POST /org
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

Future addition

```
Add option to Org creater to add org itmes. So final body will be: 

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
    "items": [
      {
        "item": "test",
        "brand": "abch"
      }
    ]
}
```
