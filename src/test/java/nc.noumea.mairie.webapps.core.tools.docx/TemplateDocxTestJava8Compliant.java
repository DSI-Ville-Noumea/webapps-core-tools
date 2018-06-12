package nc.noumea.mairie.webapps.core.tools.docx;

/**
 * Test de génération de docx en Java 8
 */

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public class TemplateDocxTestJava8Compliant {

	private static Logger log = LoggerFactory.getLogger(TemplateDocxTest.class);

	@Test
	public void testCreateDocx() throws IOException, Docx4JException, JAXBException {

		TemplateDocx templateDocx = new TemplateDocx(new File(TemplateDocxTest.DOCX_BASE_DIR + "demo-template.docx"));

		// Insertion d'une image
		File imageExemple = new File(TemplateDocxTest.DOCX_BASE_DIR + "imageExemple.png");
		byte[] imageExempleByteArray = FileUtils.readFileToByteArray(imageExemple);
		templateDocx.setText("monImage", new String(Base64.encodeBase64(imageExempleByteArray)));

		// Subsitution de texte simple
		String nomControleContenuExistantDansDocx = "nomDeFamille";
		templateDocx.setText(nomControleContenuExistantDansDocx, "Duke");

		// Création du document de résultat
		File fichierResultat = File.createTempFile("demo-resultat-jdk8", ".docx");
		log.info("fichier généré = " + fichierResultat.getAbsolutePath());
		// Generates Exception
		templateDocx.createDocx(fichierResultat);
	}

	@Test
	public void testAddParagraphOfTextNotUsingTemplateDocxWarpper() throws IOException, InvalidFormatException, Docx4JException {
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		wordMLPackage.getMainDocumentPart().addParagraphOfText("Java 8 Rocks !");
		wordMLPackage.save(new File(TemplateDocxTest.DOCX_BASE_DIR + "docx4j-from-jdk8.docx"));
	}
}
