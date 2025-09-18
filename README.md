# nuxeo-client

Spring Boot app deployed to WebLogic with context root /nuxeo-api.

## Prerequisites
- JDK 17
- Maven 3.8+
- WebLogic (context-root controlled by src/main/webapp/WEB-INF/weblogic.xml)

## Build
`powershell
mvn clean package -DskipTests
`
- WAR output: 	arget2/nuxeo-client-0.0.1-SNAPSHOT.war

## Deploy on WebLogic
- Console: Deploy the WAR and keep context root /nuxeo-api.
- Or autodeploy: copy WAR to <DOMAIN_HOME>\autodeploy\.

## Application URLs (after deploy)
- Dashboard (Thymeleaf): http://localhost:7001/nuxeo-api/ or /nuxeo-api/nuxeo
- Upload: /nuxeo-api/nuxeo/upload
- List: /nuxeo-api/nuxeo/list
- Retrieve: /nuxeo-api/nuxeo/retrieve
- Update: /nuxeo-api/nuxeo/update
- Delete: /nuxeo-api/nuxeo/delete (sélection si doublons) ou /nuxeo-api/nuxeo/delete/select (par UID)

## Notes
- Les vues Thymeleaf utilisent 	h:href/	h:action pour respecter le context path.
- weblogic.xml force le contexte /nuxeo-api.

## CI
- GitHub Actions: .github/workflows/build.yml build le WAR sur chaque push PR.
