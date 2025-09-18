connect('weblogic','weblogic123','t3://localhost:7001')
edit()
startEdit()
# Retirer AdminServer si présent
try:
  unassign('AppDeployment','nuxeo-api','Target','AdminServer')
except:
  pass
# Assigner ms1
assign('AppDeployment','nuxeo-api','Target','ms1')
save()
activate(block='true')
# Vérifier les cibles
domainConfig()
cd('/AppDeployments/nuxeo-api/Targets')
ls()
exit()
