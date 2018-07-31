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
import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Messagebox;

import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;
import nc.noumea.mairie.webapps.core.tools.zk.event.AfterSavePersistedEntityEvent;
import nc.noumea.mairie.webapps.core.tools.zk.event.BeforeSavePersistedEntityEvent;
import nc.noumea.mairie.webapps.core.tools.zk.util.ZkUtil;

/**
 * ViewModel abstrait parent des ViewModel de modification (qui permettent de modifier une entité dans un onglet).
 *
 * @param <T> Type paramétré (représente une classe d'entité en pratique)
 * @author AgileSoft.NC
 */
public abstract class AbstractEditViewModel<T extends PersistedEntity> extends AbstractViewModel<T> {

	private static Logger log = LoggerFactory.getLogger(AbstractEditViewModel.class);

	/**
	 * Initilisation du ViewModel : charge l'entité depuis la base de données
	 *
	 * @param persistedEntity entité concernée
	 */
	@Init
	public void initSetup(@ExecutionArgParam("entity") T persistedEntity) {
		this.entity = getService().findOne(persistedEntity.getId());
	}

	/**
	 * Met à jour en base de données l'entité (typiquement sur clic d'un bouton enregistrer), et notifie par une commande globale la mise à jour de l'entité. Si
	 * l'entité présente des contraintes de violation, une popup apparait et la sauvegarde en base n'a pas lieu.
	 */
	@Command
	public void update(@ContextParam(ContextType.VIEW) Component view) {
		if (showErrorPopup(this.entity)) {
			return;
		}

		BeforeSavePersistedEntityEvent eventBeforeSave = new BeforeSavePersistedEntityEvent(entity, view);
		Events.sendEvent(eventBeforeSave);

		if (eventBeforeSave.isStopSave()) {
			return;
		}

		entity = getService().save(entity);
		showNotificationEntityEnregistre();
		notifyUpdateEntity();
		this.updateOnglet(this.entity);
		Events.sendEvent(new AfterSavePersistedEntityEvent(this.entity, view));
	}

	@Override
	protected EventQueue defaultPublishOnQueue() {
		return super.defaultPublishOnQueue();
	}

	void showNotificationEntityEnregistre() {
		showNotificationStandard(getMessageNotificationEnregistre());
	}

	protected String getMessageNotificationEnregistre() {
		return ZkUtil.getSimpleClassNameOfObject(entity) + " enregistré";
	}

	/**
	 * Propose à l'utilisateur la suppression de l'entité. S'il confirme, l'entité est supprimé, l'onglet est fermé et une commande globale est lancée pour
	 * prévenir de la suppression de l'entité.
	 */
	@Command
	public void delete() {
		Messagebox.show("Voulez-vous vraiment supprimer cet élément ?", "Suppression", new Messagebox.Button[] { Messagebox.Button.YES, Messagebox.Button.NO },
				Messagebox.QUESTION, evt -> {
					if (evt.getName().equals("onYes")) {
						getService().delete(entity);
						fermeOnglet(entity);
						postGlobalCommandRefreshListe();
					}
				});
	}

	/**
	 * Recharge l'entity depuis la base de données, si l'entity a été mise à jour par ailleurs.
	 *
	 * @param persistedEntity entité
	 */
	protected void reloadOnEntityUpdate(PersistedEntity persistedEntity) {
		if (persistedEntity == null) {
			return;
		}
		if (this.entity.getId().equals(persistedEntity.getId()) && persistedEntity.getVersion() > this.entity.getVersion()) {
			reloadEntity();
		}
	}

	/**
	 * Recharge l'entity depuis la base de données
	 */
	private void reloadEntity() {
		this.entity = getService().findOne(this.entity.getId());
	}

}
