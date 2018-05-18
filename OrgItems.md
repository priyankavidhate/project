# Org Itmes Endpoints


### Get Org Itmes

```
GET  /org/:orgId/OrganizationItems
Header Authorization : Id 
Id is you got when you create account
orgId is Organization Id
```

Response

```
[
    {
        "item": "test",
        "brand": "abch"
    }
]
```


### Create Org Itmes

```
POST /org/:orgId/OrganizationItems
Header Authorization : Id 
Id is you got when you create an account
orgId is Organization Id
```

Body 

```
```
[
    {
        "item": "test",
        "brand": "abch"
    }
]
```
