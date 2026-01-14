package com.vladko.Repositories;


import com.querydsl.jpa.impl.JPAQuery;
import com.vladko.Entity.Location;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.vladko.Entity.QLocations.locations;

@Component
public class LocationRepository extends BaseRepository<Integer, Location> {
    public LocationRepository(SessionFactory sessionFactory) {
        super(Location.class, sessionFactory);
    }


    public List<Location> getLocationsByUserName(String username) {
        Session session = sessionFactory.getCurrentSession();
        return new JPAQuery<Session>(session)
                .select(locations)
                .from(locations)
                .where(locations.user.login.eq(username)).fetch();

    }
}
