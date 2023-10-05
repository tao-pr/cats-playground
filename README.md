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

#### CombineJsonRunner

Streams (fs2) muliple JSON files, parses and combine them.

> Using 
> - fs2 (Files, Stream)
> - Monoid

#### CsvToJsonRunner

Streams (fs2) multiple CSV files, parses them and write into multiple JSON files.

> Using 
> - fs2 (Files, Stream)
> - Monoid

#### EvalRunner

Runs Evals as Fibers.

> Using 
> - Eval
> - Fiber (join)
> - OptionT

#### ForkRunner

Forks multiple Fibers, with artificial wait time, and shares atomic state across all of them.

> Using
> - Ref
> - Temporal
> - Fiber

#### GenerateCsvRunner

Generates multiple CSV files and simulates errors.

> Using
> - Parallel (parTraverse)


#### PiMCRunner

Simulates Pi from Monte-Carlo method concurrently.

> Using
> - Parallel (parTraverse)

#### RaceRunner

Generates racing conditions of multiple threads which can also throw exceptions.

> Using
> - Fiber
> - ApplicativeError (via F.raiseError)
> - Temporal
> - Concurrent (racePair)

#### SemaphoreRunner

Resource capacity limit - This runner runs concurrent tasks with resource access restriction.

> Using
> - ???


## Licence

MIT