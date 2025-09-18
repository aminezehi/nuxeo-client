connect('weblogic','weblogic123','t3://localhost:7001')
edit()
startEdit()
# Create ms1 if not exists
cd('/Servers')
try:
  cmo.lookupServer('ms1')
  print 'ms1 exists'
except:
  cmo.createServer('ms1')
  print 'ms1 created'
cd('/Servers/ms1')
set('ListenAddress','localhost')
set('ListenPort',7003)
save()
activate(block='true')
# Retarget app nuxeo-api to ms1
edit()
startEdit()
try:
  unassign('AppDeployment','nuxeo-api','Target','AdminServer')
except:
  pass
assign('AppDeployment','nuxeo-api','Target','ms1')
save()
activate(block='true')
# Show targets
domainConfig()
cd('/AppDeployments/nuxeo-api/Targets')
ls()
exit()
