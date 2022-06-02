# Read Me First
Simple version manager.
Maps large amount of different version microservices to single environment version (systemVersion)

## API
Update version of single service
```
POST /deploy HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Content-Length: 46

{
"name": {service_name},
"version": {service_version}
}
```
Get services versions by systemVersion
```
GET /services?systemVersion=14 HTTP/1.1
Host: localhost:8080
```
## Setup
Don't forget to setup persistent mode for Redis.
## TODO:
1. test for redis storage
2. health checks
3. swagger