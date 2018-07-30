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

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.StringUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.customXmlProperties.DatastoreItem;
import org.docx4j.finders.ClassFinder;
import org.docx4j.model.datastorage.BindingHandler;
import org.docx4j.model.datastorage.CustomXmlDataStorageImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePart;
import org.docx4j.openpackaging.parts.CustomXmlDataStoragePropertiesPart;
import org.docx4j.openpackaging.parts.CustomXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.wml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Classe qui modélise un template avec les données utiles pour générer un fichier .docx
 *
 * @author AgileSoft.NC
 */
public class TemplateDocx {

	private static Logger					log								= LoggerFactory.getLogger(TemplateDocx.class);

	protected WordprocessingMLPackage		wordMLPackage;
	protected CustomXmlDataStoragePart		customXmlPart;
	protected Map<String, List<SdtElement>>	mapTag							= new HashMap<>();

	protected Map<String, String>			mapTagXml						= new HashMap<>();
	protected Map<String, Boolean>			mapValeurCheckBox				= new HashMap<>();
	protected List<TableFilling>			listeTableFilling;
	protected TemplateDocxTagResolver		tagResolver;

	public static final String				NOUVELLE_LIGNE_TABLEAU_IMBRIQUE	= "NOUVELLE_LIGNE_TABLEAU_IMBRIQUE";
	protected static final String			A_COMPLETER						= "<A COMPLETER>";

	/**
	 * Constructeur
	 *
	 * @param template un template .docx
	 */
	public TemplateDocx(InputStream template) throws Docx4JException {
		initDocx(template);
	}

	public void setTagResolver(TemplateDocxTagResolver tagResolver) {
		this.tagResolver = tagResolver;
	}

	/**
	 * Raccourci vers plusieurs setText, avec le peuplement de variantes sur la forme (exemple : version "capitalized_" avec 1ère lettre en majuscule)
	 *
	 * @param controlContentTag nom de la balise XML
	 * @param text texte à substituer
	 */
	public void setTextMultiple(String controlContentTag, String text) {
		setText(controlContentTag, text);
		setText("capitalized_" + controlContentTag, StringUtils.capitalize(text));
		setText("upper_" + controlContentTag, StringUtils.upperCase(text));
	}

	/**
	 * Programme le text à substituer sur un tag xml (au moment du createDocx), typiquement bindé sur un ou plusieurs contrôles de contenu (via le Word 2007
	 * content control toolkit)
	 *
	 * @param controlContentTag nom de la balise XML
	 * @param text texte à substituer
	 */
	public void setText(String controlContentTag, String text) {
		if (StringUtils.isBlank(controlContentTag)) {
			log.warn("setText appelé avec controlContentTag = " + controlContentTag);
			return;
		}
		mapTagXml.put(controlContentTag, text);
	}

	/**
	 * Programme le cochage/décochage d'une case à cocher (au moment du createDocx), directement dans la main document part du document Word (pas dans la custom
	 * xml part, comme dans setText)
	 *
	 * @param nomCheckBox Nom de la checkbox
	 * @param checked flag
	 */
	public void setCheckBox(String nomCheckBox, boolean checked) {
		if (StringUtils.isBlank(nomCheckBox)) {
			log.warn("setCheckBox appelé avec nomCheckBox==null");
			return;
		}
		if (mapValeurCheckBox == null) {
			mapValeurCheckBox = new HashMap<String, Boolean>();
		}
		String nomCheckBoxTronquePourLimiteWordA20car = StringUtils.left(StringUtils.trimToEmpty(nomCheckBox), 20);
		mapValeurCheckBox.put(nomCheckBoxTronquePourLimiteWordA20car, checked);
	}

	/**
	 * Programme l'ajout d'un remplissage de table dynamique (au moment du createDocx)
	 *
	 * @param tableFilling Table filling à rajouter
	 */
	public void addTableFilling(TableFilling tableFilling) {
		if (tableFilling == null) {
			log.warn("addTableFilling appelé avec tableFilling == null");
			return;
		}
		if (listeTableFilling == null) {
			listeTableFilling = new ArrayList<>();
		}
		listeTableFilling.add(tableFilling);
	}

	/**
	 * Génère un fichier docx, en utilisant la librairie docx4j
	 *
	 * @param targetFile Fichier résultat
	 * @throws JAXBException en cas d'erreur
	 * @throws Docx4JException en cas d'erreur
	 */
	public void createDocx(File targetFile) throws Docx4JException, JAXBException {
		WordprocessingMLPackage wordMLPackage = createDocx();
		// Ecrit dans le fichier cible
		wordMLPackage.save(targetFile);
	}

	/**
	 * Génère un fichier docx, en utilisant la librairie docx4j
	 *
	 * @return le WordprocessingMLPackage créé, qu'on peut encore modifier avant, typiquement, de l'enregistrer dans un fichier
	 * @throws JAXBException en cas d'erreur
	 * @throws Docx4JException en cas d'erreur
	 */
	public WordprocessingMLPackage createDocx() throws Docx4JException, JAXBException {
		applyBindings();
		return wordMLPackage;
	}

	private void initDocx(File fileTemplate) throws Docx4JException {
		wordMLPackage = WordprocessingMLPackage.load(fileTemplate);
		initDocx(wordMLPackage);
	}

	private void initDocx(InputStream fileTemplate) throws Docx4JException {
		wordMLPackage = WordprocessingMLPackage.load(fileTemplate);
		initDocx(wordMLPackage);
	}

	private void initDocx(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		// Récupére les custom xml parts
		Map<String, CustomXmlPart> customXmlParts = wordMLPackage.getCustomXmlDataStorageParts();
		org.w3c.dom.Document customPartDocument = null;
		boolean doBinding = customXmlParts.isEmpty();
		if (doBinding) {
			// la creation de la custum part a tendance à corrompre le fichier si le document contient déjà un xml custom part. A voir pourquoi.
			// @see CustomXmlDataStoragePart.remove()
			customXmlPart = createCustomXmlPart(wordMLPackage);
			customXmlParts.put(customXmlPart.getItemId(), customXmlPart);
			customPartDocument = customXmlPart.getData().getDocument();
		} else {
			for (CustomXmlPart part : customXmlParts.values()) {
				org.w3c.dom.Document doc = ((CustomXmlDataStoragePart) part).getData().getDocument();
				if (doc != null && "root".equals(doc.getDocumentElement().getTagName())) {
					// le document contient déjà un xml part ayant le tag root pour racine
					customPartDocument = doc;
					customXmlPart = (CustomXmlDataStoragePart) part;
					break;
				}
			}
		}

		List<Object> listeCustomField = getAllCustomField(wordMLPackage);
		for (Object customField : listeCustomField) {
			Tag tag = ((SdtElement) customField).getSdtPr().getTag();
			if (tag == null) {
				continue;
			}
			String tagName = StringUtils.trim(tag.getVal());
			if (StringUtils.isBlank(tagName)) {
				continue;
			}

			if (doBinding) {
				// Création du noeud correspondant dans le custom part s'il nexiste pas
				NodeList customPartXmlEltList = customPartDocument.getElementsByTagName(tagName);
				if (customPartXmlEltList.getLength() == 0) {
					Element customPartXmlElt = customPartDocument.createElement(tagName);
					customPartDocument.getDocumentElement().appendChild(customPartXmlElt);
				}

				// liaison entre le custom field du document et le noeud de la custom part
				CTDataBinding dataBinding = new ObjectFactory().createCTDataBinding();
				((SdtElement) customField).getSdtPr().setDataBinding(dataBinding);
				dataBinding.setStoreItemID(customXmlPart.getItemId());
				dataBinding.setXpath("/root[1]/" + tagName + "[1]");
			}

			mapTag.computeIfAbsent(tagName, s -> new ArrayList<>()).add((SdtElement) customField);
		}
	}

	private void applyBindings() throws Docx4JException, JAXBException {

		applyBindingsForTags();

		// Coche/décoche chaque case à cocher
		for (Entry<String, Boolean> valeur : mapValeurCheckBox.entrySet()) {
			updateCheckBox(wordMLPackage.getMainDocumentPart(), valeur.getKey(), valeur.getValue());
		}

		// Applique les bindings : met à jour réellement le Word à partir des tags XML de la custom xml part
		BindingHandler.applyBindings(wordMLPackage);

		// Crée les tables dynamiques
		if (!CollectionUtils.isEmpty(listeTableFilling)) {
			for (TableFilling tableFilling : listeTableFilling) {
				log.debug("TableFilling = " + tableFilling);

				// repère la 1ère table qui contient une première ligne qui démarre par le texte de repérage
				Tbl table = getTableWithFirstRowStartingWith(wordMLPackage, tableFilling.getStartOfFirstRow());

				// si non trouvée
				if (table == null && tableFilling.isThrowExceptionIfNotFound()) {
					throw new RuntimeException("Table non trouvée : " + tableFilling.getStartOfFirstRow());
				}

				// procède au remplissage (création de n lignes + suppression de la ligne "template")
				if (table != null) {
					fillTable(table, tableFilling);
				}
			}
		}
	}

	private void applyBindingsForTags() throws Docx4JException {
		for (final Entry<String, List<SdtElement>> tagEntry : mapTag.entrySet()) {
			for (SdtElement sdtElement : tagEntry.getValue()) {
				applyBindingsForTag(tagEntry.getKey(), sdtElement);
			}
		}
	}

	private void applyBindingsForTag(String tagName, SdtElement sdtElement) throws Docx4JException {

		// définition de la valeur du noeud
		String tagValue = null;
		if (tagResolver != null) {
			tagValue = tagResolver.resolve(tagName, sdtElement);
		}

		// Si tag non résulue par le resolver on parcours la map
		if (tagValue == null && mapTagXml.containsKey(tagName)) {
			tagValue = (StringUtils.isBlank(mapTagXml.get(tagName)) ? " " : StringUtils.trimToEmpty(mapTagXml.get(tagName)));
		}

		// Si tag toujours pas résolu, on met une valeur par défaut
		if (tagValue == null) {
			// TODO : Ajouter surlignage
			tagValue = A_COMPLETER;
		}

		if (StringUtils.isEmpty(tagValue)) {
			removeContentControl(wordMLPackage, tagName);
		}
		updateXmlTag(customXmlPart, tagName, tagValue);
	}

	/**
	 * Supprimer le Content Control dont le nom est spécifié.
	 *
	 * @param word Le document word
	 * @param name Le nom du Content Control à supprimer
	 */
	public static void removeContentControl(WordprocessingMLPackage word, String name) {

		if (name.isEmpty())
			throw new InvalidParameterException((name));

		MainDocumentPart documentPart = word.getMainDocumentPart();

		// Pour les SdtBlock
		ClassFinder finderSdtBlock = new ClassFinder(SdtBlock.class);
		new TraversalUtil(documentPart.getContent(), finderSdtBlock);
		for (Object o : finderSdtBlock.results) {
			Object o2 = XmlUtils.unwrap(o);
			if (o2 instanceof org.docx4j.wml.SdtBlock) {
				if (((SdtBlock) o2).getSdtPr().getTag() != null && ((SdtBlock) o2).getSdtPr().getTag().getVal().equals(name)) {
					// Si le parent est un ArrayList, il s'agit de la racine Body
					if (((SdtBlock) o2).getParent() instanceof ArrayList) {
						Body body = word.getMainDocumentPart().getJaxbElement().getBody();
						body.getEGBlockLevelElts().remove(o);
					} else {
						// Sinon nous supprimons depuis le parent du content control
						((ContentAccessor) ((SdtBlock) o2).getParent()).getContent().remove(o2);
					}
					return;
				} else if (((SdtBlock) o2).getSdtPr().getId() != null && ((SdtBlock) o2).getSdtPr().getId().getVal().equals(name)) {
					((ContentAccessor) ((SdtBlock) o2).getParent()).getContent().remove(o2);
					return;
				}
			}
		}

		// Pour les SdtRun
		ClassFinder finderSdtRun = new ClassFinder(SdtRun.class);
		new TraversalUtil(documentPart.getContent(), finderSdtRun);
		for (Object o : finderSdtRun.results) {
			Object o2 = XmlUtils.unwrap(o);
			if (o2 instanceof org.docx4j.wml.SdtRun) {
				if (((SdtRun) o2).getSdtPr().getTag() != null && ((SdtRun) o2).getSdtPr().getTag().getVal().equals(name)) {
					// Si le parent est un ArrayList, il s'agit de la racine Body
					if (((SdtRun) o2).getParent() instanceof ArrayList) {
						Body body = word.getMainDocumentPart().getJaxbElement().getBody();
						body.getEGBlockLevelElts().remove(o);
					} else {
						// Sinon nous supprimons depuis le parent du content control
						removeJAXBChild((ContentAccessor) ((SdtRun) o2).getParent(), o2);
					}
					return;
				} else if (((SdtRun) o2).getSdtPr().getId() != null && ((SdtRun) o2).getSdtPr().getId().getVal().equals(name)) {
					removeJAXBChild((ContentAccessor) ((SdtRun) o2).getParent(), o2);
					return;
				}
			}
		}

		// Pour les CTSdtCell
		ClassFinder finderCTSdtCell = new ClassFinder(CTSdtCell.class);
		new TraversalUtil(documentPart.getContent(), finderCTSdtCell);
		for (Object o : finderCTSdtCell.results) {
			Object o2 = XmlUtils.unwrap(o);
			if (o2 instanceof org.docx4j.wml.CTSdtCell) {
				if (((CTSdtCell) o2).getSdtPr().getTag() != null && ((CTSdtCell) o2).getSdtPr().getTag().getVal().equals(name)) {
					// Si le parent est un ArrayList, il s'agit de la racine Body
					if (((CTSdtCell) o2).getParent() instanceof ArrayList) {
						Body body = word.getMainDocumentPart().getJaxbElement().getBody();
						body.getEGBlockLevelElts().remove(o);
					} else {
						// Sinon nous supprimons depuis le parent du content control
						removeJAXBChild((ContentAccessor) ((CTSdtCell) o2).getParent(), o2);
					}
					return;
				} else if (((CTSdtCell) o2).getSdtPr().getId() != null && ((CTSdtCell) o2).getSdtPr().getId().getVal().equals(name)) {
					removeJAXBChild((ContentAccessor) ((CTSdtCell) o2).getParent(), o2);
					return;
				}
			}
		}

		// Pour les CTSdtRow
		ClassFinder finderCTSdtRow = new ClassFinder(CTSdtRow.class);
		new TraversalUtil(documentPart.getContent(), finderCTSdtRow);
		for (Object o : finderCTSdtRow.results) {
			Object o2 = XmlUtils.unwrap(o);
			if (o2 instanceof org.docx4j.wml.CTSdtRow) {
				if (((CTSdtRow) o2).getSdtPr().getTag() != null && ((CTSdtRow) o2).getSdtPr().getTag().getVal().equals(name)) {
					removeJAXBChild((ContentAccessor) ((CTSdtRow) o2).getParent(), o2);
					return;
				} else if (((CTSdtRow) o2).getSdtPr().getId() != null && ((CTSdtRow) o2).getSdtPr().getId().getVal().equals(name)) {
					removeJAXBChild((ContentAccessor) ((CTSdtRow) o2).getParent(), o2);
					return;
				}
			}
		}
	}

	/**
	 * Permet de supprimer un noeud JAXBElement à partir de sont parent
	 *
	 * @param parent Le noeud parent
	 * @param childToRemove Le noeud fils à supprimer
	 *
	 * @return boolean
	 */
	public static boolean removeJAXBChild(ContentAccessor parent, Object childToRemove) {

		for (Object o : parent.getContent()) {
			if (XmlUtils.unwrap(o).equals(childToRemove)) {
				return parent.getContent().remove(o);
			}
		}

		return false;
	}

	private List<Object> getAllCustomField(WordprocessingMLPackage wordMLPackage) {
		List<Object> result = getAllElementFromObject(wordMLPackage.getMainDocumentPart(), SdtElement.class);
		RelationshipsPart relationshipsPart = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
		for (org.docx4j.relationships.Relationship relationship : relationshipsPart.getRelationships().getRelationship()) {
			result.addAll(getAllElementFromObject(relationshipsPart.getPart(relationship), SdtElement.class));
		}
		return result;
	}

	private CustomXmlDataStoragePart createCustomXmlPart(WordprocessingMLPackage wordMLPackage) throws Docx4JException {
		CustomXmlDataStoragePart customXmlPart = new CustomXmlDataStoragePart();
		customXmlPart.setPackage(wordMLPackage);
		CustomXmlDataStorageImpl customXmlDataStorage = new CustomXmlDataStorageImpl();
		customXmlDataStorage.setPackage(wordMLPackage);
		customXmlPart.setData(customXmlDataStorage);
		customXmlDataStorage.setDocument(new ReaderInputStream(new StringReader("<root/>"), Charset.defaultCharset()));

		wordMLPackage.getMainDocumentPart().addTargetPart(customXmlPart, RelationshipsPart.AddPartBehaviour.RENAME_IF_NAME_EXISTS);
		CustomXmlDataStoragePropertiesPart part = new CustomXmlDataStoragePropertiesPart();

		DatastoreItem dsi = new org.docx4j.customXmlProperties.ObjectFactory().createDatastoreItem();
		String newItemId = "{" + UUID.randomUUID().toString() + "}";
		dsi.setItemID(newItemId);
		part.setJaxbElement(dsi);
		customXmlPart.addTargetPart(part);

		return customXmlPart;
	}

	/**
	 * Renvoie le texte contenu sur la ligne
	 *
	 * @param row La ligne
	 * @return Le texte contenu sur la ligne
	 */
	public static String getTextOnRow(Tr row) {
		if (row == null) {
			return "";
		}
		List<?> listeTextElement = getAllElementFromObject(row, Text.class);
		StringBuffer result = new StringBuffer();
		for (Object textElement : listeTextElement) {
			Text t = (Text) textElement;
			result.append(t.getValue());
		}
		return StringUtils.trimToEmpty(result.toString());
	}

	/**
	 * Repère la 1ère table qui contient une première ligne qui démarre par le texte de repérage
	 *
	 * @param wordMLPackage Objet principal de manipulation du .docx
	 * @param texte Le texte de repérage
	 * @return La 1ère table qui contient une première ligne qui démarre par le texte de repérage
	 */
	public static Tbl getTableWithFirstRowStartingWith(WordprocessingMLPackage wordMLPackage, String texte) {
		List<Tbl> result = getTableListWithFirstRowStartingWith(wordMLPackage, texte);
		return CollectionUtils.isNotEmpty(result) ? result.get(0) : null;
	}

	/**
	 * Repère toutes les tables qui contiennent une première ligne qui démarre par le texte de repérage
	 *
	 * @param wordMLPackage Objet principal de manipulation du .docx
	 * @param texte Le texte de repérage
	 * @return Liste des tables qui contiennent une première ligne qui démarre par le texte de repérage
	 */
	public static List<Tbl> getTableListWithFirstRowStartingWith(WordprocessingMLPackage wordMLPackage, String texte) {
		List<Tbl> result = new ArrayList<>();
		List<Object> listeTable = getAllElementFromObject(wordMLPackage.getMainDocumentPart(), Tbl.class);
		log.debug("nombre de tables dans le document : " + listeTable.size());
		for (Object table : listeTable) {
			List<Object> listeRow = getAllElementFromObject(table, Tr.class);
			if (listeRow.size() >= 1) {
				Tr firstRow = (Tr) listeRow.get(0);
				String rowContent = getTextOnRow(firstRow);
				String rowContentUpperCaseTrim = StringUtils.trimToEmpty(rowContent).toUpperCase();
				String texteUpperCaseTrim = StringUtils.trimToEmpty(texte).toUpperCase();
				log.debug("Compare rowContentUpperCaseTrim = " + rowContentUpperCaseTrim + " avec texte recherché = " + texteUpperCaseTrim);

				if (rowContentUpperCaseTrim.startsWith(texteUpperCaseTrim)) {
					result.add((Tbl) table);
				}
			}
		}
		return result;
	}

	/**
	 * Procède au remplissage (création de n lignes + suppression de la ligne "template")
	 *
	 * @param table La table à remplir
	 * @param tableFilling La table dynamique
	 * @throws Docx4JException en cas d'erreur
	 * @throws JAXBException en cas d'erreur
	 */
	protected static void fillTable(Tbl table, TableFilling tableFilling) throws Docx4JException, JAXBException {
		if (table == null || tableFilling == null) {
			return;
		}
		List<Object> rows = getAllElementFromObject(table, Tr.class);

		Tr titleRow = (Tr) rows.get(0);
		Tr templateRow = (Tr) rows.get(tableFilling.getTemplateRowIndex());

		for (Map<String, String> mapCodeValeur : tableFilling.getListeMapCodeValeur()) {
			log.debug("add row " + mapCodeValeur);
			addRowToTable(table, templateRow, mapCodeValeur, tableFilling.isKeepReplacementTextNotMatched());
		}

		if (tableFilling.isRemoveTitleRow()) {
			table.getContent().remove(titleRow);
		}

		fillTableImbrique(table, tableFilling, templateRow);

		// Remove the template row
		table.getContent().remove(templateRow);
	}

	/**
	 * Procède au remplissage d'un sous-tableau si celui-ci existe (création de n lignes + suppression de la ligne "template")
	 *
	 * @param sousTable La sous table à remplir
	 * @param tableFilling Objet de remplissage
	 * @param templateRow ligne modèle
	 * @throws Docx4JException en cas d'erreur
	 * @throws JAXBException en cas d'erreur
	 */
	private static void fillTableImbrique(Tbl sousTable, TableFilling tableFilling, Tr templateRow) {
		if (tableFilling.listeMapCodeValeurTableImbrique.isEmpty()) {
			return;
		}

		Tr workingRow = null;
		Tbl tableImbrique = null;
		Tr templateRowTableImbrique = null;

		for (Map<String, String> mapCodeValeurTableImbrique : tableFilling.getListeMapCodeValeurTableImbrique()) {
			if (workingRow == null || mapCodeValeurTableImbrique.get(NOUVELLE_LIGNE_TABLEAU_IMBRIQUE) != null) {
				// Si on entre pour la première fois dans la méthode ou qu'on est sur une nouvelle ligne alors on initialise une nouvelle ligne
				// qui va contenir une copie du tableau imbriqué
				if (tableImbrique != null) {
					tableImbrique.getContent().remove(templateRowTableImbrique);
				}
				workingRow = XmlUtils.deepCopy(templateRow);
				sousTable.getContent().add(workingRow); // On ajoute au tableau principal une nouvelle ligne qui contient une copie du tableau imbriqué
				List<Object> listeTableImbrique = getAllElementFromObject(workingRow, Tbl.class);
				tableImbrique = (Tbl) listeTableImbrique.get(0);
				List<Object> rowsTableImbrique = getAllElementFromObject(tableImbrique, Tr.class);
				templateRowTableImbrique = (Tr) rowsTableImbrique.get(0);
			}

			addRowToTable(tableImbrique, templateRowTableImbrique, mapCodeValeurTableImbrique, tableFilling.isKeepReplacementTextNotMatched());
		}

		tableImbrique.getContent().remove(templateRowTableImbrique);
	}

	/**
	 * Ajoute une ligne à une table. Inspiré de http://www.smartjava.org/content/create-complex-word-docx-documents-programatically-docx4j
	 *
	 * @param reviewtable table concernée
	 * @param templateRow ligne modèle
	 * @param replacements Map de remplacement "texte à rechercher (sur la ligne modèle)" vers "valeur de substitution"
	 * @param keepTextNotMatched flag qui indique s'il faut conserver ou non les élémentsd de la ligne modèle qui ne matchent pas la map replacements
	 */
	protected static void addRowToTable(Tbl reviewtable, Tr templateRow, Map<String, String> replacements, boolean keepTextNotMatched) {
		if (reviewtable == null || templateRow == null) {
			return;
		}
		Tr workingRow = XmlUtils.deepCopy(templateRow);
		List<?> textElements = getAllElementFromObject(workingRow, Text.class);
		log.debug("addRowToTable, textElements = " + textElements + ", replacements = " + replacements);
		for (Object object : textElements) {
			Text text = (Text) object;
			String textValue = StringUtils.trimToEmpty(text.getValue());
			String replacementValue = replacements.get(textValue);
			if (replacementValue == null && keepTextNotMatched) {
				continue; // tableFilling demande à conserver le texte qui ne correspond à rien dans replacements
			}
			text.setValue(replacementValue == null ? "" : replacementValue);
		}

		reviewtable.getContent().add(workingRow);
	}

	/**
	 * Modifié à partir de cf. http://www.smartjava.org/content/create-complex-word-docx-documents-programatically-docx4j
	 *
	 * @param obj Objet ancêtre concerné par la recherche de ses descendants
	 * @param toSearch classe des objets descendants sur laquelle on recherche
	 * @return une liste d'objets docx4j descendants de l'objet obj, et de la classe toSearch (tables, textes, etc.)
	 */
	public static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
		List<Object> result = new ArrayList<Object>();
		if (obj instanceof JAXBElement) {
			obj = ((JAXBElement<?>) obj).getValue();
		}

		if (obj != null && toSearch.isAssignableFrom(obj.getClass())) {
			result.add(obj);
		}

		if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}

		}
		return result;
	}

	protected static void updateXmlTag(CustomXmlPart customXmlPart, String tagXml, String valeur) throws Docx4JException {
		String xmlns = null; // syntaxe possible : "xmlns:ns0='http://your.namespace'"
		customXmlPart.setNodeValueAtXPath("/root/" + tagXml, valeur, xmlns);
	}

	/**
	 * Coche/décoche une case à cocher du document, soulève une exception si la case n'est pas trouvée.
	 *
	 * @param mainDocumentPart partie principale du docx
	 * @param nomCheckBox nom de la checkbox
	 * @param checked à cocher / ne pas cocher
	 */
	protected static void updateCheckBox(MainDocumentPart mainDocumentPart, String nomCheckBox, boolean checked) {
		try {
			String xpath = "//w:ffData/w:checkBox/w:default[../../w:name[@w:val='" + nomCheckBox + "']]";
			List<Object> list = mainDocumentPart.getJAXBNodesViaXPath(xpath, false);
			if (list.isEmpty()) {
				log.warn("checkbox " + nomCheckBox + " non trouvée dans document");
				return;
			}
			BooleanDefaultTrue element = (BooleanDefaultTrue) list.get(0);
			element.setVal(checked);
		} catch (Exception e) {
			log.warn("anomalie dans updateCheckBox (peut arriver si la checkbox n'existe pas)");
		}
	}
}
