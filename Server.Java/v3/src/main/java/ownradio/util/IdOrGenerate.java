package ownradio.util;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.UUIDGenerator;
import ownradio.domain.AbstractEntity;

import java.util.UUID;

/**
 * Created by a.polunina on 17.04.2017.
 */

	public class IdOrGenerate extends UUIDGenerator {

		@Override
		public UUID generate(SessionImplementor session, Object obj) throws HibernateException {
			if (obj == null) throw new HibernateException(new NullPointerException());

			if (((AbstractEntity) obj).getRecid() == null) {
				UUID id = UUID.randomUUID(); // super.generate(session, obj);
				return id;
			} else {
				return ((AbstractEntity) obj).getRecid();

			}
		}
	}