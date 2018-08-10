package nc.noumea.mairie.webapps.core.tools.util;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

public class DateUtilTest {

	@Test
	public void debutJournee() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.debutJournee(date)), "31/12/2013 00:00");

		// avec des ms cette fois
		Assert.assertTrue(DateUtil.formatDateTime(DateUtil.debutJournee(new Date())).endsWith(" 00:00"));

		// variante avec DateTime
		Assert.assertTrue(DateUtil.formatDateTime(DateUtil.debutJournee(new DateTime()).toDate()).endsWith(" 00:00"));
	}

	@Test
	public void formatDateMoisAnnee() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateMoisAnnee(date), "12/2013");

		date = DateUtil.parseDate("01/10/2013");
		Assert.assertEquals(DateUtil.formatDateMoisAnnee(date), "10/2013");
	}

	@Test
	public void formatDateCityWeb() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateCityWeb(date), "20131231");

		date = DateUtil.parseDate("01/10/2013");
		Assert.assertEquals(DateUtil.formatDateCityWeb(date), "20131001");
	}

	@Test
	public void formatDateTimeFriendlyMoisEnTexte() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateTimeFriendlyMoisEnTexte(date), "31 décembre 2013 à 23h15");

		date = DateUtil.parseDate("01/02/2013");
		Assert.assertEquals(DateUtil.formatDateTimeFriendlyMoisEnTexte(date), "1 février 2013 à 00h00");
	}

	@Test
	public void heureEnJour() {
		Assert.assertEquals(DateUtil.heureEnJour(1), 1);
		Assert.assertEquals(DateUtil.heureEnJour(24), 1);
		Assert.assertEquals(DateUtil.heureEnJour(25), 2);
	}

	@Test
	public void formatAnnee() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatAnnee(date), "2013");

		date = DateUtil.parseDate("01/10/2012");
		Assert.assertEquals(DateUtil.formatAnnee(date), "2012");
	}

	@Test
	public void debutMois() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.debutMois(date)), "01/12/2013 00:00");

		date = DateUtil.parseDateTime("31/12/2013 00:00");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.debutMois(date)), "01/12/2013 00:00");

		date = DateUtil.parseDateTime("01/12/2013 12:00");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.debutMois(date)), "01/12/2013 00:00");
	}

	@Test
	public void finMois() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.finMois(date)), "31/12/2013 23:59");

		date = DateUtil.parseDateTime("31/12/2013 00:00");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.finMois(date)), "31/12/2013 23:59");

		date = DateUtil.parseDateTime("01/12/2013 12:00");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.finMois(date)), "31/12/2013 23:59");

		date = DateUtil.parseDateTime("02/02/2013 23:15");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.finMois(date)), "28/02/2013 23:59");

		date = DateUtil.parseDateTime("11/04/2013 12:00");
		Assert.assertEquals(DateUtil.formatDateTime(DateUtil.finMois(date)), "30/04/2013 23:59");
	}

	@Test
	public void aujourdhui00h00() {
		Assert.assertTrue(DateUtil.formatDateTime(DateUtil.aujourdhui00h00()).endsWith(" 00:00"));
	}

	@Test
	public void formatDateGeneric() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy").withZone(DateTimeZone.forID(DateUtil.TIMEZONE_NOUMEA));

		Assert.assertEquals("", DateUtil.formatDateGeneric(null, null));
		Assert.assertEquals("", DateUtil.formatDateGeneric(null, new Date()));
		Assert.assertEquals("", DateUtil.formatDateGeneric(dateTimeFormatter, null));

		Date date = DateUtil.parseDateTime("31/12/2013 23:59");
		Assert.assertEquals(DateUtil.formatDateGeneric(dateTimeFormatter, date), "2013");
	}

	@Test
	public void formatDateAnneeYY() {
		Date date = DateUtil.parseDateTime("31/12/2013 23:59");
		Assert.assertEquals(DateUtil.formatDateAnneeYY(date), "31/12/13");
	}

	@Test
	public void formatDate() {
		Assert.assertEquals(DateUtil.formatDate(null), "");

		Date date = DateUtil.parseDateTime("31/12/2013 23:59");
		Assert.assertEquals(DateUtil.formatDate(date), "31/12/2013"); // heures/minutes non affichées
	}

	@Test
	public void formatDateTime() {
		Assert.assertEquals(DateUtil.formatDateTime(null), "");

		Date date = DateUtil.parseDateTime("31/12/2013 23:59");
		Assert.assertEquals(DateUtil.formatDateTime(date), "31/12/2013 23:59");
	}

	@Test
	public void formatTime() {
		Assert.assertEquals(DateUtil.formatHeureMinute(null), "");
		Date date = DateUtil.parseDateTime("31/12/2013 23:59");
		Assert.assertEquals(DateUtil.formatHeureMinute(date), "23:59");
	}

	@Test
	public void parseDate() {
		Assert.assertNull(DateUtil.parseDate(null));

		Date date = DateUtil.parseDate("31/12/2013");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Assert.assertEquals(calendar.get(Calendar.YEAR), 2013);
		Assert.assertEquals(calendar.get(Calendar.MONTH), 11); // 11 = décembre
		Assert.assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 31);
		Assert.assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 0);
		Assert.assertEquals(calendar.get(Calendar.MINUTE), 0);
		Assert.assertEquals(calendar.get(Calendar.SECOND), 0);
		Assert.assertEquals(calendar.get(Calendar.MILLISECOND), 0);
	}

	@Test
	public void parseDateTime() {
		Assert.assertNull(DateUtil.parseDateTime(null));

		Date date = DateUtil.parseDateTime("31/12/2013 23:59");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Assert.assertEquals(calendar.get(Calendar.YEAR), 2013);
		Assert.assertEquals(calendar.get(Calendar.MONTH), 11); // 11 = décembre
		Assert.assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 31);
		Assert.assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 23);
		Assert.assertEquals(calendar.get(Calendar.MINUTE), 59);
		Assert.assertEquals(calendar.get(Calendar.SECOND), 0);
		Assert.assertEquals(calendar.get(Calendar.MILLISECOND), 0);
	}

	@Test
	public void isNotNullAndAfter() {
		Assert.assertTrue(DateUtil.isNotNullAndAfter(DateUtil.parseDate("15/01/2014"), DateUtil.parseDate("14/01/2014")));

		Assert.assertFalse(DateUtil.isNotNullAndAfter(DateUtil.parseDate("14/01/2014"), DateUtil.parseDate("15/01/2014")));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(DateUtil.parseDate("15/01/2014"), DateUtil.parseDate("15/01/2014")));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(null, null));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(null, new Date()));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(new Date(), null));
	}

	@Test
	public void isNotNullAndAfterAndEquals() {
		Assert.assertTrue(DateUtil.isNotNullAndAfterOrEquals(DateUtil.parseDate("15/01/2014"), DateUtil.parseDate("14/01/2014")));
		Assert.assertTrue(DateUtil.isNotNullAndAfterOrEquals(DateUtil.parseDate("15/01/2014"), DateUtil.parseDate("15/01/2014")));
		Assert.assertFalse(DateUtil.isNotNullAndAfterOrEquals(null, null));
		Assert.assertFalse(DateUtil.isNotNullAndAfterOrEquals(null, new Date()));
		Assert.assertFalse(DateUtil.isNotNullAndAfterOrEquals(new Date(), null));

		Assert.assertFalse(DateUtil.isNotNullAndAfter(DateUtil.parseDate("14/01/2014"), DateUtil.parseDate("15/01/2014")));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(null, null));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(null, new Date()));
		Assert.assertFalse(DateUtil.isNotNullAndAfter(new Date(), null));
	}

	@Test
	public void isNotNullAndAfterNMonth() {
		Assert.assertFalse(DateUtil.isNotNullAndAfterNMonth(null, null, 0));
		Assert.assertFalse(DateUtil.isNotNullAndAfterNMonth(null, DateUtil.parseDate("01/01/2015"), 0));
		Assert.assertFalse(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("01/01/2015"), null, 0));

		Assert.assertTrue(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("15/01/2014"), DateUtil.parseDate("14/01/2014"), 0));
		Assert.assertFalse(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("15/01/2014"), DateUtil.parseDate("14/01/2014"), 1));

		Assert.assertTrue(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("15/02/2014"), DateUtil.parseDate("14/01/2014"), 1));
		Assert.assertFalse(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("15/02/2014"), DateUtil.parseDate("14/01/2014"), 2));

		Assert.assertTrue(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("15/01/2016"), DateUtil.parseDate("14/01/2014"), 24));
		Assert.assertFalse(DateUtil.isNotNullAndAfterNMonth(DateUtil.parseDate("14/01/2016"), DateUtil.parseDate("14/01/2014"), 24));
	}

	@Test
	public void libelleMois() {
		Assert.assertEquals(DateUtil.libelleMois(1), "janvier");
		Assert.assertEquals(DateUtil.libelleMois(12), "décembre");
		Assert.assertEquals(DateUtil.libelleMois(9), "septembre");
	}

	@Test
	public void formatDateAvecMoisEnTexte() {
		Assert.assertEquals(DateUtil.formatDateAvecMoisEnTexte(null), "");
		Assert.assertEquals(DateUtil.formatDateAvecMoisEnTexte(DateUtil.parseDate("28/02/1965")), "28 février 1965");
		Assert.assertEquals(DateUtil.formatDateAvecMoisEnTexte(DateUtil.parseDate("01/01/2014")), "1 janvier 2014");
		Assert.assertEquals(DateUtil.formatDateAvecMoisEnTexte(DateUtil.parseDate("31/12/2015")), "31 décembre 2015");
	}

	@Test
	public void formatDateAvecJourMoisEnTexte() {
		Assert.assertEquals(DateUtil.formatDateAvecJourMoisEnTexte(null), "");
		Assert.assertEquals(DateUtil.formatDateAvecJourMoisEnTexte(DateUtil.parseDate("28/02/2018")), "Mercredi 28 février 2018");
		Assert.assertEquals(DateUtil.formatDateAvecJourMoisEnTexte(DateUtil.parseDate("01/01/2018")), "Lundi 1 janvier 2018");
		Assert.assertEquals(DateUtil.formatDateAvecJourMoisEnTexte(DateUtil.parseDate("31/12/2018")), "Lundi 31 décembre 2018");
	}

	@Test
	public void formatDateTimeFriendly() {
		Assert.assertEquals(DateUtil.formatDateTimeFriendly(null), "");
		Assert.assertEquals(DateUtil.formatDateTimeFriendly(DateUtil.parseDateTime("28/02/1965 12:25")), "28/02/1965 à 12h25");
		Assert.assertEquals(DateUtil.formatDateTimeFriendly(DateUtil.parseDateTime("01/01/2014 23:59")), "01/01/2014 à 23h59");
		Assert.assertEquals(DateUtil.formatDateTimeFriendly(DateUtil.parseDateTime("31/12/2015 00:00")), "31/12/2015 à 00h00");
	}

	@Test
	public void testCompare() {
		Date date1 = DateUtil.parseDate("01/03/2015");
		Date date2 = DateUtil.parseDate("02/03/2015");
		Assert.assertEquals(0, DateUtil.compare(date1, date1));
		Assert.assertEquals(0, DateUtil.compare(null, null));
		Assert.assertEquals(1, DateUtil.compare(date2, date1));
		Assert.assertEquals(-1, DateUtil.compare(date1, date2));
		Assert.assertEquals(-1, DateUtil.compare(null, date1));
		Assert.assertEquals(1, DateUtil.compare(date1, null));
	}

	@Test
	public void formatDateTimeEditique() {
		Assert.assertEquals(DateUtil.formatDateTimeEditique(null), "");
		Assert.assertEquals(DateUtil.formatDateTimeEditique(DateUtil.parseDateTime("28/02/1965 12:25")), "28_02_1965_12_25");
		Assert.assertEquals(DateUtil.formatDateTimeEditique(DateUtil.parseDateTime("01/01/2014 23:59")), "01_01_2014_23_59");
		Assert.assertEquals(DateUtil.formatDateTimeEditique(DateUtil.parseDateTime("31/12/2015 00:00")), "31_12_2015_00_00");
	}

	@Test
	public void heureEntreDeuxDate() {
		Assert.assertEquals(DateUtil.heureEntreDeuxDate(null, null), 0, 0);
		Assert.assertEquals(DateUtil.heureEntreDeuxDate(new Date(), null), 0, 0);
		Assert.assertEquals(DateUtil.heureEntreDeuxDate(null, new Date()), 0, 0);

		Assert.assertEquals(DateUtil.heureEntreDeuxDate(DateUtil.parseDateTime("01/01/2017 12:00"), DateUtil.parseDateTime("01/01/2017 22:00")), 10, 0);
		Assert.assertEquals(DateUtil.heureEntreDeuxDate(DateUtil.parseDateTime("01/01/2017 12:00"), DateUtil.parseDateTime("02/01/2017 12:00")), 24, 0);
		Assert.assertEquals(DateUtil.heureEntreDeuxDate(DateUtil.parseDateTime("01/01/2017 12:00"), DateUtil.parseDateTime("02/01/2017 11:01")), 23.02, 2);
	}

	@Test
	public void jourEntreDeuxDate() {
		Assert.assertEquals(DateUtil.jourEntreDeuxDate(null, null), 0);
		Assert.assertEquals(DateUtil.jourEntreDeuxDate(new Date(), null), 0);
		Assert.assertEquals(DateUtil.jourEntreDeuxDate(null, new Date()), 0);

		Assert.assertEquals(DateUtil.jourEntreDeuxDate(DateUtil.parseDateTime("01/01/2017 12:00"), DateUtil.parseDateTime("01/01/2017 22:00")), 1);
		Assert.assertEquals(DateUtil.jourEntreDeuxDate(DateUtil.parseDateTime("01/01/2017 12:00"), DateUtil.parseDateTime("02/01/2017 12:00")), 1);
		Assert.assertEquals(DateUtil.jourEntreDeuxDate(DateUtil.parseDateTime("01/01/2017 12:00"), DateUtil.parseDateTime("02/01/2017 13:01")), 2);
	}

	@Test
	public void isDateBeforeXAnnee() {
		Assert.assertFalse(DateUtil.isDateBeforeXAnnee(null, null));
		Assert.assertFalse(DateUtil.isDateBeforeXAnnee(new Date(), null));
		Assert.assertFalse(DateUtil.isDateBeforeXAnnee(null, 10));

		Date date = DateUtil.parseDateTime("02/10/2030 12:00");

		Assert.assertTrue(DateUtil.isDateBeforeXAnnee(date, 50));
		Assert.assertFalse(DateUtil.isDateBeforeXAnnee(date, 2));
	}
}
