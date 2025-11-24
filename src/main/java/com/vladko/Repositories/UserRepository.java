package com.vladko.Repositories;


import com.vladko.Entity.Users;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;

@Component
public class UserRepository extends BaseRepository<Integer, Users>{
    private static final String CREATE_USER_HQL = "";

    public UserRepository(SessionFactory sessionFactory) {
        super(Users.class, sessionFactory);
    }
}
