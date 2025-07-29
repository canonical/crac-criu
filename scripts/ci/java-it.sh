#!/bin/bash

cd ../.. || exit 1

failures=""

docker build -t java-it:latest -f scripts/build/Dockerfile.ubuntu25.04-java-test .
if ! docker run --rm --privileged  -v ./logs:/crac-criu/test/integration-tests-java/logs java-it:latest; then
	echo "Tests failed."
	exit 1
fi
