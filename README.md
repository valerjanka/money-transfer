# RESTful API for money transfers between accounts

Java RESTful API for money transfer between accounts. 

## Assumptions
1. No authentication
2. User, who initiated transfer owns account, from which funds are transferred
3. Service operates with existed Bank system and provides an API for transferring funds between accounts.
   * Service can only update Account balance.
4. Transaction executes only if currency of both accounts is the same. Additional currency conversion logic could be added into BalanceUpdater class 