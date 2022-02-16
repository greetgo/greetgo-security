package kz.greetgo.security.session;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class SessionStorageMongoBuilder {
  private final MongoCollection<Document> collection;
  private final SessionSerializer sessionSerializer;

  public SessionStorageMongoBuilder(MongoCollection<Document> collection,
                                    SessionSerializer sessionSerializer) {
    this.collection = collection;
    this.sessionSerializer = sessionSerializer;
  }

  public SessionStorage build() {
    return new SessionStorageMongo(collection, sessionSerializer);
  }
}
