package nc.noumea.mairie.webapps.core.tools.type

enum class TypeSeparateur(val separateur: String, val separateurFinal: String? = null) {
    VIRGULE(", "),
    VIRGULE_PUIS_ET(", ", " et "),
    POINT_VIRGULE(" ; "),
    SAUT_DE_LIGNE("\n")
}