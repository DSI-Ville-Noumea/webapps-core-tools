package nc.noumea.mairie.webapps.core.tools.zk.util;

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

import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;
import nc.noumea.mairie.webapps.core.tools.service.GenericService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaSystemException;
import org.zkoss.bind.BindUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ZkUtil {

	public final static String	ZUL_BASE_PATH	= "~./zul/includes/";

	private static Logger		logger			= LoggerFactory.getLogger(ZkUtil.class);

	public static String getSimpleClassNameOfObject(Object object) {
		if (object == null) {
			return null;
		}
		return getSimpleNameOfClass(object.getClass());
	}

	public static String getSimpleNameOfClass(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == null) {
			return null;
		}
		String result = clazz.getSimpleName();
		String marqueur = "_$$"; // quelquefois le simple name contient _$$ suivi d'une chaîne générée, cette méthode permet de ne pas en tenir compte
		if (result.contains(marqueur)) {
			result = result.substring(0, result.indexOf(marqueur));
		}
		return result;
	}

	public static void disableComponentAndChildren(Component component) {
		tryDisableComponent(component);
		for (Component child : component.getChildren()) {
			disableComponentAndChildren(child);
		}
	}

	private static void tryDisableComponent(Component component) {
		// log.trace("tryDisableComponent : " + component.getClass().getName());
		try {
			if (isComponentToBeDisabled(component)) {
				Method m = component.getClass().getMethod("setDisabled", Boolean.TYPE);
				m.invoke(component, true);
			}
			if (isComponentToHide(component)) {
				Method m = component.getClass().getMethod("setVisible", Boolean.TYPE);
				m.invoke(component, false);
			}
		} catch (Exception e) {
			logger.error("component = " + component, ", classe du component = " + component.getClass().getName(), e);
		}
	}

	private static boolean isComponentToBeDisabled(Component component) {
		String componentClassName = component.getClass().getName();

		if (StringUtils.startsWith(component.getId(), "popupPieceJointe")) {
			return false;
		}
		if (StringUtils.endsWith(component.getId(), "-do-not-disable")) {
			return false;
		}
		if (componentClassName.equals("org.zkoss.zul.Tab") || componentClassName.equals("org.zkoss.zul.Tabbox")
				|| componentClassName.equals("org.zkoss.zul.Listheader")) {
			return false;
		}
		return true;
	}

	private static boolean isComponentToHide(Component component) {
		String componentClassName = component.getClass().getName();
		if (componentClassName.equals("org.zkoss.zul.Toolbarbutton")) {
			return true;
		}
		if (componentClassName.equals("org.zkoss.zul.Button")) {
			return true;
		}
		return false;
	}

	public static Label findLabelByRowId(Grid sideBarGrid, String rowId) {
		for (Component component : sideBarGrid.getRows().getChildren()) {
			if (component instanceof Row && component.getId().equals(rowId)) {
				return (Label) component.getChildren().get(1);
			}
		}

		return null;
	}

	public static void deleteReferentielElement(GenericService genericService, PersistedEntity persistedEntity, Class entityClass) {
		Messagebox.show("Voulez-vous vraiment supprimer cet élément ?", "Suppression", new Messagebox.Button[] { Messagebox.Button.YES, Messagebox.Button.NO },
				Messagebox.QUESTION, evt -> {
					if (evt.getName().equals("onYes")) {
						try {
							genericService.delete(persistedEntity.getId());
							Map<String, Object> args = new HashMap<>();
							args.put("entityClass", persistedEntity.getClass());
							BindUtils.postGlobalCommand(null, null, "refreshListeGlobal", args);
						} catch (JpaSystemException e) {
							Messagebox.show("Vous ne pouvez pas supprimer cet élément car il est utilisé dans l'application", "Suppression refusée",
									Messagebox.OK, Messagebox.ERROR);
							return;
						}
					}
				});
	}

	public static String construitLibelleTab(int nombreElement, String titre) {
		return titre + (nombreElement > 0 ? " (" + nombreElement + ")" : "");
	}

	public static Component getParentViewWithViewModel(Component view) {
		Component parent = view.getParent();
		while (parent != null) {
			Object parentViewModel = getViewModel(parent);
			if (parentViewModel != null)
				return parent;
			parent = parent.getParent();
		}
		return null;
	}

	public static Object getViewModel(Component view) {
		return view.getAttribute("$VM$");
	}

	/**
	 * Méthode utilitaire pour créer un component ZK
	 * @param pathZul le chemin relatif
	 * @param args : la map d'arguments
	 * @return le composant créé
	 */
	public static Component createComponent(String pathZul, Map<String, Object> args) {
		return Executions.createComponents(ZUL_BASE_PATH + pathZul, null, args);
	}

}
