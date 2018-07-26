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

import java.util.Iterator;

import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Window;

import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;
import nc.noumea.mairie.webapps.core.tools.zk.event.AfterSavePersistedEntityEvent;
import nc.noumea.mairie.webapps.core.tools.zk.event.BeforeSavePersistedEntityEvent;

/**
 * ViewModel abstrait parent des ViewModel de création (qui permettent de créer une nouvelle entité dans une popup).
 *
 * @param <T> Type paramétré (représente une classe d'entité en pratique)
 * @author AgileSoft.NC
 */
public abstract class AbstractCreateViewModel<T extends PersistedEntity> extends AbstractViewModel<T> {

	protected boolean openAfterCreate() {
		return true;
	}

	/**
	 * Initialisation du ViewModel (crée une entité vide)
	 *
	 * @throws InstantiationException en cas d'erreur à la création de l'entity
	 * @throws IllegalAccessException en cas d'erreur à la création de l'entity
	 */
	@Init(superclass = true)
	public void init() throws InstantiationException, IllegalAccessException {
		entity = createEntity();
	}

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		if (view == null) {
			return;
		}
		Iterator<Component> iterator = Selectors.iterable(view, "#create" + super.getEntityClass().getSimpleName()).iterator();
		if (!iterator.hasNext()) {
			iterator = Selectors.iterable(view, "window").iterator();
		}
		if (!iterator.hasNext()) {
			return; // peut arriver
		}
		setPopup((Window) iterator.next());
	}

	/**
	 * Enregistre la nouvelle entité créée, et ouvre l'entité en édition dans un nouvel onglet. Poste également un événement global pour signaler la création de
	 * l'entité.
	 */
	@Command
	@NotifyChange("entity")
	public void save() {
		if (AbstractViewModel.showErrorPopup(this.entity)) {
			return;
		}

		BeforeSavePersistedEntityEvent eventBeforeSave = new BeforeSavePersistedEntityEvent(entity,
				this.popup.getParent() == null ? this.popup : this.popup.getParent());
		Events.sendEvent(eventBeforeSave);

		if (eventBeforeSave.isStopSave()) {
			return;
		}

		getService().save(entity);
		Events.sendEvent(new AfterSavePersistedEntityEvent(entity, this.popup.getParent() == null ? this.popup : this.popup.getParent()));
		postGlobalCommandRefreshListe();
		closePopup();
		if (this.openAfterCreate()) {
			this.ouvreOnglet(entity, null, null);
		}
	}

	/**
	 * Créé une nouvelle entité vide, et ouvre la popup de création pour renseigner les champs de cet entité.
	 *
	 * @throws InstantiationException en cas d'erreur à la création de l'entity
	 * @throws IllegalAccessException en cas d'erreur à la création de l'entity
	 */
	protected void createEntityInNewWindow() throws InstantiationException, IllegalAccessException {
		this.entity = createEntity();
	}
}
