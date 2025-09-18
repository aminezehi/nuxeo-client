package com.example.nuxeo_client.controller;

import com.example.nuxeo_client.model.NuxeoDocument;
import com.example.nuxeo_client.services.NuxeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class NuxeoUploadController {

    @Autowired
    private NuxeoService nuxeoService;

    @GetMapping("/nuxeo")
    public String showDashboard() {
        return "dashboard";
    }

    @GetMapping("/nuxeo/upload")
    public String showForm() {
        return "upload-form";
    }

    @PostMapping("/nuxeo/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String docId = nuxeoService.uploadFileToNuxeo(file);
            model.addAttribute("message", "✅ Fichier uploadé avec succès. UID = " + docId);
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de l'upload : " + e.getMessage());
        }
        return "upload-result";
    }

    @GetMapping("/nuxeo/retrieve")
    public String showRetrieveForm(Model model) {
        try {
            model.addAttribute("documents", nuxeoService.getParsedDocuments());
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors du chargement des documents : " + e.getMessage());
        }
        return "retrieve-form";
    }

    @PostMapping("/nuxeo/retrieve")
    public String retrieveDocument(@RequestParam("name") String name, Model model) {
        try {
            List<NuxeoDocument> documents = nuxeoService.findDocumentByName(name);
            if (documents.size() > 1) {
                model.addAttribute("documents", documents);
                model.addAttribute("action", "retrieve");
                model.addAttribute("name", name);
                return "select-document";
            } else {
                NuxeoDocument document = documents.get(0);
                model.addAttribute("message", "✅ Document récupéré avec succès. Nom = " + name);
                model.addAttribute("fileContentAvailable", true);
                model.addAttribute("fileName", name);
                model.addAttribute("uid", document.getUid());
                return "retrieve-result";
            }
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la récupération : " + e.getMessage());
        }
        return "retrieve-form";
    }

    @PostMapping("/nuxeo/retrieve/select")
    public String retrieveDocumentByUid(@RequestParam("uid") String uid, @RequestParam("name") String name, Model model) {
        try {
            model.addAttribute("message", "✅ Document récupéré avec succès. Nom = " + name);
            model.addAttribute("fileContentAvailable", true);
            model.addAttribute("fileName", name);
            model.addAttribute("uid", uid);
            return "retrieve-result";
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la récupération : " + e.getMessage());
            return "retrieve-form";
        }
    }

    @GetMapping("/nuxeo/list")
    public String listDocuments(Model model) {
        try {
            var documents = nuxeoService.getParsedDocuments();
            model.addAttribute("documents", documents);
            model.addAttribute("message", "✅ Liste des documents récupérée avec succès.");
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la récupération de la liste : " + e.getMessage());
        }
        return "document-list";
    }

    @GetMapping("/nuxeo/update")
    public String showUpdateForm(Model model) {
        try {
            model.addAttribute("documents", nuxeoService.getParsedDocuments());
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors du chargement des documents : " + e.getMessage());
        }
        return "update-form";
    }

    @PostMapping("/nuxeo/update")
    public String updateDocument(@RequestParam("name") String name,
                                 @RequestParam(value = "newTitle", required = false) String newTitle,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 Model model) {
        try {
            List<NuxeoDocument> documents = nuxeoService.findDocumentByName(name);
            if (documents.size() > 1) {
                model.addAttribute("documents", documents);
                model.addAttribute("action", "update");
                model.addAttribute("name", name);
                model.addAttribute("newTitle", newTitle);
                model.addAttribute("file", file);
                return "select-document";
            } else {
                String docId = nuxeoService.updateDocumentByName(documents.get(0).getUid(), newTitle, file);
                model.addAttribute("message", "✅ Document mis à jour avec succès. Nom = " + name);
                return "update-result";
            }
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la mise à jour : " + e.getMessage());
            return "update-form";
        }
    }

    @PostMapping("/nuxeo/update/select")
    public String updateDocumentByUid(@RequestParam("uid") String uid,
                                      @RequestParam("name") String name,
                                      @RequestParam(value = "newTitle", required = false) String newTitle,
                                      @RequestParam(value = "file", required = false) MultipartFile file,
                                      Model model) {
        try {
            String docId = nuxeoService.updateDocumentByName(uid, newTitle, file);
            model.addAttribute("message", "✅ Document mis à jour avec succès. Nom = " + name);
            return "update-result";
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la mise à jour : " + e.getMessage());
            return "update-form";
        }
    }

    @GetMapping("/nuxeo/delete")
    public String showDeleteForm(Model model) {
        try {
            model.addAttribute("documents", nuxeoService.getParsedDocuments());
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors du chargement des documents : " + e.getMessage());
        }
        return "delete-form";
    }

    @PostMapping("/nuxeo/delete")
    public String deleteDocument(@RequestParam("name") String name, Model model) {
        try {
            List<NuxeoDocument> documents = nuxeoService.findDocumentByName(name);
            if (documents.size() > 1) {
                model.addAttribute("documents", documents);
                model.addAttribute("action", "delete");
                model.addAttribute("name", name);
                return "select-document";
            } else {
                nuxeoService.deleteDocumentByName(documents.get(0).getUid());
                model.addAttribute("message", "✅ Document supprimé avec succès. Nom = " + name);
                return "delete-result";
            }
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la suppression : " + e.getMessage());
            return "delete-form";
        }
    }

    @PostMapping("/nuxeo/delete/select")
    public String deleteDocumentByUid(@RequestParam("uid") String uid, @RequestParam("name") String name, Model model) {
        try {
            nuxeoService.deleteDocumentByName(uid);
            model.addAttribute("message", "✅ Document supprimé avec succès. Nom = " + name);
            return "delete-result";
        } catch (Exception e) {
            model.addAttribute("error", "❌ Erreur lors de la suppression : " + e.getMessage());
            return "delete-form";
        }
    }

    @GetMapping("/nuxeo/download/{uid}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String uid) throws Exception {
        try {
            Map<String, Object> result = nuxeoService.downloadFileByName(uid, null);
            byte[] fileContent = (byte[]) result.get("fileContent");
            String mimeType = (String) result.get("mimeType");
            String fileName = (String) result.get("fileName");

            ByteArrayResource resource = new ByteArrayResource(fileContent);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du téléchargement du fichier : " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint works!";
    }
}