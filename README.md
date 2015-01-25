## How to start the REST webservice ##

1. download and extract the following TAR file into the repo directory: [download](https://kindl.io/owncloud/public.php?service=files&t=ce25da2c44a3723bab34da4ffe27d33d) -- contains the folder structure and files needed by the classifier
2. run `mvn compile exec:java -Dexec.mainClass="at.ac.tuwien.infosys.dsg.aic.ws2014.g4.t1.webservice.rest.JettyTwitterSentimentRestService"` to start the webservice
3. use GUI (gui/index.html) to test the webservice
