Asset Management Digital Challenge
======================================

This is a Spring Boot RESTful application which can provides following account related operations:

 * Creating a new account
 * Reading an account
 * Making a transfer between two accounts


Example request:

```
curl -i -X PUT \
   -H "Content-Type:application/json" \
   -d \
'{
  "fromAccountId":"Id-1",
  "toAccountId":"Id-2",
  "amount": "10"
}' \
 'http://localhost:18080/v1/transfer'
```


Assumptions
--------------------

* I have not solved the problem in one go. I have written some code. Then took a break, looked at it and then refactored
it. And have taken more than the recommended time of 1 hour for the problem.
* No limitations on the number of decimal places that are allowed. Ideally there should be a limit of 2 decimals at the 
boundaries of the system. Within system we don't need any decimal limits.
* When something goes wrong when transferring amount in the to account, the amount deducted from the first account is 
rolled back. But if something goes wrong during the rollback, there is no handling for that scenario except for logging.
 

Further improvements
--------------------

* When releasing to production, instead of having a concurrent HashMap there should be a database for storing accounts. 
* Using database, we can use transactions to maintain the consistency of the database and it will reduce to amount
 of code we have currently to maintain the consistency of the account data.
* Database will help in scaling horizontally as well. 
* `AccountsRepository#clearAccounts` method is used in tests only and therefore it should be removed before going to production.
* Maintain a separate log/repository of the transactions done for an account.