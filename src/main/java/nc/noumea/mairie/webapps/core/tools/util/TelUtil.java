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


import org.apache.commons.lang.StringUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Classe utilitaire pour manipuler les numéros de téléphone, en particulier en NC.
 *
 * @author AgileSoft.NC
 */
public class TelUtil {

	/**
	 * format utilisé par la librairie phoneNumber pour la Nouvelle-Calédonie
	 */
	public static final String FORMAT_PHONE_NC = "NC";

	/**
	 * Formatte un numéro de téléphone, en se basant sur la librairie google libPhoneNumber. Si le numéro n'est pas reconnu, le numéro est retourné tel quel
	 * sans les blancs devant/derrière.
	 *
	 * @param tel numéro de téléphone à traiter
	 * @return "" si le téléphone en entrée est "vide" (null ou blanc)
	 */
	public static String formatteTel(String tel) {
		if (StringUtils.isBlank(tel)) {
			return "";
		}
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			PhoneNumber phoneNumber = phoneUtil.parse(tel, FORMAT_PHONE_NC);
			return phoneUtil.format(phoneNumber, PhoneNumberFormat.NATIONAL);
		} catch (Exception e) {
			// en cas d'erreur, on se contente de retourner le tel passé en entrée, sans les blancs devant/derrière
			return StringUtils.trimToEmpty(tel);
		}
	}

}
