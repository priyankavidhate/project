# Sign Up Endpoints 

```
GET /authy/verify/:CountryCode/:PhoneNumber
```

Response 

```
Status Code - 200 : Sucess, user will recieve SMS with verification code.
Status Code - 500 : Failure, Something went wrong either with our backend is down or sms service is down. 
```

```
GET /authy/verify/code?phone_number=XXXXXXXXXX&country_code=XXX?verification_code=XXXXXX
```

Response 

```
Status Code - 200 : Sucess, user will recieve SMS with verification code.
Status Code - 500 : Failure, Something went wrong either with our backend is down or sms service is down or Code is wrong. 
```


