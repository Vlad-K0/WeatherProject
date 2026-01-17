package com.vladko.Repositories;


import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vladko.Entity.User;
import java.util.Optional;
import lombok.Cleanup;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;


import static com.vladko.Entity.QUser.user;

@Component
public class UserRepository extends BaseRepository<Integer, User> {

    public UserRepository(SessionFactory sessionFactory) {
        super(User.class, sessionFactory);
    }

    public Optional<String> getPasswordByLogin(String username) {
        @Cleanup
        Session session = sessionFactory.openSession();
        JPAQueryFactory queryFactory = new JPAQueryFactory(session);

        String password = queryFactory
                .select(user.password)
                .from(user)
                .where(user.login.eq(username))
                .fetchOne();

        return Optional.ofNullable(password);
    }

    public Optional<User> findByUsername(String username) {
        @Cleanup
        Session session = sessionFactory.openSession();
        JPAQueryFactory queryFactory = new JPAQueryFactory(session);

        User foundUser = queryFactory
                .selectFrom(user)
                .where(user.login.eq(username))
                .fetchFirst();

        return Optional.ofNullable(foundUser);
    }
}
