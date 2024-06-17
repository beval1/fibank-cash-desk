# Fibank Cash Desk

"*You are developing a cash operations module in an internal information system. Cashiers should be able to deposit
and withdraw money in BGN and EUR. After 2 withdrawals and 2 deposits, the cashier should be able to check
his balance and denominations. The cashier starts the day with set amount in BGN and EUR in the cash desk. All
cash operations will be executed in the same working day. The user name of the cashier within the system is
MARTINA.*"

Postman Collection: https://solar-sunset-157347.postman.co/workspace/New-Team-Workspace~2257db0f-d1ad-43a5-953b-cacdbbbfa17c/collection/14739820-a8db9210-177f-4470-bc11-c2dc2e556039?action=share&creator=14739820&active-environment=14739820-0c4cf942-db38-4972-a331-ffaf63758024
* Also can be found in the repository: Fibank.postman_collection.json

## Running the program
* `cashiers.txt` - file with cashiers data (json format), must be in the same directory as executable or program won't run
* `transactions.txt` - file with transactions logs (csv format), will be automatically created in `/transactions` folder
* `java -jar target/fibank-cash-desk-0.0.1-SNAPSHOT.jar` - to run the server