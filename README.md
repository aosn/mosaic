# Mosaic

AOSN Workshop book voting system.

Demo:

* [vote.tasktoys.com](http://vote.tasktoys.com) (works on Bluemix US-South region)

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

* Java SE Development Kit 8
* MySQL 5.6

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
sudo cp build/libs/mosaic.war /opt/mosaic/
sudo service mosaic start
```

NOTE: You need to create `application-production.yml` and systemd configuration.
See our [Wiki](https://github.com/aosn/mosaic/wiki) for more information.

### Run at Cloud Foundry

```bash
./gradlew bootRepackage
cf push
```

NOTE: You need to create `application-cloud.yml` and check [manifest.yml](manifest.yml) attributes.
See our [Wiki](https://github.com/aosn/mosaic/wiki) for more information.

## Author

[@mikan](https://github.com/mikan)

## License

Mosaic licensed under the [Appache License 2.0](LICENSE).