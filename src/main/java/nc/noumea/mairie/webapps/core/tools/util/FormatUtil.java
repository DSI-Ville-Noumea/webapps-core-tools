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


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.ibm.icu.text.RuleBasedNumberFormat;

public class FormatUtil {
	private static final NumberFormat	FORMATTER_MONTANT											= construitFormatterXpf("#,##0.##");
	private static final NumberFormat	FORMATTER_XPF												= construitFormatterXpf("#,##0.## XPF");
	private static final NumberFormat	FORMATTER_SEPARATEUR_MILLIER_DEUX_CHIFFRES_APRES_VIRGULE	= construitFormatterXpf("###,##0.00");

	public static String capitalizeFullyFrench(String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		return WordUtils.capitalizeFully(str, new char[] { '-', ' ' });
	}

	/**
	 * Convertit la chaine en entrée en majuscule sans accent, et sans blanc devant/derrière.
	 *
	 * @param str chaîne concernée
	 * @return "" si la chaine en entrée est null
	 */
	public static String majusculeSansAccentTrim(String str) {
		if (str == null) {
			return "";
		}
		String s = StringUtils.trimToEmpty(str).toUpperCase();
		s = s.replaceAll("[ÀÂÄ]", "A");
		s = s.replaceAll("[ÈÉÊË]", "E");
		s = s.replaceAll("[ÏÎ]", "I");
		s = s.replaceAll("Ô", "O");
		s = s.replaceAll("[ÛÙÜ]", "U");
		s = s.replaceAll("Ç", "C");
		return s;
	}

	public static String majusculeSansAccentTrimToNull(String str) {
		return StringUtils.isBlank(str) ? null : majusculeSansAccentTrim(str);
	}

	/**
	 * Construit un texte tronqué s'il dépasse la longueur du texte indiquée
	 *
	 * @param texte texte
	 * @param longueurMaxTexte longueur max.
	 * @param suffixeSiTronque suffixe à utiliser en fin de résultat si le texte dépasse la longueur max.
	 * @return texte tronqué
	 */
	public static String construitTexteTronque(String texte, int longueurMaxTexte, String suffixeSiTronque) {
		if (texte == null) {
			return null;
		}
		if (StringUtils.isBlank(texte)) {
			return "";
		}
		String result = StringUtils.trimToEmpty(texte);
		if (result.length() <= longueurMaxTexte) {
			return result;
		}
		return StringUtils.left(result, longueurMaxTexte) + ((suffixeSiTronque == null) ? "" : suffixeSiTronque);
	}

	public static String creerReprLigne(String... listeElement) {
		List<String> listeElementRetenu = new ArrayList<>();
		for (String element : listeElement) {
			if (!StringUtils.isBlank(element)) {
				listeElementRetenu.add(StringUtils.trimToEmpty(element));
			}
		}
		return StringUtils.join(listeElementRetenu, " ");
	}

	private static DecimalFormat construitFormatterXpf(String formatXpf) {
		DecimalFormat formatter = new DecimalFormat(formatXpf);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		formatter.setDecimalFormatSymbols(symbols);
		return formatter;
	}

	public synchronized static String representationMontantSansXpf(int montant) {
		return FORMATTER_MONTANT.format(montant);
	}

	public synchronized static String representationXpf(int montant) {
		return FORMATTER_XPF.format(montant);
	}

	public static String formatteAvec2ChiffreApresVirgule(Double valeur, boolean separateurMillier) {
		if (valeur == null) {
			return "";
		}
		if (separateurMillier) {
			return FORMATTER_SEPARATEUR_MILLIER_DEUX_CHIFFRES_APRES_VIRGULE.format(valeur);
		}
		return new DecimalFormat("0.00").format(valeur).replace(".", ",");
	}

	public static String paddingZeroAGaucheSaufSiVide(String str, int nombreChiffre) {
		return StringUtils.isBlank(str) ? null : StringUtils.leftPad(str, nombreChiffre, "0");
	}

	public static String convertNombreEnLettre(int nombre) {
		RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(Locale.FRANCE, RuleBasedNumberFormat.SPELLOUT);
		return ruleBasedNumberFormat.format(nombre);
	}

	public static String convertirXpfEuros(int montantXpf) {
		return String.format("%.2f", new Double(montantXpf) * new Double(0.00838));
	}

	public static String construitOrdinal(String numero, boolean feminin) {
		if (numero.equals("1")) {
			return feminin ? "1ère" : "1er";
		} else {
			return numero + "ème";
		}
	}

	/**
	 * Arrondi un nombre à la dizaine
	 *
	 * @param nombre le nombre à arrondir
	 * @return le nombre arrondi à la dizaine
	 */
	public static int arrondiDizaine(double nombre) {
		int ratio = (int) (nombre * 100);
		if (ratio == 0) {
			return 0;
		}

		return ((ratio + 9) / 10 * 10);
	}

	public static String replaceAllSpecialCharacter(String texte) {
		if (texte == null) {
			return null;
		}

		return texte.replaceAll("[^a-zA-Z0-9.-]", "_");
	}

	public static boolean isMinimumXCaractere(String value, int nombreCar) {
		if (nombreCar <= 0) {
			return true;
		}

		if (StringUtils.isBlank(value) && nombreCar > 0) {
			return false;
		}

		return value.length() >= nombreCar;
	}

	public static String getCharForNumber(int i) {
		return i > 0 && i < 27 ? String.valueOf((char) (i + 64)) : null;
	}
}
