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
package nc.noumea.mairie.webapps.core.tools.docx

import nc.noumea.mairie.webapps.core.tools.docx.resolver.AbstractTemplateDocxTagResolver
import nc.noumea.mairie.webapps.core.tools.docx.resolver.expression.AbstractExpressionResolver
import nc.noumea.mairie.webapps.core.tools.util.ReflectUtilTest
import org.docx4j.wml.SdtBlock
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class TemplateDocxTagResolverTest {

    class ExpressionResolver(val unObjet: Any?) : AbstractExpressionResolver() {

        override fun resolveExpressionRootObject(rootObjectName: String): Any? {
            return if (rootObjectName == "unObjet") unObjet else null
        }

        override fun resolveExpressionByArbitraryRules(expression: String): Any? {
            return when (expression) {
                "uneChaine" -> "Valeur\n de mon, Expression"
                "uneAutreChaine" -> "Valeurs; séparés; par des; points vigule"
                "uneDate" -> DateTime(2018, 9, 8, 8, 28, DateTimeZone.forID("+11")).toDate()
                "uneListe" -> arrayListOf("element1", "element2", "element3", "element4")
                "conditionVraie" -> true
                "conditionFausse" -> "false"
                else -> null
            }
        }
    }

    class TagResolver(val expressionResolver: ExpressionResolver) : AbstractTemplateDocxTagResolver() {
        override fun resolveExpression(expression: String): Any? {
            return expressionResolver.resolveExpressionByPathFromRootObject(expression) ?: expressionResolver.resolveExpressionByArbitraryRules(expression)
        }
    }

    @Test
    fun testFunctions() {
        val expressionResolver = ExpressionResolver(null)
        val tagResolver = TagResolver(expressionResolver)

        assertEquals("VALEUR\n DE MON, EXPRESSION", tagResolver.resolve("uppercase_uneChaine", SdtBlock()))
        assertEquals("valeur\n de mon, expression", tagResolver.resolve("lowercase_uneChaine", SdtBlock()))
        assertEquals("8 septembre 2018", tagResolver.resolve("formatDateAvecMoisEnTexte_uneDate", SdtBlock()))
        assertEquals("Valeur, de mon, Expression", tagResolver.resolve("remplaceSautLigneParVirgule_uneChaine", SdtBlock()))
        assertEquals("Valeur\n de mon\nExpression", tagResolver.resolve("remplaceVirguleParSautLigne_uneChaine", SdtBlock()))
        assertEquals("[Valeurs, séparés, par des, points vigule]", tagResolver.resolve("split--POINT-VIRGULE_uneAutreChaine", SdtBlock()))
        assertEquals("element1\nelement2\nelement3\nelement4", tagResolver.resolve("joinListePar--SAUT-DE-LIGNE_uneListe", SdtBlock()))
        assertEquals("element1, element2, element3, element4", tagResolver.resolve("joinListePar--VIRGULE_uneListe", SdtBlock()))
        assertEquals("Valeur\n de mon, Expression", tagResolver.resolve("siVrai--conditionVraie_uneChaine", SdtBlock()))
        assertEquals("", tagResolver.resolve("siVrai--conditionFausse_uneChaine", SdtBlock()))
        assertEquals("", tagResolver.resolve("siFaux--conditionVraie_uneChaine", SdtBlock()))
        assertEquals("Valeur\n de mon, Expression", tagResolver.resolve("siFaux--conditionFausse_uneChaine", SdtBlock()))
    }

    @Test
    fun testCreationTemplateAvecTagResolver() {

        val unObjet = ReflectUtilTest.RootObjet()
        val expressionResolver = ExpressionResolver(unObjet)
        val tagResolver = TagResolver(expressionResolver)

        val templateDocx = TemplateDocx(this::class.java.getResourceAsStream("/template_with_tag_to_resolve_test.docx"))
        templateDocx.setTagResolver(tagResolver)
        val fichierTempDocxGenere = File.createTempFile("doc_with_tag_resolved_test-", ".docx")
        val wordMLPackage = templateDocx.createDocx()
        wordMLPackage.save(fichierTempDocxGenere)
        println(fichierTempDocxGenere)
    }
}
