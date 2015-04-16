JAVAC=javac
JAR=jar
JAVA=java

JUNIT_LIB=junit-4.12.jar:hamcrest-all-1.3.jar
JUNIT_RUNNER=org.junit.runner.JUnitCore

CLIENT_SRC=client/org/kryptose/client/*.java common/org/kryptose/*/*.java
SERVER_SRC=server/org/kryptose/server/*.java common/org/kryptose/*/*.java
TEST_SRC=client/org/kryptose/client/test/*.java server/org/kryptose/server/test/*.java $(CLIENT_SRC) $(SERVER_SRC)

CLIENT_BIN=bin/client
SERVER_BIN=bin/server
TEST_BIN=bin/test

CLIENT_MANIFEST=clientmanifest
SERVER_MANIFEST=servermanifest
LOG_MANIFEST=logmanifest

CLIENT_JAR=kryptose-client.jar
SERVER_JAR=kryptose-server.jar
LOG_JAR=kryptose-log-reader.jar

compile-client:
	mkdir -p $(CLIENT_BIN)
	$(JAVAC) -d $(CLIENT_BIN) $(CLIENT_SRC)

compile-server:
	mkdir -p $(SERVER_BIN)
	$(JAVAC) -d $(SERVER_BIN) $(SERVER_SRC)

compile-test:
	mkdir -p $(TEST_BIN)
	$(JAVAC) -cp $(JUNIT_LIB):. -d $(TEST_BIN) $(TEST_SRC)

compile: compile-client compile-server

jar-client: compile-client
	$(JAR) cfm $(CLIENT_JAR) $(CLIENT_MANIFEST) -C $(CLIENT_BIN) .

jar-server: compile-server
	$(JAR) cfm $(SERVER_JAR) $(SERVER_MANIFEST) -C $(SERVER_BIN) .

jar-log: compile-server
	$(JAR) cfm $(LOG_JAR) $(LOG_MANIFEST) -C $(SERVER_BIN) .

jar: jar-client jar-server jar-log

run-client: jar-client
	$(JAVA) -jar $(CLIENT_JAR)

run-server: jar-server
	$(JAVA) -jar $(SERVER_JAR)

run-log: jar-log
	$(JAVA) -jar $(LOG_JAR)

run-test: compile-test
	$(JAVA) -cp $(JUNIT_LIB):$(TEST_BIN) $(JUNIT_RUNNER) org.kryptose.server.test.UserTableTest || true 
	$(JAVA) -cp $(JUNIT_LIB):$(TEST_BIN) $(JUNIT_RUNNER) org.kryptose.server.test.SecureServerListenerTest || true
	$(JAVA) -cp $(JUNIT_LIB):$(TEST_BIN) $(JUNIT_RUNNER) org.kryptose.server.LogFormatterTest || true
	$(JAVA) -cp $(JUNIT_LIB):$(TEST_BIN) $(JUNIT_RUNNER) org.kryptose.client.test.PasswordFileTest || true
	$(JAVA) -cp $(JUNIT_LIB):$(TEST_BIN) $(JUNIT_RUNNER) org.kryptose.client.test.KeyDerivatorTest || true
	$(JAVA) -cp $(JUNIT_LIB):$(TEST_BIN) $(JUNIT_RUNNER) org.kryptose.client.test.TestKeyDerivatorDoesNotCrash || true

clean-client:
	rm -f $(CLIENT_JAR)
	rm -rf $(CLIENT_BIN)

clean-server:
	rm -f $(SERVER_JAR)
	rm -f $(LOG_JAR)
	rm -rf $(SERVER_BIN)

clean-test:
	rm -rf $(TEST_BIN)

clean: clean-client clean-server clean-test
