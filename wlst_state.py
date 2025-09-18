connect('weblogic','weblogic123','t3://localhost:7001')
domainRuntime()
try:
  deploymentState('nuxeo-client-app','AdminServer')
except:
  print('deploymentState failed')
exit()
