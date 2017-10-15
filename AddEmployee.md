# Add Employee Endpoints

### Add Employee

```
POST /add/employee
```

Body

```
{
	"org_id" :"19ace92763157e782af9fb0973aa2861",
	"phone_number": "16194887274",
	"org_name": "Lion",
	"role": "manager",
	"band" : "2"	
}
```

Description 

```
Right now we support 1,2 and 3 as band number. 
1 is creater/owner of the organization
2 has all abilities as 1 but 1 has to invite 2. 
3 has limited functionalities to organization. He can not place order and he can see only orders after they are processed. 
Example of 3 can be delivery guy. This Band concept still in very early stage and we are not going to explote this soon.

Right now we have trail version of sms service that we are using it to send an invitation. 
So, Person needs to be registered to our app to recieve an invitation. 
```

Response 

```
Status Code - 200 : Sucess, employee added.
Status Code - 500 : Failure, Something went wrong either with our backend or sms service is down. 
```
