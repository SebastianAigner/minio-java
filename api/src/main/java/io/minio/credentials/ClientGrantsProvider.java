/*
 * MinIO Java SDK for Amazon S3 Compatible Cloud Storage, (C) 2020 MinIO, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.minio.credentials;

import io.minio.Xml;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * Credential provider using <a
 * href="https://github.com/minio/minio/blob/master/docs/sts/client-grants.md">AssumeRoleWithClientGrants
 * API</a>.
 */
public class ClientGrantsProvider extends WebIdentityClientGrantsProvider {
  public ClientGrantsProvider(
      @Nonnull Supplier<Jwt> supplier,
      @Nonnull String stsEndpoint,
      @Nullable Integer durationSeconds,
      @Nullable String policy,
      @Nullable OkHttpClient customHttpClient) {
    super(supplier, stsEndpoint, durationSeconds, policy, null, null, customHttpClient);
  }

  @Override
  protected HttpUrl.Builder newUrlBuilder(Jwt jwt) {
    HttpUrl.Builder urlBuilder =
        newUrlBuilder(
            stsEndpoint,
            "AssumeRoleWithClientGrants",
            getDurationSeconds(jwt.expiry()),
            policy,
            null,
            null);
    return urlBuilder.addQueryParameter("Token", jwt.token());
  }

  @Override
  protected Credentials parseResponse(Response response) throws XmlParserException, IOException {
    ClientGrantsResponse result =
        Xml.unmarshal(ClientGrantsResponse.class, response.body().charStream());
    return result.credentials();
  }

  /** Object representation of response XML of AssumeRoleWithClientGrants API. */
  @Root(name = "AssumeRoleWithClientGrantsResponse", strict = false)
  @Namespace(reference = "https://sts.amazonaws.com/doc/2011-06-15/")
  public static class ClientGrantsResponse {
    @Path(value = "AssumeRoleWithClientGrantsResult")
    @Element(name = "Credentials")
    private Credentials credentials;

    public Credentials credentials() {
      return credentials;
    }
  }
}