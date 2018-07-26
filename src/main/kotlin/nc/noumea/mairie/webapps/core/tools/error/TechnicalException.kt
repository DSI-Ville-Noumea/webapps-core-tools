package nc.noumea.mairie.webapps.core.tools.error

class TechnicalException(override val message: String, override val cause: Throwable?) : RuntimeException(message, cause){
    constructor (message: String): this(message, null)
}