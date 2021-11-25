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
package com.stevenpaligo.spacetrack.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.stevenpaligo.spacetrack.DelayBeforeEachTestExtension;
import com.stevenpaligo.spacetrack.client.LaunchSiteQuery.LaunchSite;
import com.stevenpaligo.spacetrack.client.LaunchSiteQuery.LaunchSiteQueryField;
import com.stevenpaligo.spacetrack.client.credential.CredentialProvider;
import com.stevenpaligo.spacetrack.client.predicate.Equal;
import com.stevenpaligo.spacetrack.client.predicate.Predicate;

@ExtendWith(DelayBeforeEachTestExtension.class)
public class LaunchSiteQueryTests {

  private static CredentialProvider credentials = TestUtils.getCredentials();


  @Test
  @DisplayName("LaunchSiteQuery: Result type matches the SpaceTrack schema")
  public void test1() {

    assertDoesNotThrow(() -> {

      ResultTypeValidator.validate(LaunchSite.class, new URL("https://www.space-track.org/basicspacedata/modeldef/class/launch_site/format/json"));
    });
  }


  @Test
  @DisplayName("LaunchSiteQuery: Query field enum matches the result type")
  public void test2() {

    assertDoesNotThrow(() -> {

      QueryFieldEnumValidator.validate(LaunchSiteQueryField.class, LaunchSite.class);
    });
  }


  @Test
  @DisplayName("LaunchSiteQuery: Successful call")
  public void test3() {

    assertDoesNotThrow(() -> {

      Predicate<LaunchSiteQueryField> predicate = new Equal<>(LaunchSiteQueryField.SITE_CODE, "AFETR");
      new LaunchSiteQuery().setCredentials(credentials).addPredicate(predicate).execute();
    });
  }
}
