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

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.Locale;

public class DateUtil {
	private static final DateTimeFormatter	YEAR_FORMATTER						= dateTimeFormatterNCForPattern("yyyy");
	private static final DateTimeFormatter	SHORT_YEAR_FORMATTER				= dateTimeFormatterNCForPattern("yy");
	private static final DateTimeFormatter	TIME_FORMATTER						= dateTimeFormatterNCForPattern("HH:mm");
	private static final DateTimeFormatter	TIME_FORMATTER_H					= dateTimeFormatterNCForPattern("HH'h'mm");
	private static final DateTimeFormatter	DATE_FORMATTER_MM_YYYY				= dateTimeFormatterNCForPattern("MM/yyyy");
	private static final DateTimeFormatter	DATE_FORMATTER						= dateTimeFormatterNCForPattern("dd/MM/yyyy");
	private static final DateTimeFormatter	DATE_FORMATTER_CITYWEB				= dateTimeFormatterNCForPattern("yyyyMMdd");
	private static final DateTimeFormatter	DATE_FORMATTER_YY					= dateTimeFormatterNCForPattern("dd/MM/yy");
	private static final DateTimeFormatter	DATETIME_FORMATTER					= dateTimeFormatterNCForPattern("dd/MM/yyyy HH:mm");
	private static final DateTimeFormatter	DATETIME_FORMATTER_EDITIQUE			= dateTimeFormatterNCForPattern("dd_MM_yyyy_HH_mm");
	private static final DateTimeFormatter	DATETIME_FORMATTER_FRIENDLY			= dateTimeFormatterNCForPattern("dd/MM/yyyy 'à' HH'h'mm");
	private static final DateTimeFormatter	DATETIME_FORMATTER_FRIENDLY_DE		= dateTimeFormatterNCForPattern("dd/MM/yyyy 'de' HH'h'mm");
	private static final DateTimeFormatter	DATETIME_HEURE_FORMATTER_FRIENDLY	= dateTimeFormatterNCForPattern("' à' HH'h'mm");

	public static final String				TIMEZONE_NOUMEA						= "Pacific/Noumea";

	private static DateTimeFormatter dateTimeFormatterNCForPattern(String pattern) {
		return DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID(TIMEZONE_NOUMEA));
	}

	public static String formatDateGeneric(DateTimeFormatter dateTimeFormatter, Date date) {
		return date == null || dateTimeFormatter == null ? "" : dateTimeFormatter.print(new DateTime(date));
	}

	public static String formatDateEnLettre(Date date) {
		StringBuilder result = new StringBuilder();
		DateTime dateTime = new DateTime(date);
		result.append(dateTime.dayOfWeek().getAsText(Locale.FRANCE));
		result.append(" ");
		result.append(FormatUtil.convertNombreEnLettre(dateTime.getDayOfMonth()));
		result.append(" ");
		result.append(libelleMois(dateTime.getMonthOfYear()));
		result.append(" ");
		result.append(FormatUtil.convertNombreEnLettre(dateTime.getYear()));
		return result.toString();
	}

	public static String formatTimeEnLettre(Date date) {
		StringBuilder result = new StringBuilder();
		DateTime dateTime = new DateTime(date);
		result.append(FormatUtil.convertNombreEnLettre(dateTime.getHourOfDay()));
		result.append(" heure" + (dateTime.getHourOfDay() > 1 ? "s" : ""));
		if (dateTime.getMinuteOfHour() > 0) {
			result.append(" ");
			result.append(FormatUtil.convertNombreEnLettre(dateTime.getMinuteOfHour()));
			result.append(String.format("%d", dateTime.getMinuteOfHour()).endsWith("1") ? "e" : "");
			result.append(" minute" + (dateTime.getMinuteOfHour() > 1 ? "s" : ""));
		}
		return result.toString();
	}

	public static String formatDateTimeEnLettre(Date date) {
		StringBuilder result = new StringBuilder();
		result.append(formatDateEnLettre(date));
		result.append(" à ");
		result.append(formatTimeEnLettre(date));
		return result.toString();
	}

	public static String formatDateMoisAnnee(Date date) {
		return formatDateGeneric(DATE_FORMATTER_MM_YYYY, date);
	}

	/**
	 * Retourne la date représentée au format dd/MM/yy, ex : 31/12/16
	 *
	 * @param date date
	 * @return une représentation de la date en paramètre, ou "" si date est null
	 */
	public static String formatDateAnneeYY(Date date) {
		return formatDateGeneric(DATE_FORMATTER_YY, date);
	}

	/**
	 * Retourne la date représentée au format dd/MM/yyyy, ex : 31/12/2013
	 *
	 * @param date date
	 * @return une représentation de la date en paramètre, ou "" si date est null
	 */
	public static String formatDate(Date date) {
		return formatDateGeneric(DATE_FORMATTER, date);
	}

	public static String formatDateCityWeb(Date date) {
		return formatDateGeneric(DATE_FORMATTER_CITYWEB, date);
	}

	/**
	 * Retourne l'année d'une date
	 *
	 * @param date date
	 * @return l'année d'une date
	 */
	public static String formatAnnee(Date date) {
		return formatDateGeneric(YEAR_FORMATTER, date);
	}

	/**
	 * Retourne la partie heure d'une date au format HH:mm, ex : "23:59"
	 *
	 * @param date date
	 * @return une représentation de la partie heure de la date en paramètre, ou "" si date est null
	 */
	public static String formatHeureMinute(Date date) {
		return formatDateGeneric(TIME_FORMATTER, date);
	}

	public static String formatHeureMinuteH(Date date) {
		return formatDateGeneric(TIME_FORMATTER_H, date);
	}

	public static String formatDateHeureDebutFinAvecGestionMemeJour(Date dateDebut, Date dateFin) {
		boolean memeJour = new DateTime(dateDebut).getDayOfYear() == new DateTime(dateFin).getDayOfYear();
		if (memeJour) {
			return "Le " + formatDateTimeFriendlyDe(dateDebut) + " à " + DateUtil.formatHeureMinuteH(dateFin);
		} else {
			return "Du " + formatDate(dateDebut) + " " + formatHeureMinuteH(dateDebut) + " au " + formatDate(dateFin) + " " + formatHeureMinuteH(dateFin);
		}
	}

	/**
	 * Retourne la date/heure représentée au format dd/MM/yyyy HH:mm, ex : 31/12/2013 23:59
	 *
	 * @param date date
	 * @return une représentation de la date en paramètre, ou "" si date est null
	 */
	public static String formatDateTime(Date date) {
		return formatDateGeneric(DATETIME_FORMATTER, date);
	}

	/**
	 * Retourne la date/heure représentée au format dd_MM_yyyy_HH_mm, ex : 31_12_2013_23_59
	 *
	 * @param date date
	 * @return une représentation de la date en paramètre, ou "" si date est null
	 */
	public static String formatDateTimeEditique(Date date) {
		return formatDateGeneric(DATETIME_FORMATTER_EDITIQUE, date);
	}

	/**
	 * Retourne la date/heure représentée au format dd/MM/yyyy à HH'h'mm, ex : 31/12/2013 à 23h59
	 *
	 * @param date date
	 * @return une représentation de la date en paramètre, ou "" si date est null
	 */
	public static String formatDateTimeFriendly(Date date) {
		return formatDateGeneric(DATETIME_FORMATTER_FRIENDLY, date);
	}

	public static String formatDateTimeFriendlyDe(Date date) {
		return formatDateGeneric(DATETIME_FORMATTER_FRIENDLY_DE, date);
	}

	public static String formatDateTimeFriendlyMoisEnTexte(Date date) {
		return formatDateAvecMoisEnTexte(date) + formatDateGeneric(DATETIME_HEURE_FORMATTER_FRIENDLY, date);
	}

	/**
	 * Retoure une représentation de la date
	 *
	 * @param date date concernée
	 * @return exemple : "7 janvier 2014", "" si la date en entrée est null
	 */
	public static String formatDateAvecMoisEnTexte(Date date) {
		if (date == null) {
			return "";
		}
		DateTime dateTime = new DateTime(date);
		return dateTime.getDayOfMonth() + " " + libelleMois(dateTime.getMonthOfYear()) + " " + dateTime.getYear();
	}

	/**
	 * Retoure une représentation de la date
	 *
	 * @param date date concernée
	 * @return exemple : "Lundi 7 janvier 2014", "" si la date en entrée est null
	 */
	public static String formatDateAvecJourMoisEnTexte(Date date) {
		if (date == null) {
			return "";
		}
		DateTime dateTime = new DateTime(date);
		return dateTime.dayOfWeek().getAsText(Locale.FRANCE) + " " + dateTime.getDayOfMonth() + " " + libelleMois(dateTime.getMonthOfYear()) + " "
				+ dateTime.getYear();
	}

	/**
	 * Retourne une chaîne d'horodatage de la date courante, pratique pour suffixer le nom des fichiers générés notamment.
	 *
	 * @return horodatage
	 */
	public static String getHorodatage() {
		return DateTime.now().toString("YYYYMMddHHmmss"); // #5784
	}

	public static DateTime debutJournee(DateTime d) {
		return d.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
	}

	public static Date debutJournee(Date d) {
		return debutJournee(new DateTime(d)).toDate();
	}

	public static DateTime debutMois(DateTime d) {
		return d.dayOfMonth().withMinimumValue().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
	}

	public static DateTime finMois(DateTime d) {
		return d.dayOfMonth().withMaximumValue().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(59);
	}

	public static Date debutMois(Date d) {
		return debutMois(new DateTime(d)).toDate();
	}

	public static Date finMois(Date d) {
		return finMois(new DateTime(d)).toDate();
	}

	public static DateTime aujourdhui00h00DateTime() {
		return debutJournee(new DateTime());
	}

	public static Date aujourdhui00h00() {
		return aujourdhui00h00DateTime().toDate();
	}

	public static Date parseDateTime(String str) {
		if (str == null) {
			return null;
		}
		return DATETIME_FORMATTER.parseDateTime(str).toDate();
	}

	public static Date parseDate(String str) {
		if (str == null) {
			return null;
		}
		return DATE_FORMATTER.parseDateTime(str).toDate();
	}

	public static Date parseDateCityWeb(String str) {
		if (str == null) {
			return null;
		}
		return DATE_FORMATTER_CITYWEB.parseDateTime(str).toDate();
	}

	/**
	 * Indique si date 1 est supérieur à date 2 (et les 2 non null).
	 *
	 * @param date1 date qu'on souhaite comparer avec date2
	 * @param date2 date comparée
	 * @return true si date1 et date2 non null, et date1 supérieur date2
	 */
	public static boolean isNotNullAndAfter(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return date1.after(date2);
	}

	public static boolean isNotNullAndAfterOrEquals(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return false;
		}
		return date1.after(date2) || date1.equals(date2);
	}

	/**
	 * Indique si date 1 est supérieur à date 2 + nombreMois mois (et les 2 dates non null).
	 *
	 * @param date1 date qu'on souhaite comparer avec date2
	 * @param date2 date comparée
	 * @param nombreMois nombreMois
	 * @return true si date1 et date2 non null, et date1 supérieur à date2 + nombreMois mois
	 */
	public static boolean isNotNullAndAfterNMonth(Date date1, Date date2, int nombreMois) {
		if (date1 == null || date2 == null) {
			return false;
		}
		DateTime dateTime1 = new DateTime(date1);
		DateTime dateTime2 = new DateTime(date2);
		return dateTime1.isAfter(dateTime2.plusMonths(nombreMois));
	}

	private static final String[] LISTE_MOIS = new String[] { "janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre",
			"novembre", "décembre" };

	/**
	 * @param monthOfYear entre 1 et 12
	 * @return le libellé du mois correspondant à la date
	 */
	public static String libelleMois(int monthOfYear) {
		if (monthOfYear < 1 || monthOfYear > 12) {
			throw new IllegalArgumentException("mois en dehors de l'intervalle [1;12] : " + monthOfYear);
		}
		return LISTE_MOIS[monthOfYear - 1];
	}

	public static int compare(Date date1, Date date2) {
		if (date1 == null && date2 == null) {
			return 0; // null==null
		}

		if (date1 != null && date2 != null) {
			return date1.compareTo(date2);
		}

		if (date1 == null) {
			return -1; // null < date2
		}
		return 1; // date1 < null
	}

	public static int calculAgeEnAnnee(Date dateNaissance) {
		if (dateNaissance == null) {
			return 0;
		}
		return Years.yearsBetween(new DateTime(dateNaissance), new DateTime()).getYears();
	}

	public static int jourEntreDeuxDate(Date dateDebut, Date dateFin) {
		return (int) Math.ceil(new Double(heureEntreDeuxDate(dateDebut, dateFin)) / new Double(24));
	}

	public static int heureEnJour(double heure) {
		return (int) Math.ceil(new Double(heure) / new Double(24));
	}

	public static double heureEntreDeuxDate(Date dateDebut, Date dateFin) {
		if (dateDebut == null || dateFin == null) {
			return 0;
		}

		return ((double) dateFin.getTime() - (double) dateDebut.getTime()) / DateTimeConstants.MILLIS_PER_HOUR;
	}

	public static boolean isDateBeforeXAnnee(Date date, Integer nombreAnnee) {
		if (date == null || nombreAnnee == null) {
			return false;
		}

		Date nowPlusXAnnee = new DateTime(new Date()).plusYears(nombreAnnee).toDate();
		return DateTimeComparator.getDateOnlyInstance().compare(date, nowPlusXAnnee) < 0;
	}
}
