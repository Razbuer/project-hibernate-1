package com.game.repository;


import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    private PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "root");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.SHOW_SQL, true);

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        String SQL = "SELECT * FROM player LIMIT :limit OFFSET :offset";

        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery(SQL, Player.class)
                    .setParameter("limit", pageSize)
                    .setParameter("offset", pageNumber * pageSize);

            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery("Player_getAllCountQuery", Long.class);

            Long getAllCountQuery = query.getSingleResult();

            return Math.toIntExact(getAllCountQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.save(player);

            transaction.commit();
        }  catch (Exception e) {
            e.printStackTrace();
        }

        return player;
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.update(player);

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        Optional<Player> player = Optional.empty();

        try (Session session = sessionFactory.openSession()) {
            player = Optional.of(session.get(Player.class, id));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return player;
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();

            session.remove(player);

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}