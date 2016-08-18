# drools-sizing-tool



1) Edit the src/test/resources excel document to define the tests to perform. Tests include such configurations as number of rules, number of facts, number of fields in fact objects etc... and uses generators to generate random data to attain sizing info.

2) Run the tests of the project (mvn clean test) to product a .csv report in the target folder.