# cats-playground

An example application built from the [ce3.g8 template](https://github.com/typelevel/ce3.g8).

## Configure the application

Cats-playground consists of multiple run modes which can be configured which to run. Check out `application.conf`. The config **run-mode** defines which mode to run.

## Run application

```shell
sbt run
```

### Run modes

The project includes following playground runners

#### CombineJsonRunner

Streams (fs2) muliple JSON files, parses and combine them.

> fs2 (Files, Stream), Monoid

#### CsvToJsonRunner

Streams (fs2) multiple CSV files, parses them and write into multiple JSON files.

> fs2 (Files, Stream)

#### EvalRunner

Runs Evals as Fibers.

> Eval, Fiber (Concurrent.start, Fiber.join)

#### ForkRunner

Forks multiple Fibers, with artificial wait time, and shares atomic state across all of them.

> Ref, Temporal (sleep), Fiber (Concurrent.start)

#### GenerateCsvRunner

Generates multiple CSV files and simulates errors.

> Parallel (parTraverse)


#### PiMCRunner

Simulates Pi from Monte-Carlo method concurrently.

> Parallel (parTraverse)


## Licence

TBD