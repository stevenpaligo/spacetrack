/*
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stevenpaligo.spacetrack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.threeten.extra.scale.UtcInstant;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.NonNull;

public class ResultTypeValidator {

  private static final ObjectMapper jsonMapper = new ObjectMapper();


  private ResultTypeValidator() {

    // enforce static method usage
  }


  public static void validate(@NonNull Class<?> resultType, @NonNull URL schemaUrl) throws Exception {

    // filter the result type's fields down to those needing validation
    // (the static fields are the JSON property names)
    Field[] resultTypeFields = Arrays.stream(resultType.getDeclaredFields()).filter(f -> Modifier.isStatic(f.getModifiers()) == false).toArray(Field[]::new);


    // index the result type fields by their JSON property names
    Map<String, Field> jsonNameToResultTypeField = new HashMap<>();

    for (Field resultTypeField : resultTypeFields) {

      // get the JsonProperty annotation
      JsonProperty jsonProperty = resultTypeField.getAnnotation(JsonProperty.class);

      if (jsonProperty == null) {

        throw new Exception("One of the result type fields does not have a @JsonProperty annotation: " + resultTypeField.getName());
      }


      // get the JSON property name
      String jsonName = jsonProperty.value();

      if (jsonName.equals(JsonProperty.USE_DEFAULT_NAME)) {

        throw new Exception("One of the result type fields does not declare a JSON property name in its @JsonProperty annotation: " + resultTypeField.getName());
      }


      // map the property name to the field
      Field previouslyMappedField = jsonNameToResultTypeField.put(jsonName, resultTypeField);

      if (previouslyMappedField != null) {

        throw new Exception("One of the result type fields has a duplicate JSON property name in its @JsonProperty annotation: " + resultTypeField.getName());
      }
    }


    // download the schema from SpaceTrack as JSON
    JsonNode schema = jsonMapper.readTree(schemaUrl).get("data");


    // verify the result type matches the schema
    if (resultTypeFields.length != schema.size()) {

      throw new Exception("The schema and result type have different numbers of fields (schema: " + schema.size() + ", result type: " + resultTypeFields.length + ")");
    }

    for (int i = 0; i < schema.size(); i++) {

      JsonNode schemaField = schema.get(i);
      String schemaFieldName = schemaField.get("Field").asText();
      String schemaFieldType = schemaField.get("Type").asText();
      boolean schemaFieldNullable = schemaField.get("Null").asText().equals("YES");


      // verify the schema field exists in the result type
      Field resultTypeField = jsonNameToResultTypeField.get(schemaFieldName);

      if (resultTypeField == null) {

        throw new Exception("The result type is missing a schema field: " + schemaFieldName);
      }


      // verify the result type field is optional if and only if the schema field is nullable
      boolean resultTypeFieldOptional = resultTypeField.getType().equals(Optional.class);

      if (schemaFieldNullable != resultTypeFieldOptional) {

        if (resultTypeFieldOptional) {

          throw new Exception("A result type field is optional, but its corresponding schema field is not: " + resultTypeField.getName());

        } else {

          throw new Exception("A result type field is required, but its corresponding schema field is not: " + resultTypeField.getName());
        }
      }


      // verify the data types match unless there is a custom deserializer
      if (resultTypeField.isAnnotationPresent(JsonDeserialize.class) == false) {

        Class<?> resultFieldDataType;

        if (resultTypeFieldOptional) {

          resultFieldDataType = (Class<?>) ((ParameterizedType) resultTypeField.getGenericType()).getActualTypeArguments()[0];

        } else {

          resultFieldDataType = resultTypeField.getType();
        }

        if (schemaFieldType.startsWith("bigint(")) {

          if (resultFieldDataType != Long.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.equals("char(1)")) {

          if (resultFieldDataType != String.class && resultFieldDataType != Character.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("char(")) {

          if (resultFieldDataType != String.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.equals("date")) {

          if (resultFieldDataType != LocalDate.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.equals("datetime")) {

          if (resultFieldDataType != UtcInstant.class && resultFieldDataType != Instant.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("decimal(") && schemaFieldType.endsWith(",0)")) {

          if (resultFieldDataType != BigInteger.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("decimal(")) {

          if (resultFieldDataType != BigDecimal.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("double(") || schemaFieldType.equals("double")) {

          if (resultFieldDataType != Double.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.equals("enum('Y','N')")) {

          if (resultFieldDataType != Boolean.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("enum(")) {

          if (resultFieldDataType != String.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.equals("float")) {

          if (resultFieldDataType != Float.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("int(") || schemaFieldType.startsWith("mediumint(") || schemaFieldType.startsWith("smallint(") || schemaFieldType.startsWith("tinyint(")) {

          if (resultFieldDataType != Integer.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else if (schemaFieldType.startsWith("varchar(")) {

          if (resultFieldDataType != String.class) {

            throw new Exception("A result field's data type is incompatible with the schema field's type: " + resultTypeField.getName());
          }

        } else {

          throw new Exception("Unsupported schema field type: " + schemaFieldType);
        }
      }
    }


    // verify any optional fields in the result type are initialized by default
    Object resultTypeInstance = resultType.newInstance();

    for (Field resultTypeField : resultTypeFields) {

      resultTypeField.setAccessible(true);


      if (resultTypeField.getType().equals(Optional.class) && resultTypeField.get(resultTypeInstance) == null) {

        throw new Exception("A result field's data type is an `Optional`, but it is not initialized by default: " + resultTypeField.getName());
      }
    }
  }
}
