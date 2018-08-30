package nc.noumea.mairie.webapps.core.tools.resolver

import nc.noumea.mairie.webapps.core.tools.error.TechnicalException
import nc.noumea.mairie.webapps.core.tools.type.TypePrefixListe
import nc.noumea.mairie.webapps.core.tools.type.TypeSeparateur
import nc.noumea.mairie.webapps.core.tools.util.DateUtil
import org.docx4j.wml.ContentAccessor
import org.docx4j.wml.P
import org.docx4j.wml.SdtElement
import org.jvnet.jaxb2_commons.ppp.Child
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

abstract class AbstractTemplateTagResolver : TemplateTagResolver {

    companion object {
        /**
         * Expression régulière d'une expression d'un tag de contrôle de contenu
         * Exemple1 : "rootObjet.sousObjet.propriete"
         * Exemple2 : "maFonction_rootObjet.sousObjet.propriete"
         * Exemple3 : "maFonctionOrdre2_maFonctionOrdre1_rootObjet.sousObjet.propriete"
         */
        private val TAG_REGEXP = "(.+_)?([^_]+)".toRegex()
        /**
         * Expression régulière d'une fonction d'une expression
         * Exemple1 : "maFonction"
         * Exemple2 : "maFonction--complement"
         * Exemple3 : "maFonction--complement1-complement2"
         */
        val FUNCTION_REGEXP = "([^-]+)(--(.+))?".toRegex()

    }

    override fun resolve(tagName: String, tagElement: Any?): String? {
        val expression = getExpression(tagName)
        val expressionValue = resolveExpression(expression)
        return if (expressionValue == null) null else applyFunctions(tagName, tagElement, expressionValue)
    }

    /**
     * Résout une expression de type rootObjet.sousObjet.propriete
     * @return Si la valeur est null c'est que le résolver n'a pas pu déterminer l'expression
     */
    protected abstract fun resolveExpression(expression: String): Any?


    /**
     * Applique la ou les fonctions du tag de contrôle de contenu docx sur un objet
     * Exemple: uppercase, formatDateAvecMoisEnTexte, ...
     */
    private fun applyFunctions(tagName: String, tagElement: Any?, value: Any): String {
        val functions = getFunctions(tagName).split('_').filter { it.isNotBlank() }.reversed()
        var result = value
        functions.forEach { result = processFunction(it, result, tagElement) }
        return result.toString()
    }


    /**
     * Applique une fonction sur un objet
     *
     * uppercase_expression --> "VALEUR DE MON EXPRESSION"
     * lowercase_expression --> "valeur de mon expression"
     * formatDateAvecMoisEnTexte_expression --> "7 janvier 2014"
     * remplaceSautLigneParVigule_expression --> "valeur\nde mon \nexpression" --> "valeur,de mon ,expression"
     * remplaceViguleParSautLigne_expression --> "valeur,de mon ,expression" --> "valeur\nde mon \nexpression"
     * joinListePar--SAUT-DE-LIGNE_expression --> "[element1,element2,element3]" --> "element1\nelement2\nelement3"
     * siVrai--expressionBooleene_expression --> Si l'expression est vraie, le contrôle de contenu prend la valeur de l'expression sinon vide
     * siFaux--expressionBooleene_expression --> Si l'expression est fausse, le contrôle de contenu prend la valeur de l'expression sinon vide
     * supprimeParagrapheSiVrai_expression --> Si l'expression est vraie, le paragraphe contenant le controle de contenu est supprimé. Pas d'autre fonction possible derrière
     * supprimeParagrapheSiFaux_expression --> Si l'expression est fausse, le paragraphe contenant le controle de contenu est supprimé. Pas d'autre fonction possible derrière
     * supprimeParagrapheSiBlank_expression --> Si l'expression est une chaîne blanche, le paragraphe contenant le controle de contenu est supprimé. Pas d'autre fonction possible derrière
     */
    protected open fun processFunction(function: String, value: Any, tagElement: Any?): Any {

        val functionName = function.replace(FUNCTION_REGEXP, "$1")
        val complement = function.replace(FUNCTION_REGEXP, "$3")

        return when (functionName) {
            "uppercase" -> value.toString().toUpperCase()
            "lowercase" -> value.toString().toLowerCase()
            "formatDateAvecMoisEnTexte" -> if (value !is Date) "" else DateUtil.formatDateAvecMoisEnTexte(value)
            "remplaceSautLigneParVirgule" -> value.toString().replace(" *\n\\s*".toRegex(), ", ")
            "remplaceVirguleParSautLigne" -> value.toString().replace(",\\s*".toRegex(), "\n")
            "split" -> {
                if (value !is String) return ""
                return value.split(TypeSeparateur.valueOf(complement.replace("-", "_")).separateur.trim()).map { it.trim() }.toList()
            }
            "prefixElementListePar" -> {
                if (value !is Iterable<*>) return ""
                return value.map {
                    val typePrefix = TypePrefixListe.valueOf(complement.replace("-", "_"))
                    val prefix = if (typePrefix == TypePrefixListe.NUMERO) typePrefix.prefix.replace("{index}", "${value.indexOf(it) + 1}") else typePrefix.prefix
                    "$prefix$it"
                }
            }
            "joinListePar" -> {
                if (value !is Iterable<*>) return ""
                return value.map { it.toString() }.joinToString(TypeSeparateur.valueOf(complement.replace("-", "_")).separateur)
            }
            "siVrai" -> {
                val conditionValue = processCondition(function, complement)
                return if (conditionValue != null && conditionValue) value else ""
            }
            "siFaux" -> {
                val conditionValue = processCondition(function, complement)
                return if (conditionValue != null && !conditionValue) value else ""
            }
            "siNotNull" -> {
                val conditionValue = processCondition(function, complement)
                return if (conditionValue != null) value else ""
            }
            "supprimeParagrapheSiVrai" -> {
                if (tagElement != null && equalsToBoolean(value, true)) {
                    deleteParagraphe(tagElement)
                }
                return ""
            }
            "supprimeParagrapheSiFaux" -> {
                if (tagElement != null && equalsToBoolean(value, false)) {
                    deleteParagraphe(tagElement)
                }
                return ""
            }
            "supprimeParagrapheSiBlank" -> {
                if (tagElement != null && value.toString().isBlank()) {
                    deleteParagraphe(tagElement)
                }
                return value
            }
            else -> throw TechnicalException("Impossible de résoudre la fonction $functionName")
        }
    }

    /**
     * Evalue la condition d'un fonction
     */
    private fun processCondition(function: String, condition: String?): Boolean? {
        if (condition.isNullOrBlank()) throw TechnicalException("Erreur lors de l'évaluation du tag d'un contrôle de contenu : La fonction $function doit être de la forme $function[condition]")
        val conditionValue = resolveExpression(condition!!)
        return if (conditionValue == null) null else (conditionValue is Boolean && conditionValue) || (conditionValue is String && conditionValue.toLowerCase() == "true")
    }

    /**
     * Vérifie que la valeur booléenne d'un objet correspond à la valeur booléenne attendue
     * Exemples :
     *      value = true, expected = true --> true
     *      value = false, expected = true --> false
     *      value = false, expected = false --> true
     *      value = "false", expected = false --> true
     */
    protected fun equalsToBoolean(value: Any, expected: Boolean): Boolean {
        return (value is Boolean && value == expected) || (value is String && value.toLowerCase() == (if (expected) "true" else "false"))
    }

    /**
     * Supprime le paragraphe contenant le contrôle de contenu
     */
    protected fun deleteParagraphe(tagElement: Any) {
        if (tagElement !is SdtElement) return
        var parent = (tagElement as Child).parent
        while (parent != null && parent !is P) {
            parent = (tagElement as Child).parent
        }
        if (parent != null) {
            (((parent as Child).parent) as ContentAccessor).content.remove(parent)
        }
    }

    /**
     * Récupère la partie expression d'un tag de contrôle de contenu
     */
    private fun getExpression(tagName: String): String {
        return tagName.replaceFirst(TAG_REGEXP, "$2")
    }

    /**
     * Récupère la partie fonctions d'un tag de contrôle de contenu
     */
    private fun getFunctions(tagName: String): String {
        return tagName.replaceFirst(TAG_REGEXP, "$1")
    }
}
