package nc.noumea.mairie.webapps.core.tools.util;

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


import nc.noumea.mairie.webapps.core.error.TechnicalException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour gérer l'application-context spring
 */
public class ApplicationContextUtil implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		ApplicationContextUtil.setApplicationContextStatic(appContext);
	}

	/**
	 * passage par une méthode statique pour éviter anomalie dans les rapports findbug/pmd
	 */
	public static void setApplicationContextStatic(ApplicationContext appContext) {
		applicationContext = appContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
