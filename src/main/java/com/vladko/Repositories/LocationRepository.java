package com.vladko.Repositories;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vladko.Entity.Location;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.vladko.Entity.QLocation.location;

@Component
public class LocationRepository extends BaseRepository<Integer, Location> {
    public LocationRepository(SessionFactory sessionFactory) {
        super(Location.class, sessionFactory);
    }

    public List<Location> getLocationsByUserName(String userName) {
        @Cleanup
        Session session = sessionFactory.openSession();
        JPAQueryFactory queryFactory = new JPAQueryFactory(session);

        return queryFactory
                .selectFrom(location)
                .where(location.user.login.eq(userName))
                .fetch();
    }

}
