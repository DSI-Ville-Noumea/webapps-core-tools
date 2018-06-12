package nc.noumea.mairie.webapps.core.tools.docx;

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
