package kz.greetgo.security.session;

/**
 * Обеспечивает преобразование объектов в строку и обратно для того чтобы их можно было помещать в сессию
 */
public interface SessionSerializer {

  /**
   * Преобразует объект в строку
   *
   * @param sessionHolder объект
   * @return строка, с информацией по объекту
   */
  String serializeToStr(Object sessionHolder);

  /**
   * Преобразует строку с информацией по объекту в оригинальный объект
   *
   * @param sessionHolderSerializedStr строка с информацией по обхекту
   * @param <T>                        класс объекта
   * @return оригинальный объект
   */
  <T> T deserializeFromStr(String sessionHolderSerializedStr);

}
