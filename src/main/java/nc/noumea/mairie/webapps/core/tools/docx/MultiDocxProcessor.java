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

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.util.LinkedHashSet;

/**
 * DocxProcessor permettant d'exécuter à la suite plusieurs DocxProcessor
 */
public class MultiDocxProcessor extends LinkedHashSet<DocxProcessor> implements DocxProcessor {
	@Override
	public void apply(WordprocessingMLPackage wordprocessingMLPackage) {
		this.forEach(docxProcessor -> docxProcessor.apply(wordprocessingMLPackage));
	}

	@Override
	public boolean add(DocxProcessor docxProcessor) {
		return super.add(docxProcessor == null ? new EmptyDocxProcessor() : docxProcessor);
	}
}

class EmptyDocxProcessor implements DocxProcessor {
	@Override
	public void apply(WordprocessingMLPackage wordprocessingMLPackage) {
		// Do nothing
	}
}
