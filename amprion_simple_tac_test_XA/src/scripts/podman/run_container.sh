#!/bin/bash

if [[ $# -ne 1 ]]; then
	echo "You need to specify the name of the configuration file with the database connection."

else

CONFIG_FILE=$1

if [[ -f ${CONFIG_FILE} ]]; then

podman create \
	--name edge_snomed \
	--pod ukd_snomed_pod \
	edge-pfu:1.0

podman cp ${CONFIG_FILE} edge_snomed:/edge-snomed/config

podman start edge_snomed

else

echo "The argument provided '${CONFIG_FILE}' is not a file."

fi

fi

# podman run --detach --name edge_pseudonym_file_upload --pod ukd_edge_pod edge_pseudonym_file_upload

