package com.example.nuxeo_client.controller;

import com.example.nuxeo_client.model.NuxeoDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/nuxeo")
public class NuxeoViewController {

    @GetMapping("/documents")
    public List<NuxeoDocument> getDocuments() {
        String url = "http://localhost:9090/nuxeo/api/v1/path/default-domain/workspaces/workspace01/@children";
        RestTemplate restTemplate = new RestTemplate();
        try {
            var headers = new org.springframework.http.HttpHeaders();
            headers.setBasicAuth("Administrator", "Administrator");
            var request = new org.springframework.http.HttpEntity<String>(headers);
            var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
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
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des documents : " + e.getMessage());
        }
    }
}