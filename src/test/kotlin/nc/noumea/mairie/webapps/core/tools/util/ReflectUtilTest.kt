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
package nc.noumea.mairie.webapps.core.tools.util

import org.junit.Test
import kotlin.test.assertEquals

class ReflectUtilTest {

    @Test
    fun testFindObjectFromPath() {
        assertEquals("toto", ReflectUtil.findObjectFromPath("sousObjet1.sousObjet2.propriete1", RootObjet()))
        assertEquals(null, ReflectUtil.findObjectFromPath("sousObjet1Inconnu", RootObjet()))
        assertEquals(null, ReflectUtil.findObjectFromPath("sousObjet1.sousObjet2Inconnu", RootObjet()))
        assertEquals(null, ReflectUtil.findObjectFromPath("sousObjet1.sousObjet2.proprieteInconnue", RootObjet()))
    }

    class RootObjet {
        val sousObjet1 = SousObjet1()
    }

    class SousObjet1 {
        val sousObjet2 = SousObjet2()
    }

    class SousObjet2 {
        val propriete1 = "toto"
        val propriete2 = true
        val propriete3 = false
    }
}
