package com.feedbacks.FeedbackSystem.configure;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
public class HibernateFilterConfig {

    private final EntityManager entityManager;

    public HibernateFilterConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void enableFilters(){
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("deletedCourseFilter");
        session.enableFilter("deletedFeedbackFilter");
        session.enableFilter("deletedInstructorFilter");
    }
}
