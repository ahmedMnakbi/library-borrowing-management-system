# Library Borrowing Management System

Application Java orientee objet pour gerer une bibliotheque avec MySQL/MariaDB, JDBC, architecture en couches, version console complete et bonus Swing minimal.

## Technologies

- Java 17+ compatible IntelliJ
- Maven
- JDBC
- MySQL Connector/J
- MySQL ou MariaDB
- JUnit 5

## Structure

```text
src/main/java/com/library
|- app
|- dao
|- enums
|- exception
|- model
|- service
|- ui
|- util
```

## Mise en route dans IntelliJ IDEA

1. Ouvrir le dossier du projet dans IntelliJ IDEA.
2. Laisser IntelliJ importer le `pom.xml`.
3. Verifier que le JDK du projet est configure.
4. Demarrer le serveur MySQL/MariaDB.
5. Creer la base avec `src/main/resources/schema.sql`.
6. Adapter `src/main/resources/db.properties`.
7. Lancer `com.library.app.Main`.

## Base de donnees

Commande example avec XAMPP :

```powershell
C:\xampp\mysql\bin\mysql.exe -u root < src\main\resources\schema.sql
```

## Comptes utilisateurs

Le schema ne cree pas de comptes par defaut. Il faut creer les premiers utilisateurs via SQL ou via l'application si un administrateur existe deja.

Pour generer un hash compatible, vous pouvez lancer `com.library.app.Main` puis utiliser le menu ou la methode `PasswordUtil.hashPassword`.

## Tests

- Tests unitaires JUnit inclus sur le calcul des penalites et les regles metier principales.
- Les tests d'integration JDBC dependent d'une base active et d'un `db.properties` valide.

## Revue de conformite

- Authentification par role : oui
- Gestion livres, auteurs, categories, exemplaires : oui
- Gestion adherents et utilisateurs : oui
- Emprunts, retours, penalites, reservations : oui
- Rapports et historique : oui
- JDBC + requetes preparees : oui
- Architecture OOP en packages : oui
- Interface console minimale : oui
- Bonus Swing minimal : oui

## Notes environnement

- IntelliJ IDEA est installe localement.
- Java est disponible via le runtime embarque IntelliJ.
- MariaDB/XAMPP est installe localement.
- Maven n'est pas requis globalement si IntelliJ importe le projet.
