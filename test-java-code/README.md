For benchmarking
================
This project is for comparing to the performance between truffle-arrow-language and Java implementation. This includes a Java Implementation processing Apache Arrow data.

How to run
===========
```shell
java -cp /path/to/dir:/path/to/test-java-code.jar net.wrap_trap.truffle_arrow_language.test_java_code.GroupByRunner /path/to/${*.java} /path/to/${*.arrow} [Option]
```

- Example
```shell
java -cp .:./test-java-code.jar net.wrap_trap.truffle_arrow_language.test_java_code.GroupByRunner ./GroupBy.java ./ontime_backup.arrow 4
```