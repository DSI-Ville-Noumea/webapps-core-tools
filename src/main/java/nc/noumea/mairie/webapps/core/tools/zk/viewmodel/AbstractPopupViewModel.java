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

import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Window;

import nc.noumea.mairie.webapps.core.tools.domain.AbstractEntity;

/**
 * ViewModel abstrait parent des ViewModel de popup (pour gérer la visibilité de la popup, et la gestion de la fermeture de la popup)
 *
 * @param <T> Type paramétré (représente une classe d'entité en pratique)
 * @author AgileSoft.NC
 */
@Init(superclass = true)
public abstract class AbstractPopupViewModel<T extends AbstractEntity> {

	Window popup;

	public Window getPopup() {
		return popup;
	}

	public void setPopup(Window popup) {
		this.popup = popup;
	}

	public void closePopup() {
		if (popup != null) {
			popup.detach();
		}
	}

	@Command
	@NotifyChange("popupVisible")
	public void cancel(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		event.stopPropagation();
		closePopup();
	}
}
