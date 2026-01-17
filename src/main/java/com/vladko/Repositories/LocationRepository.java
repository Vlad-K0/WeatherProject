package com.vladko.Repositories;

import com.querydsl.jpa.impl.JPAQuery;
import com.vladko.Entity.Locations;
import com.vladko.Entity.User;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.vladko.Entity.QLocations.locations;

@Repository
public class LocationRepository extends BaseRepository<Integer, Locations> {

    public LocationRepository(SessionFactory sessionFactory) {
        super(Locations.class, sessionFactory);
    }

    public List<Locations> findByUser(User user) {
        @Cleanup
        Session session = sessionFactory.openSession();
        return new JPAQuery<Locations>(session)
                .select(locations)
                .from(locations)
                .where(locations.user.eq(user))
                .fetch();
    }
}
