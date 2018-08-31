package nc.noumea.mairie.webapps.core.tools.util

import nc.noumea.mairie.webapps.core.tools.docx.TemplateDocx
import java.io.File
import java.io.FileInputStream


const val HELP_TEXT = """*************************************************************************************************
Usage :
 DocxTagTool -h : Affiche cet aide
 DocxTagTool <commande> <arguments>
    Exemple :
        DocxTagTool list -f "c:/temp/"
        DocxTagTool rename -f "c:/temp/mon_doc.docx" -o "old.tag.name" -n "new.tag.name"
    Commandes :
        list : Affiche la liste des noms de contôle de contenu docx
        rename : renomme un contôle de contenu docx
    Arguments :
        Commande list :
            -f <chemin> : Chemin du fichier docx ou du répertoires contenant les fichiers docx à traiter
        Commande rename :
            -f <chemin> : Chemin du fichier docx ou du répertoires contenant les fichiers docx à traiter
            -o <libelle> : Nom du tag à renommer
            -n <libelle> : Nouveau nom du tag
            -e : Si présent, remplace la fin du tag correspondant à l'argument -o par le nouveau nom (replaceLast)
*************************************************************************************************"""

enum class Command {
    LIST,
    RENAME
}

fun main(vararg args: String) {
    if (args.isEmpty() || args.contains("-h")) {
        println(HELP_TEXT)
        return
    }

    if (args[0].isBlank() || Command.values().none { it.name == args[0].trim().toUpperCase() }) {
        println("La commande est obligatoire\n\n$HELP_TEXT")
        return
    }
    val command = Command.valueOf(args[0].trim().toUpperCase())

    val filename = if (args.indexOf("-f") == -1) null else args[args.indexOf("-f") + 1]
    if (filename.isNullOrBlank()) {
        println("L'argument -f <nom de fichier ou répertoire> est obligatoire\n\n$HELP_TEXT")
        return
    }

    val files = ArrayList<File>()
    val fileTemplate = File(filename!!.trim())
    if (fileTemplate.isDirectory) {
        files.addAll(fileTemplate.listFiles { dir, name -> name.endsWith(".docx", true) })
    } else {
        files.add(fileTemplate)
    }

    when (command) {
        Command.LIST -> files.forEach { listTag(it) }
        Command.RENAME -> {
            val oldTagName = if (args.indexOf("-o") == -1) null else args[args.indexOf("-o") + 1]
            if (oldTagName.isNullOrBlank()) {
                println("L'argument -o <ancien nom du tag> est obligatoire\n\n$HELP_TEXT")
                return
            }
            val newTagName = if (args.indexOf("-n") == -1) null else args[args.indexOf("-n") + 1]
            if (newTagName.isNullOrBlank()) {
                println("L'argument -n <nouveau nom du tag> est obligatoire\n\n$HELP_TEXT")
                return
            }
            val endsWith = args.indexOf("-e") != -1
            files.forEach { renameTag(it, oldTagName!!.trim(), newTagName!!.trim(), endsWith) }
        }
    }
}

private fun listTag(fileTemplate: File) {
    val template = TemplateDocx(FileInputStream(fileTemplate))
    println("""Liste des contrôles de contenu du document $fileTemplate :
------------------------
${template.allCustomFieldNames.joinToString("\n")}
------------------------""")
}

private fun renameTag(fileTemplate: File, oldTagName: String, newTagName: String, endsWith: Boolean) {
    val template = TemplateDocx(FileInputStream(fileTemplate))
    val result = template.replaceCustomFieldName(oldTagName, newTagName, endsWith)
    println("""Liste des contrôles de contenu renommés du document $fileTemplate :
------------------------
${result.joinToString("\n")}
------------------------""")
    template.saveToFile(fileTemplate)
}