@echo off
set GF=c:\glassfish3\glassfish
set JARS=dht-client.jar;lib\dht-domain.jar;lib\dht-representation.jar;lib\dht-webclient.jar;dht-nodeClient.jar;dht-routingClient.jar;dht-stateClient.jar;dht-state.jar;%GF%\lib\gf-client.jar;%GF%\modules\jersey-client.jar;%GF%\modules\jersey-core.jar
echo 'java -cp %JARS% edu.stevens.cs549.dht.client.Boot'
java -cp %JARS% edu.stevens.cs549.dht.client.Boot