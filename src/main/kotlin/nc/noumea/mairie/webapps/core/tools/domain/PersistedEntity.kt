package nc.noumea.mairie.webapps.core.tools.domain

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


import nc.noumea.mairie.webapps.core.tools.util.EntityUtil
import nc.noumea.mairie.webapps.core.tools.util.MessageErreur
import nc.noumea.mairie.webapps.core.tools.util.MessageErreurUtil
import javax.persistence.MappedSuperclass
import javax.persistence.Version

/**
 * Entité abstraite parente des entités persistées de l'application.
 *
 * @author AgileSoft.NC
 */
@MappedSuperclass
abstract class PersistedEntity : Entity {
    @Version
    open val version: Int = 0

    fun getMaxLength(property: String): Int {
        return EntityUtil.getMaxLength(this, property)!!
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (id == null) super.hashCode() else id!!.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (this::class != other::class) {
            return false
        }
        val otherPersistedEntity = other as PersistedEntity?

        if (id == null && otherPersistedEntity!!.id == null) {
            return super.equals(other)
        }

        // cas où les 2 possédent un id : ils sont considérés égaux si c'est le même id
        return if (id != null && otherPersistedEntity!!.id != null) {
            id == otherPersistedEntity.id
        } else false

        // un id est null, l'autre non
    }

    /**
     * Implémentation par défaut, peut-être redéfinie par les classes filles
     */
    override fun toString(): String {
        return libelleCourt.orEmpty()
    }

    /**
     * @return une liste de message d'erreur concernant l'entité (qui empêche typiquement sa sauvegarde). Ces erreurs peuvent être des violations de contraintes
     * déclarées ou des erreurs spécifiques métier. Ne doit pas retourner null (mais une liste vide dans le cas où il n'y a pas d'erreur). La liste
     * retournée doit être mutable pour permettre aux classes filles d'ajouter d'autres erreurs.
     */
    open fun construitListeMessageErreur(): List<MessageErreur> {
        return MessageErreurUtil.construitListeMessageErreurViolationContrainte(this)
    }

}
