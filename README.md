[![Build Status](https://dev.azure.com/VizCentric/Flair%20BI/_apis/build/status/viz-centric.flair-query-language?branchName=master)](https://dev.azure.com/VizCentric/Flair%20BI/_build/latest?definitionId=3&branchName=master)
# flair query language

Contains:
- flair query language grammar
- different compiler implementations for different data sources

# Release

To perform a release you need:
* set env variables GIT_USER and GIT_PASSWORD
* run following command you need to set development version and release version:

    mvn release:clean release:prepare release:perform -DreleaseVersion=${releaseVersion} -DdevelopmentVersion=${developmentVersion}
