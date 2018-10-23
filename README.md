# Encrypted-chat-app
This a group chat application which implements RSA and AES encryption. RSA is used to exchange an AES key between the sever and a client. After which all messages are encrytped using AES.

The server can be started from the Server.jar file with an IP of the device and port as arguments e.g. java -jar Server.jar 127.0.0.1 50002

Clients can be started from the ChatApp.jar file with the IP of the server and its port e.g. java -jar ChatApp.jar 127.0.0.1 50002

127.0.0.1 is an IP address that points to itself, another way of writing local host.

The server is multi thread and supports many users connecting to it.
