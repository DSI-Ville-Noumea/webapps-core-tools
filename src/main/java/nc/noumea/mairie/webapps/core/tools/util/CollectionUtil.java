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


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;

public class CollectionUtil extends org.apache.commons.collections.CollectionUtils {

	/**
	 * @return dernier élément d'une liste s'il existe sinon null
	 */
	public static Object getLastOrNull(List<?> liste) {
		return CollectionUtils.isEmpty(liste) ? null : liste.get(liste.size() - 1);
	}

	/**
	 * @param listeString liste de chaînes
	 * @param separateur separateur de liste
	 * @return chaine représentant la liste séparée par le séparateur
	 */
	public static String joinListeStringNotBlank(List<String> listeString, String separateur) {
		return joinListeStringNotBlank(listeString, separateur, null);
	}

	/**
	 * @param listeString liste de chaînes
	 * @param separateur separateur de liste
	 * @param tronquerApresIndex index de la liste après lequel la liste sera tronquée et affichera "..." (les index de valeur invalide sont ignorés)
	 * @return chaine représentant la liste séparée par le séparateur et tronquée après l'index définit
	 */
	public static String joinListeStringNotBlank(List<String> listeString, String separateur, Integer tronquerApresIndex) {
		if (listeString == null) {
			return null;
		}
		List<String> listeStringRetenu = new ArrayList<>();
		for (String ligne : listeString) {
			if (!StringUtils.isBlank(ligne)) {
				listeStringRetenu.add(StringUtils.trimToEmpty(ligne));
			}
		}
		if (tronquerApresIndex != null && tronquerApresIndex > 0 && tronquerApresIndex < listeStringRetenu.size()) {
			return StringUtils.join(listeStringRetenu.toArray(), separateur, 0, tronquerApresIndex) + separateur + "...";
		} else {
			return StringUtils.join(listeStringRetenu, separateur);
		}
	}

	public static List ajoutNullDebutListe(List result) {
		if (CollectionUtils.isEmpty(result)) {
			return null;
		}

		result.add(0, null);
		return result;
	}

	public static List ajoutNullDebutListe(Iterable result) {
		return ajoutNullDebutListe(Lists.newArrayList(result));
	}

	/**
	 * Méthode utilitaire, créé une liste d'objet depuis une liste de tableau d'objets (sur chaque tableau on ne s'intéresse qu'à la colonne indexColumn)
	 *
	 * @param listOfArrayOfObject liste de tableau d'objets
	 * @param indexColumn numéro de colonne (index dans chaque tableau)
	 * @param <T> type d'objet
	 * @return la liste des objets en position indexColumn dans la liste de tableau d'objets passée en entrée
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> createList(List<Object> listOfArrayOfObject, int indexColumn) {
		List<T> result = new ArrayList<>();
		if (listOfArrayOfObject != null) {
			for (Object arrayOfObject : listOfArrayOfObject) {
				Object[] arrayOfObjectCast = (Object[]) arrayOfObject;
				result.add((T) arrayOfObjectCast[indexColumn]);
			}
		}
		return result;
	}
}
