.PHONY: clean compile dryRelease doRelease release update stop installSample startSample runSample sleepHack runConnected

all: clean compile update dryRelease

# deep clean
clean:
	./gradlew clean cleanBuildCache

# build and run tests
compile:
	./gradlew build

# do a dry release (like a local deploy)
dryRelease:
	./gradlew bintrayUpload -PbintrayUser=dryUser -PbintrayKey=dryKey

# deploy the current build to bintray, jcenter and maven central
doRelease:
	./gradlew bintrayUpload -PbintrayUser="$(BINTRAY_USERNAME)" -PbintrayKey="$(BINTRAY_KEY)" -PmavenCentralUser="$(MAVEN_USER)" -PmavenCentralPassword="$(MAVEN_PASS)" -PmavenCentralSync=true -PdryRun=false

# deep clean, build and deploy to bintray, jcenter and maven central
release: clean compile dryRelease doRelease

# check for dependencies update
update:
	./gradlew dependencyUpdates -Drevision=release

# We stop gradle at the end to make sure the cache folders
# don't contain any lock files and are free to be cached.
stop:
	./gradlew --stop

# install and run sample on connected emulator/device
installSample:
	./gradlew installDebug

# start sample using adb start
startSample:
	adb shell am instrument -w -r -e debug false -e class 'io.sentry.sample.MainActivityTest' io.sentry.sample.test/androidx.test.runner.AndroidJUnitRunner

# hack for sleeping 10 seconds, so emulator won't get killed asap
sleepHack:
	sleep 5

# install debug mode, run with adb and wait for 10 seconds
runSample: installSample startSample sleepHack

# run connectedCheck
runConnected:
	./gradlew connectedCheck
