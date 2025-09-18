connect('weblogic','weblogic123','t3://localhost:7001')
domainConfig()
edit()
startEdit()
try:
  stopApplication('nuxeo-api')
except:
  pass
try:
  undeploy('nuxeo-api')
except:
  pass
try:
  stopApplication('nuxeo-client-app')
except:
  pass
try:
  undeploy('nuxeo-client-app')
except:
  pass
try:
  stopApplication('nuxeo-client-0.0.1-SNAPSHOT')
except:
  pass
try:
  undeploy('nuxeo-client-0.0.1-SNAPSHOT')
except:
  pass
save()
activate(block='true')
exit()







