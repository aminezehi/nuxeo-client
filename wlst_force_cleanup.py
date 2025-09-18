connect('weblogic','weblogic123','t3://localhost:7001')
domainConfig()
edit()
startEdit()
try:
  stopApplication('nuxeo-client')
except:
  pass
try:
  undeploy('nuxeo-client')
except:
  pass
try:
  stopApplication('nuxeo-api')
except:
  pass
try:
  undeploy('nuxeo-api')
except:
  pass
save()
activate(block='true')
print("Cleanup completed")
exit()







