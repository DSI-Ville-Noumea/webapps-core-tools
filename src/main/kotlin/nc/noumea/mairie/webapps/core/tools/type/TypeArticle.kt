package nc.noumea.mairie.webapps.core.tools.type

/*-
 * #%L
 * Logiciel de Gestion des Permis de Construire de la Ville de Nouméa
 * %%
 * Copyright (C) 2013 - 2017 Mairie de Nouméa, Nouvelle-Calédonie
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
import nc.noumea.mairie.webapps.core.tools.util.FormatUtil
import org.apache.commons.lang.StringUtils

/**
 * Enumération permettant de préfixer un champ par une préposition et/ou par un article. Les règles de grammaires pour la contraction de l'article sont
 * appliquées.
 */
enum class TypeArticle private constructor(internal var masculin: String, internal var masculinContracte: String, internal var feminin: String, internal var femininContracte: String, internal var pluriel: String) {
    // masculin, masculin contracté, féminin, féminin contracté, pluriel
    ARTICLE_INDEFINI("un ", "un ", "une ", "une ", "des "),
    ARTICLE_DEFINI("le ", "l'", "la ", "l'", "les "),
    ARTICLE_INDEFINI_PREPOSITION_DE("d'un ", "d'un ", "d'une ", "d'une ", "des "),
    ARTICLE_DEFINI_PREPOSITION_DE("du ", "de l'", "de la ", "de l'", "des "),
    ARTICLE_INDEFINI_PREPOSITION_A("à un ", "à un ", "à une ", "à une ", "à des "),
    ARTICLE_DEFINI_PREPOSITION_A("au ", "à l'", "à la ", "à l'", "aux ");

    fun build(nom: String?, feminin: Boolean = false, pluriel: Boolean = false): String {
        val nomTemp = if (nom.isNullOrBlank()) "-" else nom
        val premiereLettre = if (StringUtils.isNotBlank(nomTemp)) nomTemp?.substring(0, 1) else ""
        val contracte = FormatUtil.PATTERN_L_APOSTROPHE.matcher(premiereLettre).matches()
        if (pluriel) {
            // TODO : voir si il existe un lib qui le fait
            return this.pluriel + nomTemp + "s"
        }
        return if (feminin) {
            (if (contracte) this.femininContracte else this.feminin) + nomTemp
        } else {
            (if (contracte) this.masculinContracte else this.masculin) + nomTemp
        }
    }
}
