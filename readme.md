# Palantir

``./plntr`` - a CLI tool for GitLab. It allows to:
* Get information from GitLab via CLI
* Create reports about projects activities

# How to build

- Clone repository ``git clone https://github.com/e13mort/palantir.git``
- Run ``./gradlew assembleDist`` command
- Add ``<cloned-dir>/cli/build/install/plntr/bin`` to your shell config
- Run ``./plntr`` command

# Configuration

## Properties

After the first launch palantir will create the working directory at  ``~/.plntr``.

There are two ways to configure the tool: via environment variables and via config file.

Available configuration properties:

- `GITLAB_URL` Url to your Gitlab instance. E.g. `gitlab.com` 
- `GITLAB_KEY` Access token for api requests
- `PERIOD_DATE_FORMAT` format for dates arguments. Default is `dd-MM-yyyy`
- `SYNC_PERIOD_MONTHS` period for synchronization

## Config file

All properties are stored in file at `<plntr-work-dir>/settings.properties`. The file is created automatically with default values. 

## Env

Also you can specify properties via env vars. All property names should start with `PALANTIR_`, e.g. `PALANTIR_GITLAB_URL`.
Properties specified via env vars has a higher priority over the same properties in `settings.properties` file.

# Usage

``./plntr scan projects`` - add projects to local index

``./plntr scan project <project-id>`` - add all project's information to index

``./plntr print projects`` - print projects from local index

``./plntr print project <project-id>`` - print project's summary

``./plntr print branches <project-id>`` - print project branches

``./plntr print mrs <project-id>`` - print project merge requests

``./plntr print mr <merge-request-id>`` - print merge requests description

``./plntr sync`` - update synced projects

``./plntr report`` - print all available reports

``./plntr report approves`` - print approves statistics variants

``./plntr report approves total <project-id>`` - print approves statistics by total approves amount

``./plntr report approves first <project-id>`` - print approves statistics by first approve event

``./plntr report mr start <project-id> --from 1-1-1970 --until 1-2-2021`` - print merge requests statistics by first approve or start discussion event
