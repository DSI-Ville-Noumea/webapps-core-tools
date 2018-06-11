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


import java.util.Date;

/**
 * Stockage d'une String associée à une Date (par exemple pour stocker une valeur valide à partir d'une date donnée)
 */
public class StringDate {

	private String	valeur;
	private Date	date;

	/**
	 * @param date date, valeur null autorisée
	 * @param valeur valeur, valeur null autorisée
	 */
	public StringDate(Date date, String valeur) {
		this.date = date;
		this.valeur = valeur;
	}

	public Date getDate() {
		return date;
	}

	public String getValeur() {
		return valeur;
	}
}
