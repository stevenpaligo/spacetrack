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
package com.stevenpaligo.spacetrack.client.predicate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.stevenpaligo.spacetrack.DelayBeforeEachTestExtension;
import com.stevenpaligo.spacetrack.TestUtils;
import com.stevenpaligo.spacetrack.client.SatCatQuery;
import com.stevenpaligo.spacetrack.client.SatCatQuery.SatCatQueryField;
import com.stevenpaligo.spacetrack.client.credential.CredentialProvider;
import com.stevenpaligo.spacetrack.client.query.QueryField;
import lombok.EqualsAndHashCode;

@ExtendWith(DelayBeforeEachTestExtension.class)
public class ContainsTests {

  private static CredentialProvider credentials = TestUtils.getCredentials();


  @Test
  @DisplayName("Contains: Constructor parameter validation")
  public void test1() {

    // disallowed values
    assertThrows(IllegalArgumentException.class, () -> {
      new Contains<>(null, "TEST");
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Contains<>(new TestQueryField(), (String) null);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Contains<>(null, 1);
    });

    assertThrows(IllegalArgumentException.class, () -> {
      new Contains<>(new TestQueryField(), (Integer) null);
    });


    // allowed values
    assertDoesNotThrow(() -> {
      new Contains<>(new TestQueryField(), "TEST");
    });

    assertDoesNotThrow(() -> {
      new Contains<>(new TestQueryField(), 1);
    });
  }


  @Test
  @DisplayName("Contains: Query parameter format and correct contents")
  public void test2() {

    // string value
    assertEquals("NORAD_CAT_ID/~~ABC", new Contains<>(new TestQueryField(), "ABC").toQueryParameter());


    // numeric value
    assertEquals("NORAD_CAT_ID/~~255", new Contains<>(new TestQueryField(), 255).toQueryParameter());
  }


  @Test
  @DisplayName("Contains: Getters")
  public void test3() {

    // field
    assertEquals(new TestQueryField(), new Contains<>(new TestQueryField(), "ABC").getField());


    // value
    assertEquals("ABC", new Contains<>(new TestQueryField(), "ABC").getValue());
  }


  @Test
  @DisplayName("Contains: Successful call")
  public void test4() {

    assertDoesNotThrow(() -> {

      new SatCatQuery().setCredentials(credentials).addPredicate(new Contains<>(SatCatQueryField.INTERNATIONAL_DESIGNATOR, "2021-069")).execute();
    });
  }


  @EqualsAndHashCode
  private static class TestQueryField implements QueryField {

    @Override
    public String getQueryFieldName() {

      return "NORAD_CAT_ID";
    }
  }
}
