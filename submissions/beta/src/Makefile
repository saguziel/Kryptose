JAVAC=javac
JAR=jar
JAVA=java

CLIENT_SRC=client/org/kryptose/client/*.java common/org/kryptose/*/*.java
SERVER_SRC=server/org/kryptose/server/*.java common/org/kryptose/*/*.java

CLIENT_BIN=bin/client
SERVER_BIN=bin/server

CLIENT_MANIFEST=clientmanifest
SERVER_MANIFEST=servermanifest

CLIENT_JAR=kryptose-client.jar
SERVER_JAR=kryptose-server.jar

compile-client:
	mkdir -p $(CLIENT_BIN)
	$(JAVAC) -d $(CLIENT_BIN) $(CLIENT_SRC)

compile-server:
	mkdir -p $(SERVER_BIN)
	$(JAVAC) -d $(SERVER_BIN) $(SERVER_SRC)

compile: compile-client compile-server

jar-client: compile-client
	$(JAR) cfm $(CLIENT_JAR) $(CLIENT_MANIFEST) -C $(CLIENT_BIN) .

jar-server: compile-server
	$(JAR) cfm $(SERVER_JAR) $(SERVER_MANIFEST) -C $(SERVER_BIN) .

jar: jar-client jar-server

run-client: jar-client
	$(JAVA) -jar $(CLIENT_JAR)

run-server: jar-server
	$(JAVA) -jar $(SERVER_JAR)

clean-client:
	rm -f $(CLIENT_JAR)
	rm -rf $(CLIENT_BIN)

clean-server:
	rm -f $(SERVER_JAR)
	rm -rf $(SERVER_BIN)

clean: clean-client clean-server
