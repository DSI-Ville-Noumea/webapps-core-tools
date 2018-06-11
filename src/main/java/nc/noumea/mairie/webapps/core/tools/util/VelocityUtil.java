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


import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import nc.noumea.mairie.webapps.core.tools.docx.EvaluationContext;

public class VelocityUtil {

	public static String evaluateVelocityStringTrim(EvaluationContext evaluationContext, String expression) {
		return evaluateVelocityStringTrim(evaluationContext.getContext(), expression);
	}

	/**
	 * Evalue une expression vélocity, sous forme d'une chaîne de caractères, sans blanc devant/derrière dans le résultat de problème de parsing. En cas de
	 * problème (parsing ou autre), soulève une exception.
	 *
	 * @param mapContexte contexte velocity
	 * @param expression expression à évaluer
	 * @return résultat sous forme de chaîne de caractère
	 */
	public static String evaluateVelocityStringTrim(Map<String, Object> mapContexte, String expression) {
		VelocityContext velocityContext = new VelocityContext(mapContexte);

		StringWriter writer = new StringWriter();
		boolean parsingOk = Velocity.evaluate(velocityContext, writer, "logTag", new StringReader(expression));
		if (!parsingOk) {
			throw new RuntimeException("Erreur sur evaluateVelocityStringTrim, expression=" + expression + ", contexte = " + mapContexte);
		}
		return StringUtils.trimToEmpty(writer.toString());
	}
}
