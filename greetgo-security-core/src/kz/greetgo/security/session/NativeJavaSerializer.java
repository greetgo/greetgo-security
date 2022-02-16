package kz.greetgo.security.session;

import kz.greetgo.security.errors.SerializedClassChanged;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static kz.greetgo.security.util.Base64Util.base64ToBytes;
import static kz.greetgo.security.util.Base64Util.bytesToBase64;

public class NativeJavaSerializer {
  static byte[] serialize(Object object) {
    try {

      ByteArrayOutputStream bOut = new ByteArrayOutputStream();
      ObjectOutputStream    out  = new ObjectOutputStream(bOut);

      out.writeObject(object);

      return bOut.toByteArray();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  static <T> T deserialize(byte[] bytes) {
    try {

      if (bytes == null) {
        return null;
      }

      ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
      ObjectInputStream    in  = new ObjectInputStream(bIn);
      //noinspection unchecked
      return (T) in.readObject();

    } catch (RuntimeException e) {
      throw e;
    } catch (InvalidClassException | ClassNotFoundException e) {
      throw new SerializedClassChanged(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  static String serializeToStrStatic(Object object) {
    return bytesToBase64(serialize(object));
  }

  static <T> T deserializeFromStrStatic(String serializedStr) {
    return deserialize(base64ToBytes(serializedStr));
  }

  public static SessionSerializer create() {
    return new SessionSerializer() {
      @Override
      public String serializeToStr(Object sessionHolder) {
        return serializeToStrStatic(sessionHolder);
      }

      @Override
      public <T> T deserializeFromStr(String sessionHolderSerializedStr) {
        return deserializeFromStrStatic(sessionHolderSerializedStr);
      }
    };
  }
}
