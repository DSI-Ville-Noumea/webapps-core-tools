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
import nc.noumea.mairie.webapps.core.tools.domain.AbstractEntity;

/**
 * Evénement pour demander l'ouverture d'un onglet concernant une entité particulière.
 *
 * @author AgileSoft.NC
 */
public class OuvreOngletAbstractEntityEvent extends Event {

	private static final long		serialVersionUID	= 1L;

	@Getter
	private final AbstractEntity	abstractEntity;

	@Getter
	private final String			editViewURI;

	@Getter
	private final Integer			selectedTabIndex;

	public OuvreOngletAbstractEntityEvent(AbstractEntity abstractEntity, String editViewURI, Integer selectedTabIndex) {
		super("ouvreOngletAbstractEntity", null, abstractEntity);
		this.abstractEntity = abstractEntity;
		this.editViewURI = editViewURI;
		this.selectedTabIndex = selectedTabIndex;
	}
}
