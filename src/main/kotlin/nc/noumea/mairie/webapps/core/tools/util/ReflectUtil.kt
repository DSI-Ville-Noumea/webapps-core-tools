package nc.noumea.mairie.webapps.core.tools.util

import kotlin.reflect.full.functions

abstract class ReflectUtil {
    companion object {
        /**
         * Permet de trouver un objet depuis un chemin
         * Ex: findObjectFromPath("monObjet.monSousObjet.monObjetCherche", monObjetRacine) retourne monObjetRacine.monObjet.monSousObjet.monObjetCherche
         * Renvoie null si l'objet cible est null ou si un de ses parents est null : meme principe qu'en kotlin : monObjet?.monSousObjet?.monObjetCherche
         */
        fun findObjectFromPath(path: String, root: Any): Any? {
            val objectName = if (path.contains('.')) path.split('.')[0] else path
            var objectValue = root::class.functions.find { it.name == "get" + objectName[0].toUpperCase() + objectName.substring(1) }?.call(root)
            if (objectValue == null) {
                objectValue = root::class.members.find { it.name == objectName }?.call(root)
            }
            val newPath = if (path.contains('.')) path.replaceFirst("$objectName.", "") else path.replaceFirst(objectName, "")
            if (objectValue == null || newPath.isBlank()) return objectValue
            return findObjectFromPath(newPath, objectValue)
        }
    }
}