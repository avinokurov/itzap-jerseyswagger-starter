# itzap-jerseyswagger
**itzap-jerseyswagger** library is designed to provide swagger UI endpoint to the Jersey REST API application. 
## Usage
Include **itzap-jerseyswagger** dependency to your project.
```xml
<dependency>
    <groupId>com.itzap</groupId>
    <artifactId>itzap-jerseyswagger</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
``` 
add the following properties to the application config file
```properties
swagger.package="com.itzap"
swagger.apiBaseUrl="http://${email.host}/v1/itzap/message/"
```
The following code will add swaggerUI artifacts and context
```java
SwaggerContext.addSwaggerServlet(tomcat, context,
        ConfigBuilder.builder(ConfigType.TYPE_SAFE)
                .build()
                .getConfig("swagger"),
        EmailApplication.class);
```
### How To Build
* Clone the following projects: 
	* `git clone git@github.com:avinokurov/itzap-parent.git`
	* `git clone git@github.com:avinokurov/itzap-common.git`
* Build all projects
	* `cd itzap-parent && mvn clean install`
	* `cd ../itzap-common && mvn clean install`
