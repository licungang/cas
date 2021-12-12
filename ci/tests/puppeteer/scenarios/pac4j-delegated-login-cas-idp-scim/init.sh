echo Starting SCIM Server
${PWD}/ci/tests/scim/run-scim-server.sh
echo SCIM Server started

echo -e "Mapping CAS keystore to ${CAS_KEYSTORE}"
docker run -d \
  --mount type=bind,source="${CAS_KEYSTORE}",target=/etc/cas/thekeystore \
  -e SPRING_APPLICATION_JSON='{"cas": {"service-registry": {"core": {"init-from-json": true} } } }' \
  -p 8444:8443 --name casserver apereo/cas:6.4.4
clear
docker logs -f casserver &
echo -e "Waiting for CAS..."
until curl -k -L --output /dev/null --silent --fail https://localhost:8444/cas/login; do
    echo -n '.'
    sleep 1
done
echo -e "\n\nReady!"
