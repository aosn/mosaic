# Mosaic

[![Build Status](https://travis-ci.org/aosn/mosaic.svg?branch=develop)](https://travis-ci.org/aosn/mosaic)

AOSN Workshop book voting system.

Demo:

* [vote.aosn.ws](https://vote.aosn.ws) (works on Heroku)

## Key features

* Easy to create, vote and check result
* Books can propose at GitHub Issues
* Login by GitHub OAuth2
* Poll progress is hidden except poll owner
* I18N supported both English and Japanese

### Future works

* Automatic comment & close GitHub Issues
* Poll open & close notification for Slack

## Requirements

### Client

* Any modern browsers
* GitHub.com account

### Server

* OpenJDK 11

#### Tested databases

* H2
* MySQL 5.5, 5.6

## Frameworks & Libraries

Mosaic using full-stack Java frameworks that provides SQL-free, HTML-free and JavaScript-free implementation style.

* Spring Boot
* Spring Security 
* Spring Data JPA
* Vaadin

## How to Run

### Run locally

```bash
./gradlew bootRun
```

### Run as service

```bash
./gradlew bootRepackage
sudo service mosaic stop
sudo cp build/libs/mosaic.jar /opt/mosaic/
sudo service mosaic start
```

NOTE: You need to create `application-production.yml` and systemd configuration.
See our [Wiki](https://github.com/aosn/mosaic/wiki) for more information.

### Deploy to Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/aosn/mosaic)

## Author

[@mikan](https://github.com/mikan)

## License

Mosaic licensed under the [Appache License 2.0](LICENSE).
