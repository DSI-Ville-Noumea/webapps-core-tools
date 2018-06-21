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

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaSystemException;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.BindUtils;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;

import nc.noumea.mairie.webapps.core.tools.domain.AbstractEntity;
import nc.noumea.mairie.webapps.core.tools.mail.PieceJointeMail;
import nc.noumea.mairie.webapps.core.tools.service.GenericService;
import nc.noumea.mairie.webapps.core.tools.util.FormatUtil;
import nc.noumea.mairie.webapps.core.tools.util.IOUtil;
import nc.noumea.mairie.webapps.core.tools.zk.viewmodel.AbstractViewModel;

public class ZkUtil {

	private static Logger logger = LoggerFactory.getLogger(ZkUtil.class);

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
		if (componentClassName.equals("org.zkoss.zul.Tab") || componentClassName.equals("org.zkoss.zul.Tabbox")) {
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

	public static void deleteReferentielElement(GenericService genericService, AbstractEntity abstractEntity, Class entityClass) {
		Messagebox.show("Voulez-vous vraiment supprimer cet élément ?", "Suppression", new Messagebox.Button[] { Messagebox.Button.YES, Messagebox.Button.NO },
				Messagebox.QUESTION, evt -> {
					if (evt.getName().equals("onYes")) {
						try {
							genericService.delete(abstractEntity.getId());
							BindUtils.postGlobalCommand(null, null, "refreshListe" + getSimpleNameOfClass(entityClass), null);
						} catch (JpaSystemException e) {
							Messagebox.show("Vous ne pouvez pas supprimer cet élément car il est utilisé dans l'application", "Suppression refusée",
									Messagebox.OK, Messagebox.ERROR);
							return;
						}
					}
				});
	}

	public static PieceJointeMail uploadPieceJointe(BindContext ctx, AbstractViewModel viewModel, String nomPieceJointe, Integer tailleMaxPieceJointeEnMo,
			boolean pdfUniquement) {
		UploadEvent upEvent = null;
		Object objUploadEvent = ctx.getTriggerEvent();
		if (objUploadEvent != null && (objUploadEvent instanceof UploadEvent)) {
			upEvent = (UploadEvent) objUploadEvent;
		}
		if (upEvent != null) {
			Media media = upEvent.getMedia();
			if (media != null) {
				if (pdfUniquement && !media.getFormat().equals("pdf")) {
					Messagebox.show("Vous devez importer un fichier PDF");
					return null;
				}
				byte[] contenu = media.getByteData();
				double tailleFichierEnMo = contenu.length / (1024.0 * 1024.0);

				if (tailleMaxPieceJointeEnMo != null && tailleFichierEnMo > tailleMaxPieceJointeEnMo) {
					Messagebox.show("Pièce trop grande, le fichier fait " + FormatUtil.formatteAvec2ChiffreApresVirgule(tailleFichierEnMo, false)
							+ " Mo, et la limite est de " + tailleMaxPieceJointeEnMo + "Mo");
					return null;
				}
				PieceJointeMail pieceJointe = new PieceJointeMail();
				pieceJointe.setNomFichier(media.getName());
				pieceJointe.setContenu(contenu);
				pieceJointe.setMimeType(IOUtil.getMimeType(contenu));
				AbstractViewModel.notifyChange(nomPieceJointe, viewModel);
				return pieceJointe;
			}
		}
		return null;
	}

	public static String construitLibelleTab(int nombreElement, String titre) {
		return titre + (nombreElement > 0 ? " (" + nombreElement + ")" : "");
	}

}
