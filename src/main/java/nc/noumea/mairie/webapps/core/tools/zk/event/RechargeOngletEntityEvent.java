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

import org.zkoss.zk.ui.event.Event;

import lombok.Getter;
import nc.noumea.mairie.webapps.core.tools.domain.Entity;

/**
 * Evénement pour demander le rechargement complet d'un onglet concernant une entité particulière.
 *
 * @author AgileSoft.NC
 */
public class RechargeOngletEntityEvent extends Event {

	private static final long	serialVersionUID	= 1L;

	@Getter
	private final Entity		entity;

	@Getter
	private final String		editViewURI;

	@Getter
	private final String		titreOnglet;

	@Getter
	private final Integer		selectedTabIndex;

	public RechargeOngletEntityEvent(Entity entity, String editViewURI, String titreOnglet, Integer selectedTabIndex) {
		super("rechargeOngletEntityEvent", null, entity);
		this.entity = entity;
		this.editViewURI = editViewURI;
		this.titreOnglet = titreOnglet;
		this.selectedTabIndex = selectedTabIndex;
	}
}
