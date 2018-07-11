package nc.noumea.mairie.webapps.core.tools.docx

import nc.noumea.mairie.webapps.core.tools.util.DateUtil
import nc.noumea.mairie.webapps.core.tools.util.ReflectUtil
import org.docx4j.wml.SdtElement
import org.slf4j.LoggerFactory
import java.util.*

/*-
 * #%L
 * WebApps Core Tools
 * %%
 * Copyright (C) 2018 Mairie de Nouméa, Nouvelle-Calédonie
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

abstract class AbstractTemplateDocxTagResolver : TemplateDocxTagResolver {

    companion object {
        /**
         * Exemple : "uppercase_rootObjet.sousObjet.propriete"
         */
        private val EXPRESSION_REGEXP = "(.+_)?([^_]+)".toRegex()
    }

    private val logger = LoggerFactory.getLogger(AbstractTemplateDocxTagResolver::class.java)

    override fun resolve(tagName: String, tagElement: SdtElement): String? {
        val path = getPath(tagName)
        val rootObjectName = resolvePathRootObjectName(path)
        val obj = resolvePathRootObject(rootObjectName)
        val value = if (obj != null) ReflectUtil.findObjectFromPath(path.replaceFirst("$rootObjectName.", ""), obj) else resolveSimpleName(path)

        val functions = getFunction(tagName).split('_')
        var result = value
        functions.forEach { result = processGenericFunction(it, result, tagElement) }
        return result as String?
    }

    protected open fun resolvePathRootObjectName(path: String): String {
        return path.split('.').first()
    }

    protected open fun resolvePathRootObject(pathRootName: String): Any? {
        return null
    }

    protected open fun resolveSimpleName(tagName: String): Any? {
        return null
    }

    protected open fun processGenericFunction(functionName: String, value: Any?, tagElement: SdtElement): String? {
        return when (functionName) {
            "uppercase" -> value?.toString()?.toUpperCase()
            "lowercase" -> value?.toString()?.toLowerCase()
            "formatDateAvecMoisEnTexte" -> if (value == null) null else DateUtil.formatDateAvecMoisEnTexte(value as Date)
            "supprimeValeurControleContenuSiVrai" -> {
                tagElement.sdtPr = null
                return null
            }
            else -> {
                if (functionName.isNotEmpty()) logger.warn("Impossible de résoudre la fonction $functionName")
                return value?.toString()
            }
        }
    }

    private fun getPath(tagName: String): String {
        return tagName.replaceFirst(EXPRESSION_REGEXP, "$2")
    }

    private fun getFunction(tagName: String): String {
        return tagName.replaceFirst(EXPRESSION_REGEXP, "$1")
    }
}
