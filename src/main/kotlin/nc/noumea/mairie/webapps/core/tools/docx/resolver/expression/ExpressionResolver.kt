package nc.noumea.mairie.webapps.core.tools.docx.resolver.expression

interface ExpressionResolver {

    fun resolveExpressionByArbitraryRules(expression: String): Any?

    fun resolveExpressionByPathFromRootObject(expression: String): Any?
}
