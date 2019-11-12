# Contributing to sentry-android

We love pull requests from everyone.

The project currently requires you run JDK version `1.8.x`.

To install spotlessCheck pre-commit hook:

```shell
git config core.hooksPath hooks/
```

To run the build and tests:

```shell
./gradlew build
```

To publish it:
```shell
./gradlew clean build bintrayUpload -PbintrayUser={userid_bintray} -PbintrayKey={apikey_bintray} -PdryRun=false
```

Build and tests are automatically run against branches and pull requests
via TravisCI and AppVeyor.
