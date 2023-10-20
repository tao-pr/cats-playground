# cats-playground

An example cats-effect application built from the [ce3.g8 template](https://github.com/typelevel/ce3.g8).

## Configure the application

Cats-playground consists of multiple run modes which can be configured which to run. Check out `application.conf`. The config **run-mode** defines which mode to run.

## Run application

```shell
sbt run
```

### Run modes

The project includes following playground runners

|Runner  | Task | Using |
|---|---|---|
|CombinedJsonRunner  | Streams (fs2) multiple JSON files, parses and combines them  | <ul><li>fs2 (Files, Stream)</li><li>Monoid</li></ul> |
|CsvToJsonRunner | Streams (fs2) multiple CSV files, parses them and writes in to multiple JSON files | <ul><li>fs2 (Files, Stream)</li><li>Monoid</li></ul> |
|EvalRunner | Runs Evals as Fibers|<ul><li>Eval</li><li>Fiber (join)</li><li>OptionT</li></ul>|
|ForkRunner | Forks multiple Fibers with artificial wait time and shares atomic state across them | <ul><li>Ref</li><li>Temporal</li><li>Fiber</li></ul>|
|GenerateCsvRunner| Generates multiple CSV files and simulates errors | <ul><li>Parallel (parTraverse)</li></ul>|
|PiMCRunner | Simulates Pi estimation from Monte-Carlo method concurrently| <ul><li>Parallel (parTraverse)</li></ul>|
|RaceRunner | Generates racing conditions of multiple threads which can also throw exceptions | <ul><li>Fiber</li><li>ApplicativeError (via F.raiseError)</li><li>Temporal</li><li>Concurrent (racePair)</li></ul> |
|SemaphoreRunner | Endlessly runs scheduler which only runs an execution when resources are available (and permitted)| <ul><li>Semaphore</li><li>Ref</li><li>Stream (awakeEvery)</li></ul>|
|AttemptRunner | Runs chains of operations which transforms data with functional error handling | <ul><li>Ior</li><li>Validated</li><li>Monoid (combineAll)</li><li>Parallel (parTraverse)</li></ul> |



## Licence

MIT