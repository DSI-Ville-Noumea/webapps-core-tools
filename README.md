[![](https://jitpack.io/v/DSI-Ville-Noumea/webapps-core-tools.svg)](https://jitpack.io/#DSI-Ville-Noumea/webapps-core-tools)
[![Dependabot Status](https://api.dependabot.com/badges/status?host=github&identifier=136540346)](https://dependabot.com)

# webapps-core-tools

Ensemble de classes et patterns utilitaires. 

## Installation
Pour un projet génénré à partir du ***zkboot-project-archetype*** :  
* Ajouter la dépendance MAVEN
````
 <dependency>
    <groupId>nc.noumea.mairie</groupId>
    <artifactId>webapps-core-tools</artifactId>
    <version>1.00.00</version>
</dependency>
````
* Faire hériter Utilisateur des classes *IUtilisateur* et *PersistedEntity*
````
import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;
import nc.noumea.mairie.webapps.core.tools.domain.IUtilisateur;
...

@Entity
@Table(name = "UTILISATEUR")
public class Utilisateur extends PersistedEntity implements IUtilisateur {
    ...
}
````
