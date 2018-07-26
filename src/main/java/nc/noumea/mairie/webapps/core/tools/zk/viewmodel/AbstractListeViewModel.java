package nc.noumea.mairie.webapps.core.tools.zk.viewmodel;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.ListModelList;

import com.google.common.collect.Lists;

import nc.noumea.mairie.webapps.core.tools.domain.Entity;
import nc.noumea.mairie.webapps.core.tools.error.TechnicalException;

/**
 * ViewModel abstrait parent des ViewModel de liste (qui permettent de visualiser dans une grille des entités).
 *
 * @param <T> Type paramétré (représente une classe d'entité en pratique)
 * @author AgileSoft.NC
 */
public abstract class AbstractListeViewModel<T extends Entity> extends AbstractViewModel<T> {

	private static Logger		log	= LoggerFactory.getLogger(AbstractListeViewModel.class);

	protected ListModelList<T>	listeEntity;

	/**
	 * @return la liste des entités affichées
	 */
	public ListModelList<T> getListeEntity() {
		return listeEntity;
	}

	public void setListeEntity(ListModelList<T> listeEntity) {
		this.listeEntity = listeEntity;
	}

	@Init
	public void init() {
		listeEntity = new ListModelList<>();
	}

	/**
	 * Méthode à redéfinir, en cas de volumétrie importante
	 */
	public void refreshListe() {
		listeEntity.clear();
		listeEntity.addAll(Lists.newArrayList(getService().findAll())); // par défaut, charge tout
	}

	/**
	 * Rafraîchit globalement la liste, si la classe en argument est la classe entity (ou une sous-classe)
	 * @param entityClass la classe de l'entité
	 */
	@GlobalCommand
	public void refreshListeGlobal(@BindingParam("entityClass") Class entityClass) {
		try {
			if (Class.forName(entityClass.getName()).isAssignableFrom(Class.forName(getEntityClass().getName()))) {
				// note : test un peu compliqué, mais pas de solution plus simple identifiée (le passage par le Class.forName semble obligatoire, sinon la
				// condition renvoie toujours false)
				refreshListe();
			}
		} catch (ClassNotFoundException e) {
			log.error("Classe non trouvée", e);
			throw new TechnicalException("Classe non trouvée " + entityClass.toString());
		}
	}

	/**
	 * Met àjour l'entity dans la liste (si elle s'y trouve)
	 *
	 * @param persistedEntity entité concernée
	 */
	protected void updateEntity(T persistedEntity) {
		if (persistedEntity == null) {
			return;
		}
		int index = listeEntity.indexOf(persistedEntity);
		boolean removed = listeEntity.remove(persistedEntity);
		if (removed) {
			listeEntity.add(index, persistedEntity);
		}
	}

}
