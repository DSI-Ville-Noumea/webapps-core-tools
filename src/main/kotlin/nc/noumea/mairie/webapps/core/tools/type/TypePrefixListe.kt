package nc.noumea.mairie.webapps.core.tools.type

enum class TypePrefixListe(val prefix: String) {
    TAB("\t"),
    POINT("\u25CF "),
    TIRET("\u2500 "),
    NUMERO("{index} ")
}