package nc.noumea.mairie.webapps.core.tools.docx

import nc.noumea.mairie.webapps.core.tools.util.DateUtil
import nc.noumea.mairie.webapps.core.tools.util.ReflectUtil
import org.docx4j.wml.ContentAccessor
import org.docx4j.wml.P
import org.docx4j.wml.SdtElement
import org.jvnet.jaxb2_commons.ppp.Child
import org.slf4j.LoggerFactory
import java.util.*
import javax.xml.bind.JAXBElement

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
        private val TAG_EXPRESSION_REGEXP = "(.+_)?([^_]+)".toRegex()
    }

    private val logger = LoggerFactory.getLogger(AbstractTemplateDocxTagResolver::class.java)

    override fun resolve(tagName: String, tagElement: SdtElement): String? {
        val path = getPath(tagName)
        val rootObjectName = resolvePathRootObjectName(path)
        val obj = resolvePathRootObject(rootObjectName)
        val value = if (obj != null) ReflectUtil.findObjectFromPath(path.replaceFirst("$rootObjectName.", ""), obj) else resolveSimpleName(path)

        val functions = getFunction(tagName).split('_').filter { it.isNotBlank() }.reversed()
        var result = value
        functions.forEach { result = processGenericFunction(it, result, tagElement) }
        return result?.toString()
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

    /**
     * Applique une fonction sur la valeur d'un contrôle de contenu docx
     */
    protected open fun processGenericFunction(functionName: String, value: Any?, tagElement: SdtElement): String? {
        return when (functionName) {
            "uppercase" -> value?.toString()?.toUpperCase()
            "lowercase" -> value?.toString()?.toLowerCase()
            "formatDateAvecMoisEnTexte" -> if (value == null) null else DateUtil.formatDateAvecMoisEnTexte(value as Date)
            "remplaceSautLigneParVigule" -> if (value == null) null else value.toString().replace("\n", ", ")
            "remplaceParValeurControleContenuSiVrai" -> {
                if (equalsToBoolean(value, true)) {
                    replaceTagParDefaultContent(tagElement)
                }
                return value?.toString()
            }
            "remplaceParValeurControleContenuSiFaux" -> {
                if (equalsToBoolean(value, false)) {
                    replaceTagParDefaultContent(tagElement)
                }
                return value?.toString()
            }
            "videSiVrai" -> {
                if (equalsToBoolean(value, true)) {
                    return ""
                }
                return value?.toString()
            }
            "videSiFaux" -> {
                if (equalsToBoolean(value, false)) {
                    return ""
                }
                return value?.toString()
            }
            "supprimeParagrapheSiVrai" -> {
                if (equalsToBoolean(value, true)) {
                    deleteParagraphe(tagElement)
                }
                return value?.toString()
            }
            "supprimeParagrapheSiFaux" -> {
                if (equalsToBoolean(value, false)) {
                    deleteParagraphe(tagElement)
                }
                return value?.toString()
            }
            "supprimeParagrapheSiBlank" -> {
                if (value != null && value.toString().isBlank()) {
                    deleteParagraphe(tagElement)
                }
                return value?.toString()
            }
            else -> {
                if (functionName.isNotEmpty()) logger.warn("Impossible de résoudre la fonction $functionName")
                return value?.toString()
            }
        }
    }

    protected fun equalsToBoolean(value: Any?, expected: Boolean): Boolean {
        return (value is Boolean && value == expected) || (value is String && value.toLowerCase() == (if (expected) "true" else "false"))
    }

    /**
     * Supprime le paragraphe contenant le contrôle de contenu
     */
    protected fun deleteParagraphe(tagElement: SdtElement) {
        var parent = (tagElement as Child).parent
        while (parent != null && parent !is P) {
            parent = (tagElement as Child).parent
        }
        if (parent != null) {
            (((parent as Child).parent) as ContentAccessor).content.remove(parent)
        }
    }

    /**
     * Remplace (supprime) le contrôle de contenu par la valeur par défaut contenue dans celui-ci
     */
    protected fun replaceTagParDefaultContent(tagElement: SdtElement) {
        val parentJaxbEltIndex = ((tagElement as Child).parent as ContentAccessor).content.indexOfFirst { it is JAXBElement<*> && it.value == tagElement }
        ((tagElement as Child).parent as ContentAccessor).content.removeAt(parentJaxbEltIndex)
        ((tagElement as Child).parent as ContentAccessor).content.addAll(parentJaxbEltIndex, tagElement.sdtContent.content)
    }

    private fun getPath(tagName: String): String {
        return tagName.replaceFirst(TAG_EXPRESSION_REGEXP, "$2")
    }

    private fun getFunction(tagName: String): String {
        return tagName.replaceFirst(TAG_EXPRESSION_REGEXP, "$1")
    }
}
