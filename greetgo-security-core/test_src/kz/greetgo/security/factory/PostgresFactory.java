package kz.greetgo.security.factory;

import kz.greetgo.db.AbstractJdbcWithDataSource;
import kz.greetgo.db.Jdbc;
import kz.greetgo.db.TransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class PostgresFactory {
  private final PgParams params = new PgParams();

  public Jdbc create() {
    return new AbstractJdbcWithDataSource() {
      @Override
      protected TransactionManager getTransactionManager() {
        return null;
      }

      @Override
      protected DataSource getDataSource() {
        return new AbstractDataSource() {
          @Override
          public Connection getConnection() {
            try {
              return DriverManager.getConnection(params.url(), params.user(), params.password());
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
          }
        };
      }
    };
  }


}
