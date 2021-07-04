truffle-arrow-language
======================

truffle-arrow-language is a language processing for [Apache Arrow](https://arrow.apache.org) and built using Truffle for GraalVM.

Purpose
=======
truffle-arrow-language is developed as a language for a code generation by a query engine.

Sample Code
===========
This is a sample code for filter-aggregation.
```php
$map = {}; // for aggregation
$out = arrays(key:INT, value:INT); // for output
load ("target/all_fields.arrow") {
  // Allow to reference the field value (F_INT) directly in loop
  if ($F_INT < 5) { // filter
    $map[$F_INT] = get($map[$F_INT], 0) + 1; // aggregation
  }
}
store($out, $map); // storing the result as RecordBatch(VectorSchemaRoot)
return $out;
```

Referenced
==========
- [fivetran/truffle-sql: Experimental data-lake implemented using Truffle / Graal ](https://github.com/fivetran/truffle-sql)
- [kishida/phpparser](https://github.com/kishida/phpparser)
- [graalvm/simplelanguage: A simple example language built using the Truffle API.](https://github.com/graalvm/simplelanguage)