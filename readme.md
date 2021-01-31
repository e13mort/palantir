
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

``./plntr report mr start <project-id> --from 1.1.1970 --until 1.2.2021`` - print merge requests statistics by first approve or start discussion event