package nc.noumea.mairie.webapps.core.tools.docx;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Text;

/**
 * Supprime les pages vides d'un document
 */
public class SupprimePageVideDocxProcessor implements DocxProcessor {

	@Override
	public void apply(WordprocessingMLPackage wordMLPackage) {
		List<Object> listeEltPremierNiveau = wordMLPackage.getMainDocumentPart().getContent();
		List<Object> listeEltASupprimer = new ArrayList<>();
		List<Object> listeEltDepuisSautPage = new ArrayList<>();
		boolean hasTextFromLastBreakPage = false;

		for (Object eltPremierNiveau : listeEltPremierNiveau) {
			listeEltDepuisSautPage.add(eltPremierNiveau);
			hasTextFromLastBreakPage |= containsText(eltPremierNiveau);

			// recherche un saut de page dans l'élément de premier niveau
			Optional<Object> optionalBreakPage = TemplateDocx.getAllElementFromObject(eltPremierNiveau, Br.class).stream()
					.filter(o -> ((Br) o).getType() == STBrType.PAGE).findFirst();

			boolean isLastEltPremierNiveau = eltPremierNiveau.equals(listeEltPremierNiveau.get(listeEltPremierNiveau.size() - 1));

			// recupération du prochain élément de premier niveau si pas de saut page et si ce n'est pas le dernier
			if (!optionalBreakPage.isPresent() && !isLastEltPremierNiveau) {
				continue;
			}

			if (!hasTextFromLastBreakPage) {
				listeEltASupprimer.addAll(listeEltDepuisSautPage);
			}

			listeEltDepuisSautPage.clear();
			hasTextFromLastBreakPage = false;
		}
		// Suppression de tous les éléments de premier niveau correspondant à une page vide
		wordMLPackage.getMainDocumentPart().getContent().removeAll(listeEltASupprimer);
		removeBreakPageInLastParagraph(wordMLPackage);
	}

	/**
	 * @param elt élément du document
	 * @return Vrai si l'élement contient du texte (espaces ignorés)
	 */
	private boolean containsText(Object elt) {
		return TemplateDocx.getAllElementFromObject(elt, Text.class).stream().anyMatch(textElt -> StringUtils.isNotBlank(((Text) textElt).getValue()));
	}

	/**
	 * Supprime le saut de page du dernier paragraphe du document car inutile
	 * @param wordMLPackage Package Word
	 */
	private void removeBreakPageInLastParagraph(WordprocessingMLPackage wordMLPackage) {
		List<Object> listeEltPremierNiveau = wordMLPackage.getMainDocumentPart().getContent();
		Object lastEltPremierNiveau = listeEltPremierNiveau.get(listeEltPremierNiveau.size() - 1);

		// recherche un saut de page dans l'élément de premier niveau
		Optional<Object> optionalBreakPage = TemplateDocx.getAllElementFromObject(lastEltPremierNiveau, Br.class).stream()
				.filter(o -> ((Br) o).getType() == STBrType.PAGE).findFirst();

		if (optionalBreakPage.isPresent()) {
			((ContentAccessor) ((Br) optionalBreakPage.get()).getParent()).getContent().remove(optionalBreakPage.get());
		}
	}
}
