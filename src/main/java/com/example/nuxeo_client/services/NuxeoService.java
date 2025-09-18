package com.example.nuxeo_client.services;

import com.example.nuxeo_client.model.NuxeoDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NuxeoService {

    @Value("${nuxeo.api.url}")
    private String nuxeoApiUrl;

    @Value("${nuxeo.upload.path}")
    private String nuxeoUploadPath;

    @Value("${nuxeo.username}")
    private String username;

    @Value("${nuxeo.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getPrettyJson() throws Exception {
        String url = nuxeoApiUrl + nuxeoUploadPath + "/@children";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        Object json = objectMapper.readValue(response.getBody(), Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    }

    public List<NuxeoDocument> getParsedDocuments() throws Exception {
        String url = nuxeoApiUrl + nuxeoUploadPath + "/@children";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode entries = root.path("entries");

        List<NuxeoDocument> docs = new ArrayList<>();
        for (JsonNode node : entries) {
            NuxeoDocument doc = new NuxeoDocument();
            doc.setUid(node.path("uid").asText());
            doc.setTitle(node.path("title").asText());
            doc.setPath(node.path("path").asText());
            doc.setType(node.path("type").asText());
            docs.add(doc);
        }

        return docs;
    }

    public byte[] downloadFile(String uid) throws Exception {
        String fileUrl = nuxeoApiUrl + "/id/" + uid + "/@blob/file:content";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, byte[].class);

        return response.getBody();
    }

    public List<NuxeoDocument> findDocumentByName(String name) throws Exception {
        List<NuxeoDocument> documents = getParsedDocuments();
        List<NuxeoDocument> matchingDocuments = new ArrayList<>();
        for (NuxeoDocument doc : documents) {
            if (doc.getTitle().equals(name) || doc.getPath().endsWith("/" + name)) {
                matchingDocuments.add(doc);
            }
        }
        if (matchingDocuments.isEmpty()) {
            throw new RuntimeException("Aucun document avec le nom " + name + " non trouvé.");
        }
        return matchingDocuments;
    }

    public Map<String, Object> downloadFileByName(String uid, String ignoredFileName) throws Exception {
        // Récupérer les métadonnées du document pour obtenir le type MIME et un nom de fichier
        String docUrl = nuxeoApiUrl + "/id/" + uid;
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        var docResponse = restTemplate.exchange(docUrl, HttpMethod.GET, entity, String.class);
        JsonNode docJson = objectMapper.readTree(docResponse.getBody());
        String mimeType = docJson.path("properties").path("file:content").path("mime-type").asText("application/octet-stream");
        String derivedFileName = docJson.path("properties").path("file:content").path("name").asText(
                docJson.path("title").asText("download")
        );

        // Télécharger le contenu du fichier
        byte[] fileContent = downloadFile(uid);

        Map<String, Object> result = new HashMap<>();
        result.put("fileContent", fileContent);
        result.put("mimeType", mimeType);
        result.put("fileName", derivedFileName);

        return result;
    }

    public String uploadFileToNuxeo(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide.");
        }

        String batchInitUrl = nuxeoApiUrl + "/upload";
        HttpHeaders initHeaders = new HttpHeaders();
        initHeaders.setBasicAuth(username, password);
        initHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> initRequest = new HttpEntity<>(new byte[0], initHeaders);
        var initResponse = restTemplate.postForEntity(batchInitUrl, initRequest, String.class);

        if (initResponse.getStatusCode().is2xxSuccessful()) {
            JsonNode initJson = objectMapper.readTree(initResponse.getBody());
            String batchId = initJson.path("batchId").asText();

            String uploadUrl = batchInitUrl + "/" + batchId + "/0";
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setBasicAuth(username, password);
            uploadHeaders.set("X-File-Name", file.getOriginalFilename());
            uploadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> uploadRequest = new HttpEntity<>(file.getBytes(), uploadHeaders);
            var uploadResponse = restTemplate.postForEntity(uploadUrl, uploadRequest, String.class);

            if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec de l'upload du fichier dans le batch.");
            }

            String safeName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\-_.]", "_");
            String docJson = """
                {
                  "entity-type": "document",
                  "name": "%s",
                  "type": "File",
                  "properties": {
                    "dc:title": "%s",
                    "file:content": {
                      "upload-batch": "%s",
                      "upload-fileId": "0"
                    }
                  }
                }
                """.formatted(safeName, file.getOriginalFilename(), batchId);

            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setBasicAuth(username, password);
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            String createDocUrl = nuxeoApiUrl + nuxeoUploadPath;
            HttpEntity<String> docRequest = new HttpEntity<>(docJson, jsonHeaders);
            var docResponse = restTemplate.postForEntity(createDocUrl, docRequest, String.class);

            JsonNode docJsonResponse = objectMapper.readTree(docResponse.getBody());
            return docJsonResponse.path("uid").asText();
        } else {
            throw new RuntimeException("Échec de l'initialisation du batch.");
        }
    }

    public String updateDocumentByName(String uid, String newTitle, MultipartFile file) throws Exception {
        // Log pour débogage
        System.out.println("Mise à jour du document avec ID: " + uid);
        System.out.println("Nouveau titre: " + newTitle);
        System.out.println("Fichier fourni: " + (file != null ? file.getOriginalFilename() : "aucun"));

        String batchId = null;
        if (file != null && !file.isEmpty()) {
            // Initialiser un batch pour uploader le nouveau fichier
            String batchInitUrl = nuxeoApiUrl + "/upload";
            HttpHeaders initHeaders = new HttpHeaders();
            initHeaders.setBasicAuth(username, password);
            initHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> initRequest = new HttpEntity<>(new byte[0], initHeaders);
            var initResponse = restTemplate.postForEntity(batchInitUrl, initRequest, String.class);

            if (!initResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec de l'initialisation du batch pour la mise à jour.");
            }

            JsonNode initJson = objectMapper.readTree(initResponse.getBody());
            batchId = initJson.path("batchId").asText();

            String uploadUrl = batchInitUrl + "/" + batchId + "/0";
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setBasicAuth(username, password);
            uploadHeaders.set("X-File-Name", file.getOriginalFilename());
            uploadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> uploadRequest = new HttpEntity<>(file.getBytes(), uploadHeaders);
            var uploadResponse = restTemplate.postForEntity(uploadUrl, uploadRequest, String.class);

            if (!uploadResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Échec de l'upload du fichier dans le batch.");
            }
        }

        // Construire l'objet JSON pour la mise à jour
        Map<String, Object> properties = new HashMap<>();
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            properties.put("dc:title", newTitle.replace("\"", "\\\""));
        }
        if (batchId != null) {
            Map<String, String> fileContent = new HashMap<>();
            fileContent.put("upload-batch", batchId);
            fileContent.put("upload-fileId", "0");
            properties.put("file:content", fileContent);
        }

        Map<String, Object> entity = new HashMap<>();
        entity.put("entity-type", "document");
        entity.put("uid", uid);
        entity.put("properties", properties);

        // Sérialiser l'objet en JSON
        String docJson = objectMapper.writeValueAsString(entity);
        System.out.println("Corps JSON envoyé: " + docJson);

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setBasicAuth(username, password);
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        String updateDocUrl = nuxeoApiUrl + "/id/" + uid;
        HttpEntity<String> docRequest = new HttpEntity<>(docJson, jsonHeaders);
        var docResponse = restTemplate.exchange(updateDocUrl, HttpMethod.PUT, docRequest, String.class);

        if (docResponse.getStatusCode().is2xxSuccessful()) {
            return uid;
        } else {
            throw new RuntimeException(docResponse.getStatusCode() + " on PUT request for \"" + updateDocUrl + "\": \"" + docResponse.getBody() + "\"");
        }
    }

    public void deleteDocumentByName(String uid) throws Exception {
        String deleteDocUrl = nuxeoApiUrl + "/id/" + uid;
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        var response = restTemplate.exchange(deleteDocUrl, HttpMethod.DELETE, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Échec de la suppression du document avec l'UID " + uid);
        }
    }
}