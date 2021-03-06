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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.CollectionUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import nc.noumea.mairie.webapps.core.tools.domain.Entity;
import nc.noumea.mairie.webapps.core.tools.domain.PersistedEntity;
import nc.noumea.mairie.webapps.core.tools.error.TechnicalException;
import nc.noumea.mairie.webapps.core.tools.service.GenericService;
import nc.noumea.mairie.webapps.core.tools.util.ApplicationContextUtil;
import nc.noumea.mairie.webapps.core.tools.util.EntityUtil;
import nc.noumea.mairie.webapps.core.tools.util.MessageErreur;
import nc.noumea.mairie.webapps.core.tools.util.MessageErreurUtil;
import nc.noumea.mairie.webapps.core.tools.zk.event.*;
import nc.noumea.mairie.webapps.core.tools.zk.util.ZkUtil;

/**
 * ViewModel abstrait parent des ViewModel de l'application qui manipulent une entité (création, modification, et même liste où on considére que l'entité est
 * celle sélectionnée dans la liste)
 *
 * @param <T> Type paramétré (représente une classe d'entité en pratique)
 * @author AgileSoft.NC
 */
public abstract class AbstractViewModel<T extends Entity> extends AbstractPopupViewModel<T> {

	private static Logger	log	= LoggerFactory.getLogger(AbstractViewModel.class);

	protected T				entity;

	/**
	 * @return l'entité concernée
	 */
	public T getEntity() {
		return entity;
	}

	/**
	 * Fixe l'entité concerné par le ViewModel
	 *
	 * @param entity entité concernée
	 */
	public void setEntity(T entity) {
		this.entity = entity;
	}

	/**
	 * Méthode utilitaire, pour lister les valeurs d'une énumération (dans l'ordre de leur déclaration).
	 *
	 * @param enumClassName nom complet de la classe (avec le package, ex : "nc.noumea.mairie.allodb.enums.TypeConfig")
	 * @return la liste des valeurs énumérées, dans l'ordre de leur déclaration.
	 */
	public ListModelList<?> getListeEnum(String enumClassName) {
		return getListeEnum(enumClassName, false);
	}

	/**
	 * Méthode utilitaire, pour lister les valeurs d'une énumération (dans l'ordre de leur déclaration), avec la possibilité d'insérer en tête la valeur null.
	 *
	 * @param enumClassName nom complet de la classe (avec le package, ex : "nc.noumea.mairie.allodb.enums.TypeConfig")
	 * @param insertNull indique s'il faut insérer en tête de la liste résultat la valeur null
	 * @return la liste des valeurs énumérées, dans l'ordre de leur déclaration (avec null en tête optionnellement)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ListModelList<?> getListeEnum(String enumClassName, boolean insertNull) {
		try {
			Class<?> classe = Class.forName(enumClassName);
			ListModelList result = new ListModelList(classe.getEnumConstants());
			if (insertNull) {
				result.add(0, null);
			}
			return result;
		} catch (ClassNotFoundException e) {
			log.error("erreur sur getListeEnum", e);
			Messagebox.show(e.toString());
		}
		return null;
	}

	/**
	 * Publie un évènement de demande d'ouverture d'une entity dans un nouvel onglet (ou dans un onglet existant si l'entity est déjà ouverte)
	 *
	 * @param entity entité concernée
	 * @param editViewURI la vue si on souhaite surcharger le comportement par défaut
	 * @param selectedTabIndex index de l'onglet à ouvrir
	 */
	@Command
	public void ouvreOnglet(@BindingParam("entity") Entity entity, String editViewURI, Integer selectedTabIndex) {
		defaultPublishOnQueue().publish(new OuvreOngletEntityEvent(entity, editViewURI, selectedTabIndex));
	}

	@Command
	public void ouvreOngletGeneric(String label, String viewURI) {
		defaultPublishOnQueue().publish(new OuvreOngletGenericEvent(label, viewURI));
	}

	/**
	 * Publie un événement de demande de fermeture d'un onglet qui concerne une entity
	 *
	 * @param entity entité concernée
	 */
	public void fermeOnglet(@BindingParam("entity") T entity) {
		defaultPublishOnQueue().publish(new FermeOngletEntityEvent(entity));
	}

	/**
	 * Publie un événement de demande de recharge de l'entity, dans l'onglet couramment sélectionné
	 */
	@Command
	public void rechargeOnglet() {
		rechargeOnglet(entity, null, null, null);
	}

	/**
	 * Publie un événement de demande de recharge de l'onglet d'une liste
	 */
	@Command
	public void rechargeOngletListe() {
		defaultPublishOnQueue().publish(new RechargeOngletListEntityEvent(entity));
	}

	@Command
	public void ouvrePopupCreation() {
		String entityName = getEntityName();
		try {
			ouvrePopupCreation(String.format("~./zul/includes/%s/create%s.zul", entityName.toLowerCase(), entityName));
		} catch (Exception e) {
			log.error(String.format("Erreur ouvrePopupCreation, entité = %s", entityName), e);
		}
	}

	/**
	 * Publie un événement de demande de recharge de l'entity, dans l'onglet couramment sélectionné (ou dans l'onglet indiqué)
	 *
	 * @param entity l'entité à recharger
	 * @param editViewURI l'url de la page d'édition (si null, comportement par défaut, la page sera trouvée à partir du nom de la classe de l'persistedEntity)
	 * @param titreOnglet titre de l'onglet (si null, comportement par défaut, le titre sera trouvé à partir du nom de la classe de l'persistedEntity)
	 * @param selectedTabIndex le sous onglet selectionné
	 */
	protected void rechargeOnglet(Entity entity, String editViewURI, String titreOnglet, Integer selectedTabIndex) {
		defaultPublishOnQueue().publish(new RechargeOngletEntityEvent(entity, editViewURI, titreOnglet, selectedTabIndex));
	}

	protected void refreshOngletGeneric(String titreOnglet, String viewUri) {
		RechargeOngletGenericEvent event = new RechargeOngletGenericEvent(titreOnglet, viewUri);
		defaultPublishOnQueue().publish(event);
	}

	/**
	 * Publie un événement de demande de mise à jour du libellé de l'onglet qui gére l'entity passée en argument.
	 *
	 * @param persistedEntity entité concernée
	 */
	@Command
	public void updateOnglet(@BindingParam("entity") T persistedEntity) {
		defaultPublishOnQueue().publish(new UpdateOngletEntityEvent(persistedEntity));
	}

	public GenericService<T, ?> getService() {
		return (GenericService<T, ?>) ApplicationContextUtil.getApplicationContext().getBean(StringUtils.uncapitalize(getEntityName()) + "Service");
	}

	/**
	 * Retourne le nom simple de la classe de l'entité T
	 *
	 * @return ex : "Projet"
	 */
	public String getEntityName() {
		return ZkUtil.getSimpleNameOfClass(getEntityClass());
	}

	/**
	 * @return la classe paramétrée du viewModel courant ou du viewModel dont on hérite. (ex : Projet.class dans le cas d'un ViewModel paramétré par T =
	 *         Projet). léve une TechnicalException si on n'arrive pas à trouver de classe paramétrée.
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> getEntityClass() {
		return getEntityClass(getClass().getGenericSuperclass());
	}

	@SuppressWarnings("unchecked")
	private Class<T> getEntityClass(Type type) {
		if (type instanceof ParameterizedType) {
			return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
		} else if (getClass().getSuperclass() == null) {
			throw new TechnicalException("Impossible de récupérer le type de la classe " + getClass().toString());
		} else {
			return getEntityClass(getClass().getSuperclass().getGenericSuperclass());
		}
	}

	/**
	 * Instancie un nouvel objet T
	 *
	 * @return instance créée
	 * @throws InstantiationException en cas d'erreur à la création de l'entity
	 * @throws IllegalAccessException en cas d'erreur à la création de l'entity
	 */
	protected T createEntity() throws InstantiationException, IllegalAccessException {
		return getEntityClass().newInstance();
	}

	/**
	 * Poste une commande globale pour signaler la mise à jour de l'entity gérée par le ViewModel courant, en précisant en argument l'entité concernée. Exemple
	 * de commande globale : "updateProjet"
	 */
	public void notifyUpdateEntity() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("entity", entity);
		BindUtils.postGlobalCommand(null, null, "update" + getEntityName(), args);
	}

	/**
	 * Poste une commande globale pour signaler la mise à jour d'une entity quelconque. Exemple de commande globale : "updateDemandeur"
	 *
	 * @param entityUpdated entity concernée
	 */
	protected void notifyUpdateEntity(PersistedEntity entityUpdated) {
		if (entityUpdated == null) {
			return;
		}
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("entity", entityUpdated);
		BindUtils.postGlobalCommand(null, null, "update" + ZkUtil.getSimpleClassNameOfObject(entityUpdated), args);
	}

	public static void notifyChange(String prop, Object bean) {
		BindUtils.postNotifyChange(null, null, bean, prop);
	}

	public void notifyChange(String prop) {
		notifyChange(prop, this);
	}

	public static void notifyChange(String[] listProperty, Object bean) {
		if (listProperty == null) {
			return;
		}
		if (Executions.getCurrent() == null) {
			return;
		}
		for (String prop : listProperty) {
			if (!StringUtils.isBlank(prop)) {
				notifyChange(prop, bean);
			}
		}
	}

	public void notifyChange(String[] listProperty) {
		notifyChange(listProperty, this);
	}

	public void postGlobalCommandRefreshListe() {
		final Map<String, Object> args = new HashMap<String, Object>();
		args.put("entityClass", getEntityClass());
		BindUtils.postGlobalCommand(null, null, "refreshListeGlobal", args);
	}

	/**
	 * Affiche une popup d'erreur, concernant une liste d'erreurs (si la liste est null ou fait 0 élément, la méthode ne fait rien)
	 *
	 * @param listeMessageErreur liste de messages d'erreur à afficher
	 * @return true si au moins un message a été affiché
	 */
	public static boolean showErrorPopup(List<MessageErreur> listeMessageErreur) {
		if (CollectionUtils.isEmpty(listeMessageErreur)) {
			return false;
		}
		Messagebox.show(MessageErreurUtil.construitReprListeMessageErreur(listeMessageErreur), "Erreur", Messagebox.OK, Messagebox.ERROR);
		return true;
	}

	/**
	 * Affiche une popup d'erreur, concernant une erreur unique
	 *
	 * @param message Message d'erreur (si le message est "blanc", la méthode ne fait rien)
	 * @return true si un message (non vide) a été affiché, false sinon
	 */
	public static boolean showErrorPopup(String message) {
		if (org.apache.commons.lang.StringUtils.isBlank(message)) {
			return false;
		}
		List<MessageErreur> listeMessageErreur = new ArrayList<>();
		listeMessageErreur.add(new MessageErreur(message));
		return AbstractViewModel.showErrorPopup(listeMessageErreur);
	}

	/**
	 * Affiche une popup modale de messages d'erreur concernant l'entité (les erreurs spécifiques métier et les violations de contraintes observées sur
	 * l'entité)
	 *
	 * @param entity entité concernée
	 * @return true si des erreurs ont été affichés, false si aucune erreur
	 */
	public static boolean showErrorPopup(PersistedEntity entity) {
		return showErrorPopup(entity.construitListeMessageErreur());
	}

	public static boolean showInformationPopup(String message) {
		if (StringUtils.isBlank(message)) {
			return false;
		}
		Messagebox.show(message, "Information", Messagebox.OK, Messagebox.INFORMATION);
		return true;
	}

	public void showNotificationStandard(String message) {
		Clients.showNotification(message, "info", null, "bottom_center", 3000);
	}

	public Integer getMaxLength(PersistedEntity entity, String property) throws Exception {
		return EntityUtil.getMaxLength(entity, property);
	}

	public Integer getMaxLengthClassProperty(String className, String property) throws Exception {
		return EntityUtil.getMaxLengthClassProperty(className, property);
	}

	public Object ouvrePopupCreation(String template) throws InstantiationException, IllegalAccessException {
		Map<String, Object> arguments = new HashMap<String, Object>();
		T windowEntity = getEntityClass().newInstance();
		arguments.put("entity", windowEntity);
		Window window = (Window) Executions.createComponents(template, null, arguments);
		window.doModal();
		return windowEntity;
	}

	protected EventQueue defaultPublishOnQueue() {
		return EventQueues.lookup("appQueue", EventQueues.DESKTOP, true);
	}

	public Validator getValidator() {
		return new AbstractValidator() {
			@Override
			public void validate(ValidationContext ctx) {
			}
		};
	}
}
