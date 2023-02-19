package cc.tonyhook.berry.backend.security;

import org.hibernate.Session;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

@Component
public class BerryRepositoryEventRegistration {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private BerryRepositoryEventListener listener;

    @PostConstruct
    private void registerListeners() {
        // create transactional EntityManager
        EntityManager entityManager1 = entityManager.getEntityManagerFactory().createEntityManager();

        final EventListenerRegistry registry = ((SessionFactoryImpl) entityManager1.unwrap(Session.class).getSessionFactory())
                .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_INSERT)
                .appendListener(listener);
        registry.getEventListenerGroup(EventType.POST_INSERT)
                .appendListener(listener);
    }

}
