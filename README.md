# RESTful API for money transfers between accounts

Java RESTful API for money transfer between accounts. 

## Dependencies
- JAX-RS
- Jetty with Jersey
- SLF4J + Log4J1
- H2 db

## How to run
- mvn clean package - run all tests (including integration tests) and generates executable '-shadow' jar with demo configurations. 
- http://localhost:8082/account/all - GET all accounts 
- http://localhost:8082/account/transfer - POST user transaction

```sh
java -jar money-transfer-1.0-SNAPSHOT-shaded.jar
```
It will start Jetty server with demo H2 db

### Sample JSON for User Transaction:
```sh
{  
   "fromAccountId":1,
   "toAccountId":3
   "transferAmount":10.00,
}
```
 
## Assumptions
1. No authentication
2. User, who initiated transfer owns account, from which funds are transferred
3. Service operates with existed Bank system and provides an API for transferring funds between accounts.
   * Service can only update Account balance.
   * method getAllAccounts (/account/all) was added mostly for testing purposes
4. User transaction executes only if currency of both accounts is the same. Additional currency conversion logic could be added into BalanceUpdater class
5. We do not create User transactions in DB. 

## Design
1. Bootstrap - is main class, which initializes everything by app.properties file. 
2. TransferService - main REST and JSON interface, handles BAD REQUESTs. Has 2 methods:
    * GET /account/all - returns all accounts
    * POST /account/transfer - transfer funds specified in UserTransaction
3. TransferController - main business logic:
    * Creates DaoConnection and performs commit and close
    * Validates UserTransaction object
    * Uses AccountLocker and BalanceUpdater for a transfer flow
4. DAO layer: DaoFactory, which creates needed DAO (AccountDao, DaoConnection)
5. Model: 
    * Account - DAO and REST (for testing) object
    * UserTransaction - only REST request object
6. app.properties files - specifies:
    * REST Server = JETTY
    * DAO factory = DEMO-JDBC / JDBC (with demo model or without)
    * http port for rest server. Default = 8082 
    * jdbc properties
7. log4j.properties - writes to log/money-transfer.log and to stdout
8. META-INF.services - registered services:
    * Jetty REST server
    * JDBC DAO Factory

## Tests
### Unit tests
Mostly covered with using Mockito framework to mock different parts
### Integration tests 
Ends on *ITest.java. 
1. REST server - JettyRestServerITest with mocked business logic (AccountController) 
2. TransferService logic with H2 - TransferServiceITest without mocked objects.
    * created additional multi-threaded scenarios with 100 concurrent threads


### Notes
1. Maven project code could be divided into separate modules: rest server implementation, DAO implementation and Demo.
2. ServiceLoader is used to improve unit testing and make project more generic
3. DB connection is created and closed for each transaction. Could be improved with using connection pool.
