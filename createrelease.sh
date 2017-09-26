TEMPLATE_ID='Release938977771'

curl -H 'Content-Type: application/json' -u csb:csb -X POST http://localhost:7516/api/v1/templates/Applications/${TEMPLATE_ID}/create -d '{"releaseTitle":"Testing"}'
