connect('weblogic','weblogic123','t3://localhost:7001')
domainConfig()
cd('AppDeployments')
apps = ls()
print("Applications deployed:")
for app in apps:
    print("- " + app)
exit()







