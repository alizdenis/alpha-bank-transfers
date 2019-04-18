# alpha bank transfers

## Motivation

To write simplified version of money transfer application using non blocking way, utilizing Vert.x and RxJava

### Tech stack
* Java8
* Gradle
* Vert.x
* RxJava
* Guice
* Lombok 
* REST-assured

### How to run
To launch your tests:
```
./gradlew clean test
```

To package your application:
```
./gradlew clean assemble
```

To run your application:
```
./gradlew clean run
```

To run assembled fat jar
```
java -jar build/libs/alpha-1.0.0-fat.jar 
```
Application runs on 8080 port

### API reference

#### Account
Get all accounts
```
GET localhost:8080/api/account
```
Get account by id
```
GET localhost:8080/api/account/1
```
Create account
```
POST localhost:8080/api/account
{
  "balance" : 100
}
```
Update account
```
PUT localhost:8080/api/account/1
{
  "balance" : 100
}
```
#### Transfer
Creates transfer and process it async
```
POST localhost:8080/api/transfer/async
{
  "sourceId": 1,
  "destinationId": 2,
  "amount": 100
}
```
Creates transfer and process it in sync (client waits until source & destination accounts are processed)
```
POST localhost:8080/api/transfer/sync
{
  "sourceId": 1,
  "destinationId": 2,
  "amount": 100
}
```
