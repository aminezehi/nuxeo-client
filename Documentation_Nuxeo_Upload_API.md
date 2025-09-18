# Documentation de l’API d’Upload vers Nuxeo

## 1. Aperçu
Cette application Java Spring Boot permet d’uploader des fichiers de tout type (PDF, images, etc.) depuis un poste local vers un serveur Nuxeo. Elle offre une interface web simple pour sélectionner et envoyer des fichiers, qui sont stockés dans Nuxeo sous forme de documents de type `File`.

## 2. Fonctionnalités
- **Upload de fichiers** : Envoie des fichiers vers l’espace de travail Nuxeo (`default-domain/workspaces/workspace01`).
- **Récupération des documents** : Liste les documents stockés dans Nuxeo avec leurs métadonnées (UID, titre, chemin, type).
- **Téléchargement de fichiers** : Récupère le contenu d’un fichier depuis Nuxeo via son UID.
- **Interface web** : Formulaire Thymeleaf pour uploader des fichiers et afficher les résultats.

## 3. Prérequis
- **Java 17** : Doit être installé sur le serveur où l’application sera déployée. Vérifiez avec :
  ```bash
  java -version