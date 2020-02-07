#!/bin/bash
set -eux

yarn global add @zeus-ci/cli

zeus upload -t "application/zip+aar" ./*/build/outputs/aar/*release.aar
zeus upload -t "application/zip+jar" ./*/build/libs/*release.jar
