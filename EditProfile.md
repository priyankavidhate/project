#Edit Profile Endpoints


```
PUT /account/:id
Header Authorization : Id 
Id is you got when you create an account
```

Body

```
{
    "user_name": "Rohit Vyavahare",
    "profile_pic": "url"
}
```

```
Status Code - 200 : Sucess, Account updated
Status Code - 500 : Failure, Something went wrong with our backend. 
```
