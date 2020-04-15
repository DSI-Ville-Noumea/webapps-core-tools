package nc.noumea.mairie.webapps.core.tools.util;

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

import java.lang.reflect.Field;
import java.util.Collection;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;

public class EntityUtil {

	private static Logger logger = LoggerFactory.getLogger(EntityUtil.class);

	/**
	 * retourne la taille maximale déclarée par l'annotation @{@link Column} sur la propriété d'une entité
	 *
	 * @param entity entité
	 * @param property nom de la propriété concernée
	 * @return taille max. déclarée
	 * @throws Exception exception en cas d'erreur
	 */
	public static Integer getMaxLength(PersistedEntity entity, String property) throws Exception {
		if (entity == null) {
			return null;
		}
		return getMaxLengthGeneric(entity.getClass(), property);
	}

	/**
	 * retourne la taille maximale déclarée par l'annotation @{@link Column} sur la propriété d'une entité
	 *
	 * @param entityClassName nom de la classe de l'entité
	 * @param property nom de la propriété concernée
	 * @return taille max. déclarée
	 * @throws Exception exception en cas d'erreur
	 */
	public static Integer getMaxLengthClassProperty(String entityClassName, String property) throws Exception {
		return getMaxLengthGeneric((Class<? extends PersistedEntity>) Class.forName(entityClassName), property);
	}

	private static int getMaxLengthGeneric(Class<? extends PersistedEntity> clazz, String property) throws Exception {
		Field field = null;
		try {
			field = clazz.getDeclaredField(property);
		} catch (NoSuchFieldException e) {
			Class<?> current = clazz;
			while (current.getSuperclass() != null) {
				current = current.getSuperclass();
				try {
					field = current.getDeclaredField(property);
				} catch (NoSuchFieldException nsfe) {
					// On ne fait rien
				}
			}
		}

		if (field == null) {
			throw new NoSuchFieldException();
		}

		return field.getAnnotation(Column.class).length();
	}

	/**
	 * Méthode pour "charger" une collection, pour éviter pb de lazy loading
	 *
	 * @deprecated A la place de cette méthode, utiliser Hibernate.initialize()
	 * qui est la méthde prévue pour faire ça
	 *
	 * @param collection collection à charger
	 */
	@Deprecated
	public static void chargeCollection(@SuppressWarnings("rawtypes") Collection collection) {
		// ne pas enlever cette ligne de debug, volontaire
		if (collection != null) {
			logger.debug(collection.toString());
		}
	}

	/**
	 * Méthode pour "charger" un élément, pour éviter pb de lazy loading
	 *
	 * @deprecated A la place de cette méthode, utiliser Hibernate.initialize()
	 * qui est la méthde prévue pour faire ça
	 *
	 * @param element élément à charger
	 */
	@Deprecated
	public static void chargeElement(Object element) {
		// ne pas enlever cette ligne de debug, volontaire
		if (element != null) {
			logger.debug(element.toString());
		}
	}

	// TODO: différence avec le equals de l'entité ?
	public static boolean sameIdAndNotNull(Long id1, Long id2) {
		if (id1 == null || id2 == null) {
			return false;
		}
		return id1.longValue() == id2.longValue();
	}
}
