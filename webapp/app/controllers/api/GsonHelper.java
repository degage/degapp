package controllers.api;

import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class GsonHelper {
  private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private static DateTimeFormatter dateTimeformatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  public static Gson getGson() {
    return new GsonBuilder()
      .serializeNulls()
      .registerTypeAdapter(LocalDate.class, localDateSerializer)
      .registerTypeAdapter(LocalDate.class, localDateDeserializer)
      .registerTypeAdapter(LocalDateTime.class, localDateTimeSerializer)
      .registerTypeAdapter(LocalDateTime.class, localDateTimeDeserializer)
      .excludeFieldsWithoutExposeAnnotation().create();
  }

  private static JsonSerializer<LocalDate> localDateSerializer = new JsonSerializer<LocalDate>() {
    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext
               context) {
      return src == null ? null : new JsonPrimitive(src.format(formatter));
    }
  };

  private static JsonDeserializer<LocalDate> localDateDeserializer = new JsonDeserializer<LocalDate>() {
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT,
         JsonDeserializationContext context) throws JsonParseException {
      return json == null ? null : LocalDate.parse(json.getAsString(), formatter);
    }
  };

  private static JsonSerializer<LocalDateTime> localDateTimeSerializer = new JsonSerializer<LocalDateTime>() {
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext
               context) {
      return src == null ? null : new JsonPrimitive(src.format(dateTimeformatter));
    }
  };

  private static JsonDeserializer<LocalDateTime> localDateTimeDeserializer = new JsonDeserializer<LocalDateTime>() {
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT,
         JsonDeserializationContext context) throws JsonParseException {
      return json == null ? null : LocalDateTime.parse(json.getAsString(), dateTimeformatter);
    }
  };
}
