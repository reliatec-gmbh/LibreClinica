# Using Containers for Development

It is possible to locally run a LibreClinica instance for development using [Docker Compose].

## Requirements

- [Docker] or [Podman]
- [Docker Compose]

## Preparation
For the docker setup to work the name of the WAR file needs to be stable. To achieve this, add 
`<finalName>${project.artifactId}</finalName>` to web/pom.xml (see pull request #394, commit 99ea319).

## Starting LibreClinica

Running the following command will start a LibreClinica instance:

```sh
docker compose up --build
```

If you use a `docker compose` version >= 2.25.0, you can use the [watch](https://docs.docker.com/compose/file-watch/) feature. This will rebuild and restart the container when you change the LibreClinica source or configuration files.

```sh
docker compose up --build --watch
```

When the container has been started, LibreClinica will be available at <http://localhost:8080>.

## Emails

LibreClinica is configured so that it does not actually send emails to the recipients. Instead, the emails are accessible at <http://localhost:1080>.

[Docker]: https://docs.docker.com/
[Docker Compose]: https://docs.docker.com/compose/
[Podman]: https://podman.io/
