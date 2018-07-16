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

import org.docx4j.wml.SdtBlock
import org.junit.Test
import java.util.Calendar
import kotlin.test.assertEquals

class TemplateDocxTagResolverTest {

    class TagResolver : AbstractTemplateDocxTagResolver() {
        override fun resolveExpressionByArbitraryRules(expression: String): Any? {
            return when (expression) {
                "uneChaine" -> "Valeur\n de mon, Expression"
                "uneDate" -> {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 8)
                    calendar.set(Calendar.MONTH, 8)
                    calendar.set(Calendar.YEAR, 2018)
                    calendar.set(Calendar.HOUR_OF_DAY, 8)
                    calendar.set(Calendar.MINUTE, 28)
                    calendar.time
                }
                "uneListe" -> arrayListOf("element1", "element2", "element3", "element4")
                "conditionVraie" -> true
                "conditionFausse" -> "false"
                else -> null
            }
        }
    }


    @Test
    fun testFunctions() {
        val resolver = TagResolver()

        assertEquals("VALEUR\n DE MON, EXPRESSION", resolver.resolve("uppercase_uneChaine", SdtBlock()))
        assertEquals("valeur\n de mon, expression", resolver.resolve("lowercase_uneChaine", SdtBlock()))
        assertEquals("8 septembre 2018", resolver.resolve("formatDateAvecMoisEnTexte_uneDate", SdtBlock()))
        assertEquals("Valeur,  de mon, Expression", resolver.resolve("remplaceSautLigneParVirgule_uneChaine", SdtBlock()))
        assertEquals("Valeur\n de mon\n Expression", resolver.resolve("remplaceVirguleParSautLigne_uneChaine", SdtBlock()))
        assertEquals("element1\nelement2\nelement3\nelement4", resolver.resolve("joinListePar--SAUT-DE-LIGNE_uneListe", SdtBlock()))
        assertEquals("element1, element2, element3, element4", resolver.resolve("joinListePar--VIRGULE_uneListe", SdtBlock()))
        assertEquals("Valeur\n de mon, Expression", resolver.resolve("siVrai--conditionVraie_uneChaine", SdtBlock()))
        assertEquals("", resolver.resolve("siVrai--conditionFausse_uneChaine", SdtBlock()))
        assertEquals("", resolver.resolve("siFaux--conditionVraie_uneChaine", SdtBlock()))
        assertEquals("Valeur\n de mon, Expression", resolver.resolve("siFaux--conditionFausse_uneChaine", SdtBlock()))
    }
}
