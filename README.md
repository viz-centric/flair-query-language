[![Build Status](https://dev.azure.com/VizCentric/Flair%20BI/_apis/build/status/viz-centric.flair-query-language?branchName=master)](https://dev.azure.com/VizCentric/Flair%20BI/_build/latest?definitionId=3&branchName=master)
# flair query language

Contains:
- flair query language grammar
- different compiler implementations for different data sources
## Build

To build the application and install in local maven repository use

```
mvn clean install
```

## Release

To perform a release you need:
*  have configured credentials in settings.xml

    ```
    <settings>  
        <servers>  
            <server>
                <id>github-credentials</id>  
                <username>myUser</username>  
                <password>myPassword</password>  
            </server>   
        </servers>
    </settings>   
    ```
* run following command you need to set development version and release version:

   ``` 
   mvn release:clean release:prepare release:perform -DreleaseVersion=${releaseVersion} -DdevelopmentVersion=${developmentVersion}
   ```
