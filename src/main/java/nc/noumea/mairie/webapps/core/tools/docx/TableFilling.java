package nc.noumea.mairie.webapps.core.tools.docx;

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
import java.util.Map;

/**
 * Modélise le remplissage d'une table lors de la génération d'un .docx
 *
 * @author AgileSoft.NC
 */
public class TableFilling {

	/**
	 * début textuel de la première ligne de la table, pour l'identifier
	 */
	protected String					startOfFirstRow;

	/**
	 * true si la premiere ligne de la table doit être supprimée
	 */
	protected boolean					removeTitleRow					= false;

	/**
	 * true si le texte qui ne correspond pas à un code de remplacement dans listeMapCodeValeur doit être conservé
	 */
	protected boolean					keepReplacementTextNotMatched	= false;

	/**
	 * true si on souhaite que la génération du .docx soulève une exception quand la table n'est pas trouvée dans le template
	 */
	protected boolean					throwExceptionIfNotFound		= true;

	/**
	 * L'index de la ligne qui sert de modèle
	 */
	protected int						templateRowIndex				= 1;

	/**
	 * Contenu à positionner dans la table : liste de map code/valeur où code est le texte en entête à substituer (sur la 2ème ligne de la table)
	 */
	protected List<Map<String, String>>	listeMapCodeValeur				= new ArrayList<>();

	/**
	 * Contenu à positionner dans la table : liste de map code/valeur où code est le texte en entête à substituer (sur la 2ème ligne de la table)
	 * L'Integer en clé permet d'identifier le numéro de ligne de chaque template Row dans le cas où on est dans un document ayant besoin de répéter
	 * plusieurs template les uns à la suite des autres (voir modèle dans le TU)
	 */
	protected List<Map<Integer, Map<String, String>>>	listeMultipleTemplateRowMapCodeValeur				= new ArrayList<>();

	protected List<Map<String, String>>	listeMapCodeValeurTableImbrique	= new ArrayList<>();

	public TableFilling(String startOfFirstRow) {
		this.startOfFirstRow = startOfFirstRow;
	}

	@Override
	public String toString() {
		return "TableFilling [startOfFirstRow=" + startOfFirstRow + ", removeTitleRow=" + removeTitleRow + ", keepReplacementTextNotMatched="
				+ keepReplacementTextNotMatched + ", throwExceptionIfNotFound=" + throwExceptionIfNotFound + ", listeMapCodeValeur=" + listeMapCodeValeur
				+ ", listeMapCodeValeurTableImbrique=" + listeMapCodeValeurTableImbrique + "]";
	}

	public String getStartOfFirstRow() {
		return startOfFirstRow;
	}

	public void setStartOfFirstRow(String startOfFirstRow) {
		this.startOfFirstRow = startOfFirstRow;
	}

	public boolean isRemoveTitleRow() {
		return removeTitleRow;
	}

	public void setRemoveTitleRow(boolean removeTitleRow) {
		this.removeTitleRow = removeTitleRow;
	}

	public boolean isKeepReplacementTextNotMatched() {
		return keepReplacementTextNotMatched;
	}

	public void setKeepReplacementTextNotMatched(boolean keepReplacementTextNotMatched) {
		this.keepReplacementTextNotMatched = keepReplacementTextNotMatched;
	}

	public boolean isThrowExceptionIfNotFound() {
		return throwExceptionIfNotFound;
	}

	public void setThrowExceptionIfNotFound(boolean throwExceptionIfNotFound) {
		this.throwExceptionIfNotFound = throwExceptionIfNotFound;
	}

	public List<Map<String, String>> getListeMapCodeValeur() {
		return listeMapCodeValeur;
	}

	public void setListeMapCodeValeur(List<Map<String, String>> listeMapCodeValeur) {
		this.listeMapCodeValeur = listeMapCodeValeur;
	}

	public List<Map<Integer, Map<String, String>>> getListeMultipleTemplateRowMapCodeValeur() {
		return listeMultipleTemplateRowMapCodeValeur;
	}

	public void setListeMultipleTemplateRowMapCodeValeur(List<Map<Integer, Map<String, String>>> listeMultipleTemplateRowMapCodeValeur) {
		this.listeMultipleTemplateRowMapCodeValeur = listeMultipleTemplateRowMapCodeValeur;
	}

	public List<Map<String, String>> getListeMapCodeValeurTableImbrique() {
		return listeMapCodeValeurTableImbrique;
	}

	public void setListeMapCodeValeurTableImbrique(List<Map<String, String>> listeMapCodeValeurTableImbrique) {
		this.listeMapCodeValeurTableImbrique = listeMapCodeValeurTableImbrique;
	}

	public int getTemplateRowIndex() {
		return templateRowIndex;
	}

	public void setTemplateRowIndex(int templateRowIndex) {
		this.templateRowIndex = templateRowIndex;
	}

	public void addLigne(Map<String, String> mapCodeValeur) {
		if (mapCodeValeur == null) {
			return;
		}
		this.listeMapCodeValeur.add(mapCodeValeur);
	}

	public void addLigneMultipleTemplateRow(Map<Integer, Map<String, String>> mapTemplateRowCodeValeur) {
		if (mapTemplateRowCodeValeur == null) {
			return;
		}

		this.listeMultipleTemplateRowMapCodeValeur.add(mapTemplateRowCodeValeur);
	}

	public void addLigneTableImbrique(Map<String, String> mapCodeValeur) {
		if (mapCodeValeur == null) {
			return;
		}
		this.listeMapCodeValeurTableImbrique.add(mapCodeValeur);
	}
}
