Launcher
========
This project is for executing a truffle-arrow script from your terminal.

How to run
==========
```shell
java(graal vm) -Dtruffle.class.path.append="/path/to/truffle-arrow-language.jar" -cp /path/to/dir:/path/to/truffle-arrow-language-launcher.jar net.wrap_trap.truffle_arrow.language.launcher.TruffleArrowLanguageLauncher ${*.ta} 
```

- Example
```shell
/opt/graalvm-ce-java8-21.0.0.2/bin/java -Dtruffle.class.path.append="./truffle-arrow-language.jar" -cp .:./truffle-arrow-language-launcher.jar net.wrap_trap.truffle_arrow.language.launcher.TruffleArrowLanguageLauncher test3.ta
```

Referenced
==========
- https://github.com/graalvm/simplelanguage/tree/master/launcher