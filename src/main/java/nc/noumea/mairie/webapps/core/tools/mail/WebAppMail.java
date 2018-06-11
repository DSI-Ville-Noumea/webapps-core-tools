package nc.noumea.mairie.webapps.core.tools.mail;

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


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;

import lombok.Getter;
import lombok.Setter;
import nc.noumea.mairie.webapps.core.tools.domain.IUtilisateur;

/**
 * Modélise un mail géré par l'application (les clés/contenus sont stockés en paramètres applicatifs)
 *
 * @author AgileSoft.NC
 */
public class WebAppMail {

	@Getter
	List<InternetAddress>	listeDestinataire		= new ArrayList<>();

	@Getter
	List<InternetAddress>	listeDestinataireCc		= new ArrayList<>();

	@Getter
	@Setter
	String					sujet;

	@Getter
	@Setter
	String					contenu;

	@Getter
	@Setter
	List<PieceJointeMail>	listePieceJointeMail	= new ArrayList<>();

	public WebAppMail(String contenu, String sujet, List<PieceJointeMail> listePieceJointeMail) {
		this.contenu = contenu;
		this.sujet = sujet;
		this.listePieceJointeMail = listePieceJointeMail;
	}

	public void addDestinataire(List<IUtilisateur> listeUtilisateur) throws UnsupportedEncodingException {
		for (IUtilisateur utilisateur : listeUtilisateur) {
			addDestinataire(utilisateur);
		}
	}

	public void addDestinataire(IUtilisateur utilisateur) throws UnsupportedEncodingException {
		if (utilisateur == null) {
			return;
		}
		if (StringUtils.isBlank(utilisateur.getEmail())) {
			return;
		}
		this.listeDestinataire.add(createInternetAddress(utilisateur));
	}

	public void addDestinataire(String email) {
		if (StringUtils.isBlank(email)) {
			return;
		}
		try {
			addDestinataire(new InternetAddress(email));
		} catch (AddressException e) {
		}
	}

	public void addDestinataire(InternetAddress adresse) {
		if (adresse == null) {
			return;
		}
		this.listeDestinataire.add(adresse);
	}

	public void addDestinataireCc(IUtilisateur utilisateur) throws UnsupportedEncodingException {
		if (utilisateur == null) {
			return;
		}
		if (StringUtils.isBlank(utilisateur.getEmail())) {
			return;
		}
		this.listeDestinataireCc.add(createInternetAddress(utilisateur));
	}

	public void addDestinataireCc(String email) {
		if (StringUtils.isBlank(email)) {
			return;
		}
		try {
			addDestinataireCc(new InternetAddress(email));
		} catch (AddressException e) {
		}
	}

	public void addDestinataireCc(InternetAddress adresse) {
		if (adresse == null) {
			return;
		}
		this.listeDestinataireCc.add(adresse);
	}

	private InternetAddress createInternetAddress(IUtilisateur utilisateur) throws UnsupportedEncodingException {
		if (utilisateur == null) {
			throw new IllegalArgumentException();
		}
		return new InternetAddress(utilisateur.getEmail(), utilisateur.getNomComplet());
	}
}
