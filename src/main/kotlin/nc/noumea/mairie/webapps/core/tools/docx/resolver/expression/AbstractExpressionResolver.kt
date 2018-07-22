package nc.noumea.mairie.webapps.core.tools.docx.resolver.expression

import nc.noumea.mairie.webapps.core.tools.util.ReflectUtil

abstract class AbstractExpressionResolver : ExpressionResolver {

    /**
     * Résout une expression par réflexion sur l'objet root
     */
    override fun resolveExpressionByPathFromRootObject(expression: String): Any? {
        val rootObjectName = resolveExpressionRootObjectName(expression)
        val rootObject = resolveExpressionRootObject(rootObjectName)
        return if (rootObject == null) null else ReflectUtil.findObjectFromPath(expression.replaceFirst("$rootObjectName.?".toRegex(), ""), rootObject)
    }

    /**
     * Résout le nom de l'objet root d'une expression
     * Exemple1 : rootObjet.sousObjet.propriete --> "rootObjet"
     * Exemple2 : rootObjetPart1.rootObjetPart2.sousObjet.propriete --> "rootObjetPart1.rootObjetPart2"
     *
     * Par défaut, prend le premier segment de l'expression
     */
    protected open fun resolveExpressionRootObjectName(expression: String): String {
        return expression.split('.').first()
    }

    /**
     * Retourne l'objet corespondant au nom de l'objet root d'une expression
     */
    protected open fun resolveExpressionRootObject(rootObjectName: String): Any? {
        return null
    }

    /**
     * Resout de façon arbitraire une expression
     * @return Si la valeur est null c'est que le résolver n'a pas pu déterminer l'expression
     */
    override fun resolveExpressionByArbitraryRules(expression: String): Any? {
        return null
    }
}