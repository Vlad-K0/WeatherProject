package com.vladko.Repositories;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vladko.DTO.UserDTO;
import com.vladko.Entity.QUser;
import com.vladko.Entity.User;

import java.util.List;
import java.util.Optional;

import lombok.Cleanup;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaQuery;

import static com.vladko.Entity.QUser.user;

@Component
public class UserRepository extends BaseRepository<Integer, User> {
    private static final String GET_HASHED_PASSWORD_HQL = "select u.password from users u where u.login = :login";
    private static final String GET_USER_BY_NAME_HQL = "select u from users u where u.login = :login";

    public UserRepository(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }

    public List<User> findALl() {
        @Cleanup
        Session session = sessionFactory.openSession();
        return new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .fetch();
    }

    public Optional<String> getPasswordByLogin(String username) {
        @Cleanup
        Session session = sessionFactory.openSession();
//        return session.createQuery(GET_HASHED_PASSWORD_HQL, String.class)
//                .setParameter("login", username)
//                .uniqueResultOptional();
        return Optional.ofNullable(
                new JPAQuery<User>(session)
                        .select(user.password)
                        .from(user)
                        .where(user.login.eq(username))
                        .fetchOne()
        );
    }

    public Optional<User> findByUsername(String username) {
        @Cleanup
        Session session = sessionFactory.openSession();
//        CriteriaQuery<User> criteria = session.getCriteriaBuilder().createQuery(User.class).where();
//        criteria.from(User.class);
//        return Optional.of(new User());
        return Optional.ofNullable(new JPAQuery<User>(session)
                .select(user)
                .from(user)
                .where(user.login.eq(username))
                .fetch().get(0));

    }
}
