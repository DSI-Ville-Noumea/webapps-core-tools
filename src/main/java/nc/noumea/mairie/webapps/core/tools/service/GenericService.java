package nc.noumea.mairie.webapps.core.tools.service;

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

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import nc.noumea.mairie.webapps.core.tools.domain.Entity;
import nc.noumea.mairie.webapps.core.tools.error.TechnicalException;
import nc.noumea.mairie.webapps.core.tools.type.ActifInactif;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

@Transactional
public abstract class GenericService<T extends Entity, R extends JpaRepository> {

	@Autowired
	private ApplicationContext	applicationContext;

	private R					repository;

	protected R getRepository() {
		if (this.repository == null) {
			this.repository = (R) applicationContext.getBean(getRepositoryBeanName());
			if (this.repository == null) {
				throw new TechnicalException("Impossible de trouver le bean " + getRepositoryBeanName());
			}
		}
		return repository;
	}

	protected String getRepositoryBeanName() {
		return StringUtils.uncapitalize(getClasseReferente().getSimpleName()) + "Repository";
	}

	public Class<? extends T> getClasseReferente() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}


    public <S extends T> S save(S var1) {
        return (S) getRepository().saveAndFlush(var1);
	}

	public <S extends T> Iterable<S> saveAll(Iterable<S> var1) {
		return getRepository().saveAll(var1);
	}

	public T findOneOrNull(Long id) {
		Optional<T> optionalT = getRepository().findById(id);
		if (optionalT.isPresent())
			return optionalT.get();
		return null;
	}

	public T findOne(Long id) {
		Optional<T> optionalT = getRepository().findById(id);
		return optionalT.get();
	}

	public boolean exists(Long id) {
		return getRepository().existsById(id);
	}

	public Iterable<T> findAll() {
		return getRepository().findAll();
	}

	public Page<T> findAll(Pageable pageable) {
		return getRepository().findAll(pageable);
	}

	public long count() {
		return getRepository().count();
	}

	public void delete(Long entite) {
        getRepository().deleteById(entite);
	}

	public void delete(T entite) {
        getRepository().delete(entite);
	}

	public void deleteAll(Iterable<? extends T> collectionEntite) {
		getRepository().deleteAll(collectionEntite);
	}

	public void deleteAll() {
		getRepository().deleteAll();
	}

	public List<T> construitListeActifInactif(List<T> liste) {
		List<T> result = new ArrayList<>();
		Class classe = ((T) new Object()).getClass();
		if (!classe.isInstance(ActifInactif.class)) {
			return liste;
		}

		List<T> listeActif = new ArrayList<>();
		List<T> listeInactif = new ArrayList<>();
		for (T element : liste) {
			if (element instanceof ActifInactif) {
				if (((ActifInactif) element).getActif()) {
					listeActif.add(element);
				} else {
					listeInactif.add(element);
				}
			}

		}
		result.addAll(listeActif);
		result.addAll(listeInactif);

		return result;
	}
}
