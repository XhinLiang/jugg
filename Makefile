build:
	(cd web && npm i && npm run build) && rm -rf src/main/resources/fire_public && mvn clean install -U
build-ws:
	mkdir -p target && (cd node-client && npm i && node_modules/pkg/lib-es5/bin.js -t node10-linux-x64 index.js && mv index ../target/ws-client)
run:
	java -jar target/jugg.jar
release:
	mvn clean deploy -P ossrh
