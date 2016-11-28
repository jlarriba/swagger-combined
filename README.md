# swagger-combined
A Java application that is able to serve an endpoint to expose the combination 
of different swagger.json endpoints in one combined endpoint.

## usage
You need to declare a swagger-combined.properties file and put it in the same directory
as the jar.

An example swagger-combined.properties:

``` 
 swagger.combined.urls=http://petstore.swagger.io/v2/swagger.json,http://<my-own-api>/v1/swagger.json
 
 swagger.combined.title=Combined Example API
 swagger.combined.version=1.0
```
 
 With the properties created, just launch the shaded jar:
 
 ```
  java -jar swagger-combined.jar 
 ```
 
 It will start a server in 0.0.0.0:8080 and serve the combined json.