package kz.greetgo.security.session;

import com.mongodb.client.MongoCollection;
import kz.greetgo.db.DbType;
import kz.greetgo.db.Jdbc;
import org.bson.Document;

public class SessionStorageBuilder {

  private SessionStorageBuilder() {}

  private SessionSerializer sessionSerializer = NativeJavaSerializer.create();

  public SessionStorageBuilder sessionSerializer(SessionSerializer sessionSerializer) {
    this.sessionSerializer = sessionSerializer;
    return this;
  }

  public static SessionStorageBuilder newBuilder() {
    return new SessionStorageBuilder();
  }

  public SessionStorageJdbcBuilder setJdbc(DbType dbType, Jdbc jdbc) {
    return new SessionStorageJdbcBuilder(dbType, jdbc, sessionSerializer);
  }

  public SessionStorageMongoBuilder setMongoCollection(MongoCollection<Document> collection) {
    return new SessionStorageMongoBuilder(collection, sessionSerializer);
  }
}
