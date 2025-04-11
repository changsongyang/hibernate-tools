/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 *
 * Copyright 2024-2025 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.tool.orm.jbt.internal.factory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.tool.orm.jbt.api.wrp.QueryWrapper;
import org.hibernate.tool.orm.jbt.api.wrp.SessionFactoryWrapper;
import org.hibernate.tool.orm.jbt.api.wrp.SessionWrapper;
import org.hibernate.tool.orm.jbt.internal.wrp.AbstractWrapper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class SessionWrapperFactory {

	public static SessionWrapper createSessionWrapper(Session wrappedSession) {
		return new SessionWrapperImpl(wrappedSession);
	}
	
	private static class SessionWrapperImpl 
			extends AbstractWrapper
			implements SessionWrapper {
		
		private Session session = null;
		
		private SessionWrapperImpl(Session session) {
			this.session = session;
		}
		
		@Override 
		public Session getWrappedObject() { 
			return session; 
		}
		
		@Override 
		public String getEntityName(Object o) { 
			return session.getEntityName(o); 
		}

		@Override 
		public SessionFactoryWrapper getSessionFactory() { 
			SessionFactory sf = session.getSessionFactory();
			return sf == null ? null : SessionFactoryWrapperFactory.createSessionFactoryWrapper(sf); 
		}

		@Override 
		public QueryWrapper createQuery(String s) { 
			org.hibernate.query.Query<?> query = session.createQuery(s, null);
			return QueryWrapperFactory.createQueryWrapper(query);
		}

		@Override 
		public boolean isOpen() { 
			return session.isOpen(); 
		}

		@Override 
		public void close() { 
			session.close(); 
		}

		@Override 
		public boolean contains(Object o) { 
			boolean result = false;
			try {
				result = session.contains(o);
			} catch (IllegalArgumentException e) {
				String message = e.getMessage();
				if (!(message.startsWith("Class '") && message.endsWith("' is not an entity class"))) {
					throw e;
				}
			}
			return result;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override 
		public QueryWrapper createCriteria(Class<?> c) {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery(c);
			criteriaQuery.select((Root)criteriaQuery.from(c));
			Query<?> query = ((Session)getWrappedObject()).createQuery(criteriaQuery);
			return QueryWrapperFactory.createQueryWrapper(query);
		}

	}

}
