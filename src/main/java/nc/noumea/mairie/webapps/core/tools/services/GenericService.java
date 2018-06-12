package nc.noumea.mairie.webapps.core.tools.services;

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


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

public interface GenericService<T> {

	Class<? extends T> getClasseReferente();

	<S extends T> S save(S var1);

	<S extends T> Iterable<S> save(Iterable<S> var1);

	T findOne(Long var1);

	T findOneOrNull(Long var1);

	boolean exists(Long var1);

	Iterable<T> findAll();

	Page<T> findAll(Pageable pageable);

	long count();

	void delete(Long var1);

	void delete(T var1);

	void delete(Iterable<? extends T> var1);

	void deleteAll();

	List<T> construitListeActifInactif(List<T> liste);
}
