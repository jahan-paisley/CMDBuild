#!/bin/bash

function compress {
	grep -E 'cmdbuild/.*.js' ../../$1 | \
		sed 's/.*src=\"javascripts\/cmdbuild\/\(.*\)\".*/\1/' >/tmp/cmdbuild-list

	rm -f $2; for i in `cat /tmp/cmdbuild-list`; do cat $i >>$2; echo -e "\n\n" >>$2; done
}

compress coreJsFiles.jsp cmdbuild-core.js
compress managementJsFiles.jsp cmdbuild-management.js
compress administrationJsFiles.jsp cmdbuild-administration.js
