connect('weblogic','weblogic123','t3://localhost:7001')
domainConfig()
edit()
startEdit()
for name in ['nuxeo-api','nuxeo-client-app']:
  try:
    stopApplication(name)
  except:
    pass
  try:
    undeploy(name)
  except:
    pass
deploy(appName='nuxeo-api', path=r'C:\Users\MSI\nuxeo-client\nuxeo-client\target2\nuxeo-client-0.0.1-SNAPSHOT.war', targets='AdminServer', upload='true', remote='false', contextRoot='/nuxeo-api')
save()
activate(block='true')
startApplication('nuxeo-api')
domainRuntime()
cd('AppRuntimeStateRuntime/AppRuntimeStateRuntime')
print 'Intended=', cmo.getIntendedState('nuxeo-api','AdminServer')
print 'Current =', cmo.getCurrentState('nuxeo-api','AdminServer')
exit()
