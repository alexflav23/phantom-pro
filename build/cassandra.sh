echo "Launching Cassandra cluster with ${CASSANDRA_NODES} nodes and version ${CASSANDRA_VERSION}"

pip install --user 'requests[security]'
pip install --user ccm
ccm create test -v ${CASSANDRA_VERSION} -n ${CASSANDRA_NODES} -s
