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
Status Code - 500 : Failure, Something went wrong either with our backend is down or sms service is down or VerificationCode is wrong. 
```

```
POST /onboard
```

Body

```
{
  "name": "abcd",
  "phone_number": "efgh",
  "confirm": true,
  "profile_pic": url (optional parameter)
}
```

Response 

```
If account is already exist and not active for more than 45 then create new account with phone number
If account is already exist and active within last 45 days then update existing account with new phone number
Account does not exist create new account.
Status Code - 200 : Sucess, account is created and gives back id which is stored in app and used for further communication with backend.
Status Code - 500 : Failure, Something went wrong either with our backend is down. 
```




