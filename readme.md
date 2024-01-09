# Palantir

``./plntr`` - a CLI tool for GitLab. It allows to:
* Get information from GitLab via CLI
* Create reports about projects activities

# How to build

- Clone repository ``git clone https://github.com/e13mort/palantir.git``
- Run ``./gradlew installDist`` command
- Add ``<cloned-dir>/client/cli/build/install/plntr/bin`` to your shell config
- Run ``./plntr`` command

# Desktop App

Palantir desktop application is under construction

## Run

```shell
./gradlew :client:desktop:run
```

# Configuration

## Properties

After the first launch palantir will create the working directory at  ``~/.plntr``.

There are two ways to configure the tool: via environment variables and via config file.

Available configuration properties:

- `GITLAB_URL` Url to your Gitlab instance. E.g. `gitlab.com` 
- `GITLAB_KEY` Access token for api requests
- `PERIOD_DATE_FORMAT` format for dates arguments. Default is `dd-MM-yyyy`
- `SYNC_PERIOD_MONTHS` period for synchronization
- `PERCENTILES_IN_REPORTS` target percentiles for reports. 
  Values from the enum [ReportsRepository.Percentile](model/src/commonMain/kotlin/com/e13mort/palantir/model/ReportsModel.kt)
  divided by `,` (e.g. `P50,P90`)

## Config file

All properties are stored in file at `<plntr-work-dir>/settings.properties`. The file is created automatically with default values. 

## Env

Also you can specify properties via env vars. All property names should start with `PALANTIR_`, e.g. `PALANTIR_GITLAB_URL`.
Properties specified via env vars has a higher priority over the same properties in `settings.properties` file.

# Usage

## Start

You might start with the following commands:

1. Get all available remote projects: ``./plntr scan projects`` 
2. Print local projects with ids ``./plntr print projects``
3. Download a specific project to a local index ``./plntr scan project <project-id>``
4. Work with local projects with `print`, `report` and `sync` commands

## Commands

### `scan` & `sync`

``./plntr scan projects`` - add projects to local index

``./plntr sync active`` - sync all active projects information

``./plntr sync project <project-id>`` - sync specified project information

### `print`

Commands to show some project related information 

``./plntr print projects`` - print projects from local index

``./plntr print projects -a`` - print projects from local index with clone urls (https & git)

``./plntr print project <project-id>`` - print project's summary

``./plntr print branches <project-id>`` - print project branches

``./plntr print mrs <project-id>`` - print project merge requests

``./plntr print mr <merge-request-id>`` - print merge requests description

### `report`

Commands to print reports for a synced project

``./plntr report`` - print all available reports

#### `approves`

'Approves' based reports

``./plntr report approves`` - print approves statistics variants

``./plntr report approves total <project-id>`` - print approves statistics by total approves amount

``./plntr report approves first <project-id>`` - print approves statistics by first approve event

#### `mr`

'Merge request' based reports 

``./plntr report mr start <project-id> --ranges 1-1-1970:1-2-2021:1-3-2021`` - print merge requests statistics by first approve or start discussion event

### `remove`

Remove items from local index

#### `project`

``./plntr remove project <project-id>`` - remove all project info from the index