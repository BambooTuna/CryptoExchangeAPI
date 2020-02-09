# CryptoExchangeAPI

## Manual deploy
1. Change version
    `version := "1.0.0-SNAPSHOT"`

2. Build jar file
```bash
$ sbt publishLocal
```

3. Push to repository(master branch)


## Sample

- dependencies
```
resolvers += "Maven Repo on github" at "https://BambooTuna.github.com/CryptoExchangeAPI"
"com.github.BambooTuna" %% "cryptoexchangeapi" % "1.0.0-SNAPSHOT"
```
[Usage](https://github.com/BambooTuna/CryptoExchangeAPI/blob/master/boot/src/main/scala/com/github/BambooTuna/CryptoExchangeAPI/Main.scala)
