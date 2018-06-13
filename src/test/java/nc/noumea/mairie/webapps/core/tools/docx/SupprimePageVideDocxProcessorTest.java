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

/**
 * Test de suppression d'une page docx vide
 */

import java.io.File;
import java.io.IOException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;

public class SupprimePageVideDocxProcessorTest {

	@Test
	public void applyTest() throws Docx4JException, IOException {
		WordprocessingMLPackage wordPackage = WordprocessingMLPackage.load(new File(TemplateDocxTest.DOCX_BASE_DIR + "doc-with-empty-pages.docx"));

		new SupprimePageVideDocxProcessor().apply(wordPackage);

		File outFile = File.createTempFile("empty-pages-removed", ".docx");
		wordPackage.save(outFile);
	}
}
