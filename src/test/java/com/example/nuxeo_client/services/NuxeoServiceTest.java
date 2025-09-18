package com.example.nuxeo_client.services;

import com.example.nuxeo_client.model.NuxeoDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"nuxeo.api.url=http://localhost:8080/nuxeo/api/v1"})
public class NuxeoServiceTest {

    @Autowired
    private NuxeoService nuxeoService;

    @Test
    public void testGetParsedDocuments() {
        try {
            List<NuxeoDocument> documents = nuxeoService.getParsedDocuments();

            // Vérifier que la liste n'est pas nulle
            assertNotNull(documents, "La liste des documents ne doit pas être nulle");

            // Vérifier que la liste contient au moins un élément (selon ta config Nuxeo)
            assertFalse(documents.isEmpty(), "La liste des documents ne doit pas être vide");

            // Afficher les titres dans la console pour vérification manuelle
            documents.forEach(doc -> System.out.println(doc.getTitle() + " | " + doc.getPath()));

        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for debugging
            fail("Exception levée pendant l'appel au service : " + e.getMessage());
        }
    }

    @Test
    public void testGetPrettyJson() {
        try {
            String json = nuxeoService.getPrettyJson();

            assertNotNull(json, "Le JSON formaté ne doit pas être nul");
            assertTrue(json.startsWith("{") && json.endsWith("}"), "Le JSON doit commencer par { et finir par }");

            System.out.println(json); // Affichage pour vérification

        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for debugging
            fail("Exception levée pendant l'appel au service (JSON) : " + e.getMessage());
        }
    }
}