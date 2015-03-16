#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
RETRIES=1

if [[ "$1" == "--travis" ]]; then
	RETRIES=3
fi

function build() {
	cd "$DIR/../prefetch-simulation"
	# -P ci to activate release-like things like javadoc generation (and warnings)
	mvn -P ci clean verify
}



try=0

while [ $try -lt $RETRIES ]; do
	build && break
	((try++))
done

[ $try -eq $RETRIES ] && exit 1
exit 0
