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


import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

public class IOUtil {

	private static Logger logger = LoggerFactory.getLogger(IOUtil.class);

	/**
	 * @param content contenu à tester
	 * @return null si content est null, ou vide, ou en cas d'erreur de détection du type mime
	 */
	public static String getMimeType(byte[] content) {
		if (content == null || content.length == 0) {
			return null;
		}
		try {
			MagicMatch match = Magic.getMagicMatch(content);
			return match.getMimeType();
		} catch (Exception e) {
			return "text/plain";
		}
	}

	/**
	 * Permet de redimensionner une image
	 *
	 * @param fileData l'image
	 * @param width la largeur souhaitée
	 * @param height la hauteur souhaitée
	 * @return l'image modifiée
	 * @throws IOException si le fichier n'a pas pu être lu
	 */
	public static byte[] scale(byte[] fileData, int width, int height) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(fileData);
		try {
			BufferedImage img = ImageIO.read(in);
			BufferedImage result = Scalr.resize(img, Scalr.Method.QUALITY, width, height);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ImageIO.write(result, "png", buffer);
			return buffer.toByteArray();
		} catch (IIOException e) {
			return null;
		}
	}

	/**
	 * Lit le contenu d'un fichier, et le convertit en base 64
	 *
	 * @param file fichier
	 * @return contenu base 64
	 * @throws IOException exception
	 */
	public static String readFileToBase64(File file) throws IOException {
		byte[] bytes;
		bytes = Files.readAllBytes(file.toPath());
		StringBuilder sb = new StringBuilder();
		sb.append("data:" + getMimeType(bytes) + ";base64,");
		sb.append(org.apache.commons.codec.binary.StringUtils.newStringUtf8(Base64.encodeBase64(bytes, false)));
		return sb.toString();
	}

	/**
	 * @return le contenu d'une url sous forme d'une chaîne de caractères
	 */
	public static String readUrlContent(String url) {
		InputStream in = null;
		try {
			in = new URL(url).openStream();
			return IOUtils.toString(in, "UTF-8");
		} catch (Exception e) {
			logger.error("Erreur sur lecture du contenu de l'URL : " + url, e);
		} finally {
			if (in != null) {
				IOUtils.closeQuietly(in);
			}
		}
		return null;
	}
}
