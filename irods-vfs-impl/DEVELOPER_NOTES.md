# Developer notes

This project's unit and functional testing framework derives from the unit testing framework in [Jargon](https://github.com/DICE-UNC/jargon/blob/master/DOCKERTEST.md). 
Refer to that document, as well as the settings.xml and other artifacts laid out in that document for instructions on preparing test iRODS and build/test tools that can enhance
unit testing. Further unit testing tips are available in the [GitHub Jargon Wiki](https://github.com/DICE-UNC/jargon/wiki/Setting-up-unit-tests-for-4X).

## Steps for building and testing

### Set up a running iRODS data grid with appropriate test users and other configuration

For testing against a known or externally provisioned iRODS server, follow the test setup instructions in the [GitHub Jargon Wiki](https://github.com/DICE-UNC/jargon/wiki/Setting-up-unit-tests-for-4X). This involves:

* Installing iRODS

* Running a test configuration [script](https://github.com/DICE-UNC/jargon/blob/master/docker-test-framework/4-2/testsetup-consortium.sh) to set up a known set of users/resources

* Adding a maven profile (via the ~/.m2/settings.xml) and activating it with settings that match the pre-configured iRODS. See this [example](https://github.com/DICE-UNC/jargon/blob/master/settings.xml) and adapt to the configured iRODS server

* Run mvn install to read the provile and generate a testing.properties file in src/test/resources that will be used by unit tests

For testing against the jargon docker test framework, start the docker test framework via docker-compose and then configure settings XML to that target server, noting the default
settings.xml in the Jargon test framework appropriate to the iRODS version. These settings may be put into the local ~/.m2/settings.xml and activated as a profile, or the provided 
Docker test/build container will automatically mount and activate the correct settings.xml file

