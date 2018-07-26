package nc.noumea.mairie.webapps.core.tools.zk.event;

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

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import lombok.Getter;
import lombok.Setter;
import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;

/**
 * Evénement généré avant l'enregistrement d'une entité
 *
 * @author AgileSoft.NC
 */
public class BeforeSavePersistedEntityEvent extends Event {

	public static final String		ON_BEFORE_SAVE_ENTITY	= "onBeforeSaveEntity";

	@Getter
	@Setter
	public boolean					stopSave				= false;

	@Getter
	@Setter
	public Component				popup;

	@Getter
	private final PersistedEntity	persistedEntity;

	public BeforeSavePersistedEntityEvent(PersistedEntity persistedEntity, Component target) {
		super(ON_BEFORE_SAVE_ENTITY, target, persistedEntity);
		this.persistedEntity = persistedEntity;
		this.popup = target;
	}
}
