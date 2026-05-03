# Test Script

Ce guide permet de tester le projet de bout en bout avec des donnees d'exemple, sans improviser.

## Pre-requis

1. Demarrer `MySQL/MariaDB` depuis XAMPP.
2. Verifier ou adapter [db.properties](C:/Users/Mega%20pc/Desktop/java%20project/src/main/resources/db.properties).
3. Si la base n'existe pas encore, executer [schema.sql](C:/Users/Mega%20pc/Desktop/java%20project/src/main/resources/schema.sql).
4. Lancer [Main.java](C:/Users/Mega%20pc/Desktop/java%20project/src/main/java/com/library/app/Main.java) dans IntelliJ.

## Jeu de donnees recommande

- Admin
  - username: `admin1`
  - password: `admin123`
  - prenom: `Ali`
  - nom: `Admin`
  - email: `admin@test.com`
  - telephone: `12345678`
  - numero employe: `EMP001`

- Bibliothecaire
  - username: `lib1`
  - password: `lib123`
  - prenom: `Lina`
  - nom: `Bib`
  - email: `lib1@test.com`
  - telephone: `22222222`
  - numero employe: `EMP002`

- Adherent
  - username: `mem1`
  - password: `mem123`
  - prenom: `Sami`
  - nom: `Member`
  - email: `mem1@test.com`
  - telephone: `555111`
  - numero adhesion: `M001`
  - adresse: `Tunis`

- Categorie
  - nom: `Informatique`
  - description: `Livres info`

- Auteur
  - prenom: `Robert`
  - nom: `Martin`

- Livre
  - ISBN: `9780132350884`
  - titre: `Clean Code`
  - editeur: `Prentice Hall`
  - annee: `2008`

- Exemplaires
  - copy 1 barcode: `BC001`
  - copy 2 barcode: `BC002`

## Test 1 - Creation du premier administrateur

Si l'ecran affiche `Aucun utilisateur detecte. Creation du premier administrateur.` :

1. Saisir `admin1`
2. Saisir `admin123`
3. Saisir `Ali`
4. Saisir `Admin`
5. Saisir `admin@test.com`
6. Saisir `12345678`
7. Saisir `EMP001`
8. Appuyer sur `Enter` pour la date d'embauche

Resultat attendu :
- un administrateur est cree
- retour au menu principal

## Test 2 - Connexion admin

1. Choisir `1. Connexion`
2. Username: `admin1`
3. Password: `admin123`

Resultat attendu :
- ouverture du menu administrateur

## Test 3 - Fonctionnalites admin

### 3.1 Ajouter un bibliothecaire

1. Choisir `Ajouter un bibliothecaire`
2. Username: `lib1`
3. Password: `lib123`
4. Prenom: `Lina`
5. Nom: `Bib`
6. Email: `lib1@test.com`
7. Telephone: `22222222`
8. Numero employe: `EMP002`
9. Date embauche: `Enter`

Resultat attendu :
- message de creation avec ID

### 3.2 Lister les utilisateurs

1. Choisir `Lister les utilisateurs`

Resultat attendu :
- voir `admin1`
- voir `lib1`

### 3.3 Rechercher un utilisateur

1. Choisir `Rechercher un utilisateur`
2. Saisir `lib1`

Resultat attendu :
- le bibliothecaire apparait

### 3.4 Modifier un utilisateur

1. Choisir `Modifier un utilisateur`
2. Saisir l'ID de `lib1`
3. Changer par exemple le telephone

Resultat attendu :
- modification enregistree

### 3.5 Test compte inactif

Optionnel :

1. Choisir `Desactiver un utilisateur`
2. Desactiver un compte de test
3. Tenter de se connecter avec ce compte

Resultat attendu :
- connexion refusee

### 3.6 Reactiver un utilisateur

1. Choisir `Reactiver un utilisateur`
2. Saisir l'ID du compte desactive
3. Tenter de se reconnecter avec ce compte

Resultat attendu :
- le compte redevient actif
- la connexion fonctionne a nouveau

## Test 4 - Connexion bibliothecaire

1. Retourner au menu principal
2. Choisir `Connexion`
3. Username: `lib1`
4. Password: `lib123`

Resultat attendu :
- ouverture du menu bibliothecaire

## Test 5 - Creer categorie, auteur, livre

### 5.1 Ajouter une categorie

1. `Gerer les livres`
2. `Ajouter une categorie`
3. Nom: `Informatique`
4. Description: `Livres info`

### 5.2 Ajouter un auteur

1. `Ajouter un auteur`
2. Prenom: `Robert`
3. Nom: `Martin`

### 5.3 Ajouter un livre

1. `Ajouter un livre`
2. ISBN: `9780132350884`
3. Titre: `Clean Code`
4. Editeur: `Prentice Hall`
5. Annee: `2008`
6. Choisir la categorie `Informatique`
7. Choisir l'auteur `Robert Martin`

Resultat attendu :
- livre cree avec ID

### 5.4 Lister les livres

1. `Lister les livres`

Resultat attendu :
- `Clean Code` apparait

## Test 6 - Creer des exemplaires

1. `Gerer les exemplaires`
2. `Ajouter un exemplaire`
3. ID livre: utiliser l'ID de `Clean Code`
4. Code-barres: `BC001`
5. Statut: `AVAILABLE`
6. Date acquisition: `Enter`

Recommencer pour un deuxieme exemplaire :

1. ID livre: meme livre
2. Code-barres: `BC002`
3. Statut: `AVAILABLE`

Resultat attendu :
- les deux exemplaires apparaissent dans `Lister tous les exemplaires`

## Test 7 - Creer un adherent

1. `Gerer les adherents`
2. `Ajouter un adherent`
3. Username: `mem1`
4. Password: `mem123`
5. Prenom: `Sami`
6. Nom: `Member`
7. Email: `mem1@test.com`
8. Telephone: `555111`
9. Numero d'adhesion: `M001`
10. Adresse: `Tunis`
11. Date inscription: `Enter`
12. Max emprunts: `3`

Resultat attendu :
- adherent cree avec ID

Puis :

1. `Lister les adherents`
2. `Verifier l'eligibilite d'emprunt`

Resultat attendu :
- l'adherent apparait
- il peut emprunter

## Test 8 - Creer un emprunt

1. `Creer un emprunt`
2. ID adherent: ID de `mem1`
3. ID exemplaire: ID de `BC001`

Resultat attendu :
- emprunt cree
- date de retour prevue = aujourd'hui + 14 jours

Verifier ensuite :

1. `Lister tous les exemplaires`
2. `Voir l'historique d'un adherent`

Resultat attendu :
- `BC001` est `BORROWED`
- l'emprunt apparait dans l'historique

## Test 9 - Connexion adherent

1. Retour au menu principal
2. `Connexion`
3. Username: `mem1`
4. Password: `mem123`

Tester :

1. `Rechercher des livres`
2. `Consulter mes emprunts`
3. `Consulter mes penalites`
4. `Consulter mes reservations`

Resultat attendu :
- l'emprunt est visible
- aucune penalite
- aucune reservation

## Test 10 - Retour sans penalite

1. Reconnexion en bibliothecaire
2. `Enregistrer un retour`
3. Saisir l'ID du pret actif

Resultat attendu :
- retour enregistre
- penalite = `0`
- exemplaire redevient `AVAILABLE`

Verifier :

1. historique adherent
2. liste des exemplaires

## Test 11 - Retour avec penalite

Pour tester un retard, refaire un emprunt puis modifier la date d'echeance en base.

### 11.1 Refaire un emprunt

1. `Creer un emprunt`
2. meme adherent
3. exemplaire `BC001`

Noter l'ID du nouvel emprunt.

### 11.2 Simuler un retard dans MySQL/phpMyAdmin

Executer :

```sql
USE library_db;

UPDATE loans
SET due_date = DATE_SUB(CURDATE(), INTERVAL 3 DAY)
WHERE loan_id = YOUR_LOAN_ID;
```

Remplacer `YOUR_LOAN_ID` par le vrai ID.

### 11.3 Retourner le livre

1. `Enregistrer un retour`
2. saisir cet `loan_id`

Resultat attendu :
- penalite = `6.00`

Car :

```text
3 jours de retard x 2.00 = 6.00
```

## Test 12 - Penalites impayees

1. `Gerer les penalites`
2. `Lister les penalites impayees`

Resultat attendu :
- voir la penalite `UNPAID`

## Test 13 - Blocage en cas d'impaye

1. Tenter `Creer un emprunt` pour `mem1`

Resultat attendu :
- refus d'emprunt car penalite impayee

## Test 14 - Paiement ou annulation de penalite

### 14.1 Marquer comme payee

1. `Gerer les penalites`
2. `Marquer comme payee`
3. saisir l'ID de la penalite

Puis retester un emprunt.

Resultat attendu :
- l'emprunt redevient possible

### 14.2 Variante annulation

1. `Annuler une penalite`

Resultat attendu :
- statut `CANCELLED`

## Test 15 - Reservations

Pour tester les reservations, il faut qu'aucun exemplaire du livre ne soit disponible.

### 15.1 Rendre le livre non disponible

Option simple :

1. emprunter `BC001`
2. emprunter `BC002` avec un autre adherent ou liberer le blocage de `mem1` puis emprunter les 2 copies

### 15.2 Creer une reservation

Depuis le compte adherent :

1. `Reserver un livre`
2. saisir l'ID du livre `Clean Code`

Resultat attendu :
- reservation creee avec statut `PENDING`

### 15.3 Tester le doublon

1. refaire `Reserver un livre`
2. meme livre

Resultat attendu :
- refus car reservation deja en attente

### 15.4 Annuler

Depuis bibliothecaire :

1. `Gerer les reservations`
2. `Lister les reservations`
3. `Annuler une reservation`

Resultat attendu :
- statut `CANCELLED`

## Test 16 - Rapports

Depuis admin ou bibliothecaire :

1. `Consulter les rapports`

Verifier la presence des rapports :

1. emprunts actifs
2. retards
3. penalites impayees
4. livres les plus empruntes
5. adherents les plus actifs
6. disponibilite par livre

## Test 17 - Verification directe en base

Vous pouvez verifier les donnees avec :

```sql
USE library_db;

SELECT * FROM users;
SELECT * FROM members;
SELECT * FROM staff;
SELECT * FROM categories;
SELECT * FROM authors;
SELECT * FROM books;
SELECT * FROM book_copies;
SELECT * FROM loans;
SELECT * FROM fines;
SELECT * FROM reservations;
```

## Test 18 - Bonus Swing

Dans IntelliJ, lancer l'application avec l'argument :

```text
--swing
```

Verifier :

1. ecran de connexion
2. connexion avec un compte existant
3. affichage du tableau de bord
4. consultation livres, adherents, emprunts, penalites, rapports

## Checklist finale

- Connexion admin : OK
- Connexion bibliothecaire : OK
- Connexion adherent : OK
- Creation utilisateurs : OK
- Creation adherent : OK
- Creation categorie/auteur/livre : OK
- Creation exemplaires : OK
- Emprunt : OK
- Retour sans penalite : OK
- Retour avec penalite : OK
- Blocage si impaye : OK
- Paiement/annulation penalite : OK
- Reservation : OK
- Rapports : OK
- Persistance en base : OK
- Bonus Swing : optionnel
