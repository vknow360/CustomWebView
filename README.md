# MIT App Inventor Extensions

This repository is a template repository that you can use for building
an App Inventor extension separate from the App Inventor source
repository. It is used primarily for our system to create
TensorFlow.js-based extensions.

To get started, place the sources for your extension under the `src/`
directory. All of the standard annotations App Inventor provides are
available to you. Any libraries you need should be placed under
`lib/deps/`.

## Prerequisites

You will need:

* java 1.8 (either OpenJDK or Oracle)
  * Do not use Java 8 or newer features as our build system does not yet support this.
* ant 1.10 or higher
